package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import app.product.ProductService;

/**
 * This class runs fetchDaily() when the app is run. 
 * TODO: REMOVE WHEN MOVING TO MYSQL
 */
@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    private ProductService productService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        productService.fetchDaily();
    }
}
