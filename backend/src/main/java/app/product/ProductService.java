package app.product;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import app.query.QueryService;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final QueryService queryService;

    public ProductService(ProductRepository productRepository, QueryService queryService) {
        this.productRepository = productRepository;
        this.queryService = queryService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDaily() {
        try {
            List<String> keywords = List.of("sugar", "bread", "milk", "egg", "rice");

            for (String keyword : keywords) {
                List<Map<String, Object>> response = queryService.searchTariffArticles(keyword);

                for (Map<String, Object> map : response) {
                    String htsCode = (String) map.get("hts_code");
                    double price = (double) map.get("general");

                    Product product = new Product();
                    product.setFetchDate(LocalDate.now());
                    product.setHtsCode(htsCode);
                    product.setPrice(price);

                    Optional<Product> latestRecord = productRepository.findTopByHtsCodeOrderByFetchDateDesc(htsCode);

                    if (latestRecord.isPresent() && latestRecord.get().equals(product)) {
                        continue;
                    }

                    productRepository.save(product);
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching API: " + e.getMessage());
        }
    }
}
