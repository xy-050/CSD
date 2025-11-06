package app.product;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import app.exception.HTSCodeNotFoundException;
import app.query.CountryCodes;
import app.query.QueryService;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final QueryService queryService;

    ObjectMapper objectMapper = new ObjectMapper();

    public ProductService(ProductRepository productRepository, QueryService queryService) {
        this.productRepository = productRepository;
        this.queryService = queryService;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void fetchDaily() {
        try {
            List<String> keywords = List.of("sugar", "bread", "milk", "egg", "rice");

            for (String keyword : keywords) {
                List<Map<String, Object>> response = queryService.searchTariffArticles(keyword);

                for (Map<String, Object> map : response) {
                    // System.out.println("Map:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));

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

    public Optional<Product> getProductPrice(String htsCode) {
        return productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);
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
                product -> selectPrice(product, CountryCodes.isoCountryMap.get(country))
            )
        );
    }

    public Map<String, String> mapCountryToPrice(String htsCode) {
        Optional<Product> product = productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);
        Set<String> countries = CountryCodes.isoCountryMap.keySet();

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
}
