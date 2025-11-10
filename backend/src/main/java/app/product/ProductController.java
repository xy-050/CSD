package app.product;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDate;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
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
        Set<Product> products = productService.getHighestLevelCategory(keyword);
        return ResponseEntity.ok(Map.of(
                "message", "Categories for keyword " + keyword + " fetched successfully",
                "categories", products));

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
        Set<Product> subcategories = productService.getNextLevelCategory(htsCode);
        return ResponseEntity.ok(Map.of(
                "message", "Subcategories for HTS " + htsCode + " fetched successfully",
                "categories", subcategories));

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
        Product product = productService.findProductByHtsCode(htsCode);

        return ResponseEntity.ok(Map.of(
                "message", "Product with HTS code " + htsCode + " found",
                "htsCode", product.getHtsCode(),
                "description",
                product.getDescription() != null ? product.getDescription() : "No description available",
                "general", product.getGeneral() != null ? product.getGeneral() : "",
                "special", product.getSpecial() != null ? product.getSpecial() : "",
                "category", product.getCategory() != null ? product.getCategory() : ""));
    }

    /**
     * Returns the specific and general prices of a specific product.
     * 
     * @param htsCode
     * @return
     */
    @GetMapping("/price/{htsCode}")
    public ResponseEntity<Map<String, Object>> getMostRecentProductPrice(@PathVariable String htsCode) {
        Optional<Product> latest = productService.getMostRecentProductPrice(htsCode);

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
    public ResponseEntity<Map<String, Object>> getMostRecentProductPriceAtSpecificTime(
            @PathVariable String htsCode,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {

        LocalDate date = LocalDate.of(year, month, day);
        Optional<Product> record = productService.getMostRecentProductPriceAtTime(htsCode, date);

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
        Map<LocalDate, String> prices = productService.getPrices(htsCode, country);
        return ResponseEntity.ok().body(prices);
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
        Map<String, String> result = productService.mapCountryToPrice(htsCode);
        return ResponseEntity.ok().body(result);
    }
}
