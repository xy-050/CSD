package app;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import app.product.Product;
import app.product.ProductRepository;
import app.product.ProductService;

/**
 * This class runs fetchDaily() when the app is run.
 * TODO: REMOVE WHEN MOVING TO MYSQL
 */
@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Product> products = List.of(
                new Product(
                        "0407.11.00.00",
                        LocalDate.of(2023, 1, 1),
                        "Of fowls of the species <i>Gallus domesticus</i>",
                        "2.0¢/doz",
                        "Free (AU, SG)",
                        "Egg"),
                new Product(
                        "0407.11.00.00",
                        LocalDate.of(2024, 1, 1),
                        "Of fowls of the species <i>Gallus domesticus</i>",
                        "2.4¢/doz",
                        "Free (AU, SG)",
                        "Egg"),
                new Product(
                        "0407.11.00.00",
                        LocalDate.of(2024, 1, 1),
                        "Of fowls of the species <i>Gallus domesticus</i>",
                        "2.6¢/doz",
                        "Free (AU, SG,  NZ)",
                        "Egg"));

        for (Product product : products) {
            productRepository.save(product);
        }

        productService.fetchDaily();
    }
}
