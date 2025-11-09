package app.product;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import app.exception.ProductNotFoundException;
import app.fta.FTAService;
import app.query.TariffApiClient;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final TariffApiClient apiClient;
    private final FTAService ftaService;

    public ProductService(ProductRepository productRepository, TariffApiClient apiClient, FTAService ftaService) {
        this.productRepository = productRepository;
        this.apiClient = apiClient;
        this.ftaService = ftaService;
    }

    /**
     * Fetches tariff data from the external API and saves it. Automatically called
     * every Monday.
     */
    @Scheduled(cron = "0 0 0 * * MON")
    public void fetchExternal() {
        try {
            List<String> keywords = List.of("sugar", "bread", "milk", "egg", "rice");

            for (String keyword : keywords) {
                List<Map<String, Object>> response = apiClient.searchTariffArticles(keyword);

                for (Map<String, Object> map : response) {
                    String htsCode = (String) map.get("htsno");

                    Product product = mapToProduct(map, htsCode);
                    product.setCategory(keyword);
                    product.setFetchDate(LocalDate.now());

                    Optional<Product> latestRecord = getMostRecentProductPrice(htsCode);

                    if (!latestRecord.isPresent() || !latestRecord.get().equals(product)) {
                        productRepository.save(product);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching data from external API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Finds product by HTS code using three-tier fallback strategy:
     * local database → category search → external API.
     *
     * @param htsCode The HTS code to search for
     * @return Product matching the HTS code
     * @throws ProductNotFoundException if product not found through any strategy
     */
    public Product findProductByHtsCode(String htsCode) {
        System.out.println("Fetching product by HTS code: " + htsCode);

        // Try local database first
        Optional<Product> product = getMostRecentProductPrice(htsCode);
        if (product.isPresent()) {
            System.out.println("Product found in database: " + product.get());
            return product.get();
        }

        // Try category search
        System.out.println("Product not in database, searching by keyword: " + htsCode);
        Optional<Product> searchResult = searchByCategory(htsCode);
        if (searchResult.isPresent()) {
            System.out.println("Product found by search: " + searchResult.get());
            return searchResult.get();
        }

        // Try external API
        System.out.println("Product not found locally, fetching from external API: " + htsCode);
        Optional<Product> apiResult = fetchFromExternalApi(htsCode);
        if (apiResult.isPresent()) {
            System.out.println("Product found from API: " + apiResult.get().getHtsCode());
            return apiResult.get();
        }

        System.out.println("No product found with HTS code: " + htsCode);
        throw new ProductNotFoundException("No product found with HTS code: " + htsCode);
    }

    /**
     * Searches for product by category keyword.
     * Returns exact match if available, otherwise first result.
     *
     * @param htsCode The keyword to search by
     * @return Optional containing matched product or empty if none found
     */
    private Optional<Product> searchByCategory(String htsCode) {
        Optional<List<Product>> searchResults = getProductsByCategory(htsCode);

        if (searchResults.isEmpty() || searchResults.get().isEmpty()) {
            return Optional.empty();
        }

        return searchResults.get().stream()
                .filter(prod -> prod.getHtsCode().equals(htsCode))
                .findFirst()
                .or(() -> Optional.of(searchResults.get().get(0)));
    }

    /**
     * Fetches product from external API.
     * Returns exact match if available, otherwise first result.
     *
     * @param htsCode The HTS code to fetch
     * @return Optional containing product from API or empty if fetch fails
     */
    private Optional<Product> fetchFromExternalApi(String htsCode) {
        try {
            List<Map<String, Object>> apiResults = apiClient.searchTariffArticles(htsCode);
            if (apiResults == null || apiResults.isEmpty()) {
                return Optional.empty();
            }

            Map<String, Object> match = apiResults.stream()
                    .filter(item -> htsCode.equals(item.get("htsno")))
                    .findFirst()
                    .orElse(apiResults.get(0));

            return Optional.of(mapToProduct(match, htsCode));

        } catch (Exception e) {
            System.out.println("Error fetching from external API: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Maps API response data to Product entity.
     *
     * @param apiData Map containing API response fields
     * @param htsCode Fallback HTS code if not present in API data
     * @return Product instance populated from API data
     */
    private Product mapToProduct(Map<String, Object> apiData, String htsCode) {
        Product product = new Product();
        product.setHtsCode(apiData.get("htsno") != null ? apiData.get("htsno").toString() : htsCode);
        product.setDescription(apiData.get("description") != null ? apiData.get("description").toString() : null);
        product.setGeneral(apiData.get("general") != null ? apiData.get("general").toString() : null);
        product.setSpecial(apiData.get("special") != null ? apiData.get("special").toString() : null);
        return product;
    }

    /**
     * Retrieves most recent product by HTS code from repository.
     *
     * @param htsCode The HTS code to look up
     * @return Optional containing most recent product or empty if not found
     */
    public Optional<Product> getMostRecentProductPrice(String htsCode) {
        return productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);
    }

    /**
     * Retrieves all products matching the HTS code and returns the most recent one.
     *
     * @param htsCode The HTS code to search for
     * @return Optional containing the most recent product or empty if not found
     */
    public Optional<Product> getProductByHtsCode(String htsCode) {
        System.out.println("ProductService: Looking for HTS code: " + htsCode);
        Optional<List<Product>> products = productRepository.findByHtsCode(htsCode);
        System.out.println("ProductService: Found " + (products.isPresent() ? products.get().size() : 0) + " products");

        // Return the most recent product if available
        Optional<Product> result = products.flatMap(list -> list.stream()
                .max((p1, p2) -> p1.getFetchDate().compareTo(p2.getFetchDate())));

        System.out.println(
                "ProductService: Returning product: " + (result.isPresent() ? result.get().getHtsCode() : "none"));
        return result;
    } 

    /**
     * Searches products by category or HTS code prefix.
     *
     * @param keyword Category name or HTS code prefix to search for
     * @return Optional containing list of matching products or empty list if none
     *         found
     */
    public Optional<List<Product>> getProductsByCategory(String keyword) {
        return Optional.of(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(keyword, keyword)
                .orElse(List.of()));
    }

    /**
     * Retrieves the most recent product price for a given HTS code on or before the
     * specified date.
     *
     * @param htsCode The HTS code to search for
     * @param date    The date threshold for fetch date
     * @return Optional containing the product or empty if not found
     */
    public Optional<Product> getMostRecentProductPriceAtTime(String htsCode, LocalDate date) {
        return productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(htsCode, date);
    }

    /**
     * Given a keyword, return all the products that has the keyword in the
     * description and are supercategories (e.g. "1704").
     * A `ProductNotFoundException` is thrown when there are not matching records
     * in the database.
     * 
     * @param keyword Keyword to search for
     * @return Set of corresponding super-products
     */
    public Set<Product> getHighestLevelCategory(String keyword) {
        Optional<List<Product>> products = productRepository.findByCategoryContainingIgnoreCase(keyword);

        if (!products.isPresent()) {
            throw new ProductNotFoundException("Error: Product with keyword " + keyword + " cannot be found!");
        }

        return products.get().stream()
                .filter(p -> !p.getHtsCode().contains("."))
                .collect(Collectors.toSet());
    }

    /**
     * Given a HTS code, return the subcategories. For instance, for HTS "1704",
     * return "1704.00", "1704.03", ...
     * A `ProductNotFoundException` is thrown when no matching records in the
     * database.
     * 
     * @param htsCode Target HTS code
     * @return Set of all products in subcategories
     */
    public Set<Product> getNextLevelCategory(String htsCode) {
        Optional<List<Product>> products = productRepository.findByHtsCodeStartingWith(htsCode + ".");

        if (!products.isPresent()) {
            throw new ProductNotFoundException("Error: Product with HTS code " + htsCode + ".* not found!");
        }

        return products.get().stream()
                .collect(Collectors.toMap(
                        Product::getHtsCode,
                        Function.identity(),
                        (p1, p2) -> p1.getFetchDate().isAfter(p2.getFetchDate()) ? p1 : p2))
                .values().stream()
                .collect(Collectors.toSet());
    }

    /**
     * Selects the appropriate price for a product based on country.
     * Returns special price if available for the country, otherwise returns general
     * price.
     *
     * @param product The product to get price from
     * @param country The country code to check for special pricing
     * @return Price string or empty string if no price available
     */
    public String selectPrice(Product product, String country) {
        String special = product.getSpecial();
        String general = product.getGeneral();

        if (special != null && special != "" && special.contains(country)) {
            return special.split(" ")[0];
        }

        return general != null ? general : "";
    }

    /**
     * Retrieves historical prices for a product across all fetch dates.
     *
     * @param htsCode The HTS code to search for
     * @param country The country code for price selection
     * @return Map of fetch dates to prices
     * @throws HTSCodeNotFoundException if product with HTS code not found
     */
    public Map<LocalDate, String> getHistoricalPrices(String htsCode, String country) {
        Optional<List<Product>> products = productRepository.findByHtsCode(htsCode);

        if (!products.isPresent()) {
            throw new ProductNotFoundException("Error: Product with HTS Code " + htsCode + " not found!");
        }

        return products.get().stream().collect(
                Collectors.toMap(
                        Product::getFetchDate,
                        product -> selectPrice(product, country)));
    }

    /**
     * Combines historical and future prices for a product.
     *
     * @param htsCode The HTS code to search for
     * @param country The country code for price selection
     * @return Map of dates to prices, including both historical and future prices
     */
    public Map<LocalDate, String> getPrices(String htsCode, String country) {
        Map<LocalDate, String> historicalPrices = getHistoricalPrices(htsCode, country);
        Map<LocalDate, String> futurePrices = ftaService.getFuturePrices(country, htsCode);
        historicalPrices.putAll(futurePrices);
        return historicalPrices;
    }

    /**
     * Maps all ISO country codes to their respective prices for a product.
     *
     * @param htsCode The HTS code to search for
     * @return Map of country codes to prices
     * @throws HTSCodeNotFoundException if product with HTS code not found
     */
    public Map<String, String> mapCountryToPrice(String htsCode) {
        Optional<Product> product = productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);
        String[] countries = Locale.getISOCountries();

        System.out.println(product);

        if (!product.isPresent()) {
            throw new ProductNotFoundException("Product with HTS code " + htsCode + " not found!");
        }

        Map<String, String> pricesByCountry = new HashMap<>();

        for (String country : countries) {
            pricesByCountry.put(country, selectPrice(product.get(), country));
        }
        return pricesByCountry;
    }
}
