package app.product;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import app.exception.HTSCodeNotFoundException;
import app.exception.ProductNotFoundException;
import app.favourites.FavouritesService;
import app.fta.FTAService;
import app.query.QueryService;
import app.email.EmailService;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final QueryService queryService;
    private final FTAService ftaService;
    private final FavouritesService favouritesService;
    private final EmailService emailService; 

    ObjectMapper objectMapper = new ObjectMapper();

    public ProductService(ProductRepository productRepository, QueryService queryService, FTAService ftaService, FavouritesService favouritesService, EmailService emailService) {
        this.productRepository = productRepository;
        this.queryService = queryService;
        this.ftaService = ftaService;
        this.favouritesService = favouritesService; 
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void fetchExternal() {
        try {
            List<String> keywords = List.of("sugar", "bread", "milk", "egg", "rice");

            for (String keyword : keywords) {
                List<Map<String, Object>> response = queryService.searchTariffArticles(keyword);

                for (Map<String, Object> map : response) {
                    // System.out.println("Map:\n" +
                    // objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));

                    // retrieve relevant information
                    String htsCode = (String) map.get("htsno");
                    String description = (String) map.get("description");
                    String category = keyword;
                    String general = (String) map.get("general");
                    String special = (String) map.get("special");

                    // create new product and store it
                    Product product = new Product(htsCode, LocalDate.now(), description, general, special, category);
                    Optional<Product> latestRecord = productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);

                    // check with latest record - if same then don't save
                    if (!latestRecord.isPresent()) {
                        productRepository.save(product);
                    } else if (!latestRecord.get().equals(product)) {
                    // Product has changed
                        Product oldProduct = latestRecord.get();
                        String oldGeneral = oldProduct.getGeneral();
                        String newGeneral = general;
                    
                        // Save the updated product
                        productRepository.save(product);
                    
                        // Notify users if the general rate changed
                        if (oldGeneral != null && newGeneral != null && !oldGeneral.equals(newGeneral)) {
                            notifyUsersOfPriceChange(htsCode, oldGeneral, newGeneral);  
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching data from external API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Optional<Product> getProductPrice(String htsCode) {
        return productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);
    }

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

    public Optional<List<Product>> getProductsByCategory(String keyword) {
        return Optional.of(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(keyword, keyword)
                .orElse(List.of()));
    }

    public Optional<List<Product>> getProductsByHtsCode(String htsCode) {
        return productRepository.findByHtsCodeStartingWith(htsCode);
    }

    public Optional<Product> getProductPriceAtTime(String htsCode, LocalDate date) {
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
                        (p1, p2) -> p1.getFetchDate().isAfter(p2.getFetchDate()) ? p1 : p2
                ))
                .values().stream()
                .collect(Collectors.toSet());
    }

    public String selectPrice(Product product, String country) {
        String special = product.getSpecial();
        String general = product.getGeneral();

        if (special != null && special.contains(country)) {
            return special.split(" ")[0];
        }

        return general != null ? general : "";
    }

    public Map<LocalDate, String> getHistoricalPrices(String htsCode, String country) {
        Optional<List<Product>> products = productRepository.findByHtsCode(htsCode);

        if (!products.isPresent()) {
            throw new HTSCodeNotFoundException("Error: Product with HTS Code " + htsCode + " not found!");
        }

        return products.get().stream().collect(
                Collectors.toMap(
                        Product::getFetchDate,
                        product -> selectPrice(product, country)));
    }

    public Map<LocalDate, String> getPrices(String htsCode, String country) {
        Map<LocalDate, String> historicalPrices = getHistoricalPrices(htsCode, country);
        Map<LocalDate, String> futurePrices = ftaService.getFuturePrices(country, htsCode);
        historicalPrices.putAll(futurePrices);
        return historicalPrices;
    }

    public Map<String, String> mapCountryToPrice(String htsCode) {
        Optional<Product> product = productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);
        String[] countries = Locale.getISOCountries();

        System.out.println(product);

        if (!product.isPresent() || product == null) {
            throw new HTSCodeNotFoundException("Product with HTS code " + htsCode + " not found!");
        }

        Map<String, String> pricesByCountry = new HashMap<>();

        for (String country : countries) {
            pricesByCountry.put(country, selectPrice(product.get(), country));
        }
        return pricesByCountry;
    }

    /**
     * Notifies all users who have favorited an HTS code about a price change
     * 
     * @param htsCode Target HTS code
     * @param oldPrice Previous general rate
     * @param newPrice New general rate
    */
    private void notifyUsersOfPriceChange(String htsCode, String oldPrice, String newPrice) {
        try {
            List<String> userEmails = favouritesService.getUserEmailsByHtsCode(htsCode);
            Double oldprice = Double.parseDouble(oldPrice);
            Double newprice = Double.parseDouble(newPrice); 

            if (userEmails.isEmpty()) {
                System.out.println("No users to notify for HTS code: " + htsCode);
                return;
            }
            
            System.out.println("Notifying " + userEmails.size() + " users about price change for " + htsCode);
            
            for (String email : userEmails) {
                try {
                    
                    emailService.sendNotificationEmail(email, htsCode, oldPrice, newPrice);
                } catch (Exception e) {
                    System.out.println("Failed to send email to " + email + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error notifying users for HTS code " + htsCode + ": " + e.getMessage());
        }
    }
}
