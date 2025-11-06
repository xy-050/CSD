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
        System.out.println("Searching for keyword: " + keyword);
        Optional<List<Product>> products = productService.getProductsByCategory(keyword);

        if (products.isPresent()) {
            List<Product> productList = products.get();
            System.out.println("Found " + productList.size() + " products");
            
            // Get only the top-level categories (no dots in HTS code)
            Set<Product> categories = productList.stream()
                .peek(p -> System.out.println("Found product: " + p.toString()))
                .filter(p -> !p.getHtsCode().contains(".")) // Only include products with no dots in HTS code
                .collect(Collectors.toCollection(TreeSet::new)); // Use natural ordering from Product

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
        Optional<List<Product>> products = productService.getProductsByHtsCode(htsCode);

        if (products.isPresent()) {
            // Get unique products for the next level of categorization
            Set<Product> subcategories = products.get().stream()
                .filter(p -> {
                    String code = p.getHtsCode();
                    return code.equals(htsCode) || code.startsWith(htsCode + ".") || code.startsWith(htsCode);
                })
                .map(p -> {
                    String code = p.getHtsCode();
                    String[] parts = code.split("\\.");
                    // If this is already a leaf node or has no further subcategories, return as is
                    if (parts.length <= 1) return p;
                    
                    // For parent categories, get the next level down
                    String nextLevel = parts[0] + (parts.length > 1 ? "." + parts[1] : "");
                    return products.get().stream()
                        .filter(sub -> sub.getHtsCode().equals(nextLevel))
                        .findFirst()
                        .orElse(p);
                })
                .collect(Collectors.toCollection(TreeSet::new)); // Use natural ordering from Product

            return ResponseEntity.ok(Map.of(
                    "message", "Subcategories for HTS " + htsCode + " fetched successfully",
                    "categories", subcategories));
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
