package app.product;

import org.springframework.web.bind.annotation.*;

import app.exception.HTSCodeNotFoundException;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.time.LocalDate;

import app.query.QueryService;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final QueryService queryService;

    public ProductController(ProductService productService, QueryService queryService) {
        this.productService = productService;
        this.queryService = queryService;
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
                        if (parts.length <= 1)
                            return p;

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

    @GetMapping("/hts/{htsCode}")
    public ResponseEntity<Map<String, Object>> getProductByHtsCode(@PathVariable String htsCode) {
        System.out.println("Fetching product by HTS code: " + htsCode);
        
        // First try to get from local database
        Optional<Product> product = productService.getProductPrice(htsCode);

        if (product.isPresent()) {
            Product p = product.get();
            System.out.println("Product found in database: " + p.toString());
            return ResponseEntity.ok(Map.of(
                    "message", "Product with HTS code " + htsCode + " found",
                    "htsCode", p.getHtsCode(),
                    "description", p.getDescription() != null ? p.getDescription() : "No description available",
                    "general", p.getGeneral() != null ? p.getGeneral() : "",
                    "special", p.getSpecial() != null ? p.getSpecial() : "",
                    "category", p.getCategory() != null ? p.getCategory() : ""));
        } else {
            // If not in database, try searching by category/keyword
            System.out.println("Product not in database, searching by keyword: " + htsCode);
            Optional<List<Product>> searchResults = productService.getProductsByCategory(htsCode);
            
            if (searchResults.isPresent() && !searchResults.get().isEmpty()) {
                // Find exact match or use first result
                Product p = searchResults.get().stream()
                    .filter(prod -> prod.getHtsCode().equals(htsCode))
                    .findFirst()
                    .orElse(searchResults.get().get(0));
                
                System.out.println("Product found by search: " + p.toString());
                return ResponseEntity.ok(Map.of(
                        "message", "Product with HTS code " + htsCode + " found",
                        "htsCode", p.getHtsCode(),
                        "description", p.getDescription() != null ? p.getDescription() : "",
                        "general", p.getGeneral() != null ? p.getGeneral() : "",
                        "special", p.getSpecial() != null ? p.getSpecial() : "",
                        "category", p.getCategory() != null ? p.getCategory() : ""));
            }
            
            // If still not found, fetch from external API
            System.out.println("Product not found locally, fetching from external API: " + htsCode);
            try {
                List<Map<String, Object>> apiResults = queryService.searchTariffArticles(htsCode);
                if (apiResults != null && !apiResults.isEmpty()) {
                    // Find exact match or use first result
                    Map<String, Object> match = apiResults.stream()
                        .filter(item -> htsCode.equals(item.get("htsno")))
                        .findFirst()
                        .orElse(apiResults.get(0));
                    
                    System.out.println("Product found from API: " + match.get("htsno"));
                    return ResponseEntity.ok(Map.of(
                            "message", "Product with HTS code " + htsCode + " found from API",
                            "htsCode", match.get("htsno") != null ? match.get("htsno") : htsCode,
                            "description", match.get("description") != null ? match.get("description") : "No description available",
                            "general", match.get("general") != null ? match.get("general") : "",
                            "special", match.get("special") != null ? match.get("special") : "",
                            "category", ""));
                }
            } catch (Exception e) {
                System.out.println("Error fetching from external API: " + e.getMessage());
            }
            
            System.out.println("No product found with HTS code: " + htsCode);
            return ResponseEntity.status(404).body(Map.of(
                    "message", "No product found with HTS code " + htsCode));
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

    @GetMapping("/price/{htsCode}/{country}")
    public ResponseEntity<?> getPrices(@PathVariable String htsCode, @PathVariable String country) {
        try {
            Map<LocalDate, String> prices = productService.getPrices(htsCode, country);
            return ResponseEntity.ok().body(prices);
        } catch (HTSCodeNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/price/map/{htsCode}")
    public ResponseEntity<?> getMapCountryToPrice(@PathVariable String htsCode) {
        try {
            Map<String, String> result = productService.mapCountryToPrice(htsCode);
            return ResponseEntity.ok().body(result);
        } catch (HTSCodeNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
