package app.product;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getHtsCodesByCategory(@PathVariable String category) {
        Optional<List<Product>> products = productService.getHtsCodes(category);
        if (products.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Existing data for category " + category + " found",
                    "products", products.get()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No existing data on category " + category,
                    "products", null));
        }
    }

    @GetMapping("/{htsCode}")
    public ResponseEntity<Map<String, Object>> getProductPrice(@PathVariable String htsCode) {
        Optional<Product> latest = productService.getProductPrice(htsCode);

        if (latest.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Existing data for product " + htsCode + " found",
                    "price", latest.get().getPrice()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No existing data on product with HTS code " + htsCode,
                    "price", null));
        }
    }

    @GetMapping("/{htsCode}/{year}/{month}/{day}")
    public ResponseEntity<Map<String, Object>> getProductPriceAtSpecificTime(
            @PathVariable String htsCode,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {

        LocalDate date = LocalDate.of(year, month, day);
        Optional<Product> record = productService.getProductPriceAtTime(htsCode, date);

        if (record.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Existing data for product " + htsCode + " found",
                    "price", record.get().getPrice()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No existing data on product with HTS code " + htsCode,
                    "price", null));
        }
    }
}
