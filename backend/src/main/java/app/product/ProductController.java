package app.product;

import org.springframework.web.bind.annotation.*;

import app.exception.HTSCodeNotFoundException;
import app.exception.ProductNotFoundException;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.Set;
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

    /**
     * Given a keyword, return all products in the supercategory (i.e. the most
     * general). For instance, "1704" will be returned, but not "1704.01" or
     * "1704.00.01".
     * 
     * @param keyword Keyword (e.g. "Sugar", "Egg")
     * @return TreeSet containing the products in the supercategory
     */
    @GetMapping("/category/search/{keyword}")
    public ResponseEntity<Map<String, Object>> searchCategories(@PathVariable String keyword) {
        try {
            Set<Product> products = productService.getHighestLevelCategory(keyword);
            return ResponseEntity.ok(Map.of(
                    "message", "Categories for keyword " + keyword + " fetched successfully",
                    "categories", products));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Returns the next level of subcategories of a product. For instance, if "1704"
     * is passed in, then "1704.01" is returned, and if "1701.01" is passed in,
     * "1701.00.01" is returned.
     * 
     * @param htsCode Super HTS code
     * @return Sub HTS code
     */
    @GetMapping("/category/{htsCode}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String htsCode) {
        try {
            Set<Product> subcategories = productService.getNextLevelCategory(htsCode);
            return ResponseEntity.ok(Map.of(
                    "message", "Subcategories for HTS " + htsCode + " fetched successfully",
                    "categories", subcategories));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Returns the details of a product given a HTS code. Tries to search database
     * first, but if fails, will call external API.
     * 
     * @param htsCode Target HTS code
     * @return Product details
     */
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
                            "description",
                            match.get("description") != null ? match.get("description") : "No description available",
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

    /**
     * Returns the specific and general prices of a specific product.
     * 
     * @param htsCode
     * @return
     */
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

    /**
     * Returns the price of a specific product at a specific time.
     * 
     * @param htsCode
     * @param year
     * @param month
     * @param day
     * @return
     */
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

    /**
     * Returns historical and future prices of a specific product for a specific
     * country. Can be used to analyse how prices change over time.
     * 
     * @param htsCode HTS code of target product
     * @param country Target country
     * @return Map that maps the date to the price (example: { "2023-1-1": "$2.10",
     *         "2025-01-01", "$2.30" })
     */
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

    /**
     * Maps a country to the price of a specific product. Can be used to compare the
     * most recent prices across countries.
     * 
     * @param htsCode HTS code of target product
     * @return Map (example: { "AF": "$2.30", "AU": "Free", ... })
     */
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
