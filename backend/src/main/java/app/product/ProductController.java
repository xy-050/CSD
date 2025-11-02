package app.product;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.time.LocalDate;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/category/search/{keyword}")
    public ResponseEntity<Map<String, Object>> searchCategories(@PathVariable String keyword) {
        Optional<List<Product>> products = productService.getProductsByCategory(keyword);

        if (products.isPresent()) {
            Set<String> categories = products.get().stream()
                .map(p -> {
                    String code = p.getHtsCode();
                    int idx = code.indexOf('.');
                    return idx == -1 ? code : code.substring(0, idx);
                })
                .collect(Collectors.toCollection(TreeSet::new)); // deduplicate & sort

            return ResponseEntity.ok(Map.of(
                    "message", "Categories for keyword " + keyword + " fetched successfully",
                    "categories", categories));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "Failed to fetch categories for keyword " + keyword));
        }
    }

    @GetMapping("/category/{htsCode}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String htsCode) {
        Optional<List<Product>> products = productService.getProductsByCategory(htsCode);

        if (products.isPresent()) {
            Set<String> subcodes = products.get().stream()
                .map(Product::getHtsCode)
                .filter(code -> code.equals(htsCode) || code.startsWith(htsCode + ".") || code.startsWith(htsCode))
                .map(code -> {
                    int firstDot = code.indexOf('.');
                    if (firstDot == -1) return code;
                    int secondDot = code.indexOf('.', firstDot + 1);
                    return secondDot == -1 ? code : code.substring(0, secondDot);
                })
                .collect(Collectors.toCollection(TreeSet::new)); // deduplicate & sort

            return ResponseEntity.ok(Map.of(
                    "message", "Subcategories for HTS " + htsCode + " fetched successfully",
                    "categories", subcodes));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "Failed to fetch subcategories for HTS " + htsCode));
        }
    }

    @GetMapping("/price/{htsCode}")
    public ResponseEntity<Map<String, Object>> getProductPrice(@PathVariable String htsCode) {
        Optional<Product> latest = productService.getProductPrice(htsCode);

        if (latest.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Existing data for product " + htsCode + " found",
                    "general", latest.get().getGeneral(),
                    "special", latest.get().getSpecial()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No existing data on product with HTS code " + htsCode,
                    "general", null, 
                    "special", null));
        }
    }

    @GetMapping("/price/{htsCode}/{year}/{month}/{day}")
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
                    "general", record.get().getGeneral(),
                    "special", record.get().getSpecial()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", "No existing data on product with HTS code " + htsCode,
                    "general", null, 
                    "special", null));
        }
    }
}
