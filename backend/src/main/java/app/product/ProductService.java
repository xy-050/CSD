package app.product;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public Optional<List<Product>> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Optional<List<Product>> getProductsByHtsCode(String htsCode) {
        return productRepository.findByHtsCode(htsCode);
    }

    public Optional<Product> getProductPriceAtTime(String htsCode, LocalDate date) {
        return productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(htsCode, date);
    }
}
