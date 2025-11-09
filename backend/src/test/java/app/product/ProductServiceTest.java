package app.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import app.exception.ProductNotFoundException;
import app.query.QueryService;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private QueryService queryService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product existing;

    private List<Product> products;

    @BeforeEach
    void setUp() {
        existing = new Product(
                "1704.90.35",
                LocalDate.of(2025, Month.APRIL, 1),
                "Brown sugar",
                "5.5¢/t",
                "Free (AU, SG)",
                "sugar");

        products = List.of(existing, new Product(
                "1704.90.35",
                LocalDate.of(2025, Month.DECEMBER, 1),
                "Brown sugar",
                "5.5¢/t",
                "Free (AU, SG, NZ)",
                "sugar"));
    }

    // -------------------------------------------------------------------
    // --------------- testing fetchDaily() method -----------------------
    // -------------------------------------------------------------------

    @Test
    void fetchDailyData_WhenNoExistingRecord_ShouldSaveData() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("htsno", "1704.90.35");
        map.put("description", "Brown sugar");
        map.put("general", "5.5¢/t");
        map.put("special", "Free (AU, SG)");

        when(queryService.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.empty());

        // Act
        productService.fetchExternal();

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals("Brown sugar", saved.getDescription());
            assertEquals("5.5¢/t", saved.getGeneral());
            assertEquals("Free (AU, SG)", saved.getSpecial());
            assertNotNull(saved.getFetchDate());
            assertNotNull(saved.getCategory());
        }
    }

    @Test
    void fetchDailyData_WhenRecordUpdated_ShouldSaveData() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("htsno", "1704.90.35");
        map.put("description", "Brown sugar - Updated");
        map.put("general", "6.0¢/t");
        map.put("special", "Free (AU, SG, NZ)");

        when(queryService.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchExternal();

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals("Brown sugar - Updated", saved.getDescription());
            assertEquals("6.0¢/t", saved.getGeneral());
            assertEquals("Free (AU, SG, NZ)", saved.getSpecial());
        }
    }

    @Test
    void fetchDailyData_WhenRecordIsSame_ShouldNotSaveData() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("htsno", "1704.90.35");
        map.put("description", "Brown sugar");
        map.put("general", "5.5¢/t");
        map.put("special", "Free (AU, SG)");

        when(queryService.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchExternal();

        // Assert
        verify(productRepository, never()).save(any(Product.class));
    }

    // -------------------------------------------------------------------
    // ------------- testing getMostRecentProductPrice() method --------------------
    // -------------------------------------------------------------------

    @Test
    void getMostRecentProductPrice_WhenRecordExists_ShouldReturnValue() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.of(existing));

        // Act
        Optional<Product> result = productService.getMostRecentProductPrice("1704.90.35");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1704.90.35", result.get().getHtsCode());
        assertEquals("Brown sugar", result.get().getDescription());
        assertEquals("5.5¢/t", result.get().getGeneral());
        assertEquals("Free (AU, SG)", result.get().getSpecial());
        assertEquals("sugar", result.get().getCategory());
        verify(productRepository, times(1)).findTopByHtsCodeOrderByFetchDateDesc("1704.90.35");
    }

    @Test
    void getMostRecentProductPrice_WhenRecordDoesNotExists_ShouldNotReturnValue() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("9999.99.99"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getMostRecentProductPrice("9999.99.99");

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findTopByHtsCodeOrderByFetchDateDesc("9999.99.99");
    }

    // -------------------------------------------------------------------
    // ---------- testing getMostRecentProductPriceAtTime() method -----------------
    // -------------------------------------------------------------------

    @Test
    void getMostRecentProductPriceAtTime_WhenRecordExists_ShouldReturnValue() {
        // Arrange
        LocalDate queryDate = LocalDate.of(2025, Month.MAY, 1);
        when(productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(
                "1704.90.35", queryDate)).thenReturn(Optional.of(existing));

        // Act
        Optional<Product> result = productService.getMostRecentProductPriceAtTime("1704.90.35", queryDate);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1704.90.35", result.get().getHtsCode());
        assertEquals("Brown sugar", result.get().getDescription());
        assertEquals("5.5¢/t", result.get().getGeneral());
        verify(productRepository, times(1))
                .findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc("1704.90.35", queryDate);
    }

    @Test
    void getMostRecentProductPriceAtTime_WhenRecordDoesNotExists_ShouldNotReturnValue() {
        // Arrange
        LocalDate queryDate = LocalDate.of(2025, Month.JANUARY, 1);
        when(productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(
                "1704.90.35", queryDate)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getMostRecentProductPriceAtTime("1704.90.35", queryDate);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1))
                .findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc("1704.90.35", queryDate);
    }

    // @Test
    // void getMostRecentProductPrice_WhenRecordExistsByCategory_ShouldReturnValue() {
    // // Arrange
    // Product product2 = new Product(
    // "1704.90.40",
    // LocalDate.of(2025, Month.APRIL, 1),
    // "White sugar",
    // "4.5¢/t",
    // "Free (CA)",
    // "sugar");

    // List<Product> products = List.of(existing, product2);
    // when(productRepository.findByCategory("sugar")).thenReturn(Optional.of(products));

    // // Act
    // Optional<List<Product>> result =
    // productService.getProductsByCategory("sugar");

    // // Assert
    // assertTrue(result.isPresent());
    // assertEquals(2, result.get().size());
    // assertEquals("1704.90.35", result.get().get(0).getHtsCode());
    // assertEquals("1704.90.40", result.get().get(1).getHtsCode());
    // assertEquals("sugar", result.get().get(0).getCategory());
    // assertEquals("sugar", result.get().get(1).getCategory());
    // verify(productRepository, times(1)).findByCategory("sugar");
    // }

    // -------------------------------------------------------------------
    // ---------------- testing selectPrice() method ---------------------
    // -------------------------------------------------------------------

    @Test
    void selectPrice_WhenCountryInSpecial_ShouldReturnSpeicalPrice() {
        // Act
        String result = productService.selectPrice(existing, "SG");

        // Assert
        assertEquals(result, "Free");
    }

    @Test
    void selectPrice_WhenCountryNotInSpecial_ShouldReturnGeneralPrice() {
        // Act
        String result = productService.selectPrice(existing, "NZ");

        // Assert
        assertEquals(result, "5.5¢/t");
    }

    // -------------------------------------------------------------------
    // ------------ testing getHistoricalPrices() method -----------------
    // -------------------------------------------------------------------

    @Test
    void getHistoricalPrices_WhenAllSpecial_ShouldReturnAllSpecial() {
        // Arrange
        when(productRepository.findByHtsCode(anyString())).thenReturn(Optional.of(products));

        // Act
        Map<LocalDate, String> result = productService.getHistoricalPrices("1704.90.35", "SG");

        // Assert
        assertEquals(result, Map.of(
                LocalDate.of(2025, Month.APRIL, 1), "Free",
                LocalDate.of(2025, Month.DECEMBER, 1), "Free"));
    }

    @Test
    void getHistoricalPrices_WhenAllGeneral_ShouldReturnAllGeneral() {
        // Arrange
        when(productRepository.findByHtsCode(anyString())).thenReturn(Optional.of(products));

        // Act
        Map<LocalDate, String> result = productService.getHistoricalPrices("1704.90.35", "AF");

        // Assert
        assertEquals(result, Map.of(
                LocalDate.of(2025, Month.APRIL, 1), "5.5¢/t",
                LocalDate.of(2025, Month.DECEMBER, 1), "5.5¢/t"));
    }

    @Test
    void getHistoricalPrices_WhenMixed_ShouldReturnMixed() {
        // Arrange
        when(productRepository.findByHtsCode(anyString())).thenReturn(Optional.of(products));

        // Act
        Map<LocalDate, String> result = productService.getHistoricalPrices("1704.90.35", "NZ");

        // Assert
        assertEquals(result, Map.of(
                LocalDate.of(2025, Month.APRIL, 1), "5.5¢/t",
                LocalDate.of(2025, Month.DECEMBER, 1), "Free"));
    }

    // -------------------------------------------------------------------
    // ------------ testing getMapCountryToPrice() method ----------------
    // -------------------------------------------------------------------
    @Test
    void mapCountryToPrice_WhenHTSCodeExists_ShouldReturnMap() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        Map<String, String> result = productService.mapCountryToPrice("1704.90.35");

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("AU"));
        assertEquals(result.get("AU"), "Free");
        assertTrue(result.containsKey("AF"));
        assertEquals(result.get("AF"), "5.5¢/t");
    }

    @Test
    void mapCountryToPrice_WhenHTSCodeDoesNotExist_ShouldThrowException() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.mapCountryToPrice("67"));
        assertEquals(exception.getMessage(), "Product with HTS code 67 not found!");
    }

    @Test
    void getNextLevelCategory_WhenSubcategoriesExists_ShouldReturnSubcategories() {
        // Arrange
        Product product2 = new Product(
                "1704.01",
                LocalDate.of(2025, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        Product product3 = new Product(
                "1704.90",
                LocalDate.of(2025, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        List<Product> products = List.of(existing, product2, product3);
        when(productRepository.findByHtsCodeStartingWith(anyString())).thenReturn(Optional.of(products));

        // Act
        Set<Product> actualResults = productService.getNextLevelCategory("1704");

        // Assert
        assertEquals(Set.of(existing, product2, product3), actualResults);
    }

    @Test
    void getNextLevelCategory_WhenNoSubcategories_ShouldThrowProductNotFoundException() {
        // Arrange
        when(productRepository.findByHtsCodeStartingWith(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.getNextLevelCategory("1704.90.35");
        });
        assertEquals(exception.getMessage(), "Error: Product with HTS code 1704.90.35.* not found!");
    }

    @Test
    void getNextLevelCategory_WhenSubcategoriesExistsAndMultipleInstancesInDB_ShouldReturnMostRecent() {
        // Arrange
        Product product2 = new Product(
                "1704.01",
                LocalDate.of(2025, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        Product product3 = new Product(
                "1704.01",
                LocalDate.of(2027, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        List<Product> products = List.of(existing, product2, product3);
        when(productRepository.findByHtsCodeStartingWith(anyString())).thenReturn(Optional.of(products));
        
        // Act
        Set<Product> actualResults = productService.getNextLevelCategory("1704");

        // Assert
        assertEquals(Set.of(existing, product3), actualResults);
    }

    @Test
    void getNextLevelCategory_WhenHtsInvalid_ShouldThrowProductNotFoundException() {
        // Arrange
        when(productRepository.findByHtsCodeStartingWith(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.getNextLevelCategory("does not exist");
        });
        assertEquals(exception.getMessage(), "Error: Product with HTS code does not exist.* not found!");
    }

    @Test
    void getHighestLevelCategory_WhenKeywordExists_ShouldReturnSetOfSuperProducts() {
        // Arrange
        Product product1 = new Product(
                "1704",
                LocalDate.of(2025, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        Product product2 = new Product(
                "1704.01",
                LocalDate.of(2025, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        when(productRepository.findByCategoryContainingIgnoreCase(anyString()))
                .thenReturn(Optional.of(List.of(product1, product2)));

        // Act
        Set<Product> products = productService.getHighestLevelCategory("Sugar");

        // Assert
        assertEquals(Set.of(product1), products);
    }

    @Test
    void getHighestLevelCategory_WhenKeywordDoesNotExist_ShouldThrowProductNotFoundException() {
        // Arrange
        when(productRepository.findByCategoryContainingIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, () -> {
            productService.getHighestLevelCategory("Sugar");
        });
        assertEquals("Error: Product with keyword Sugar cannot be found!", exception.getMessage());
    }

    @Test
    void getHighestLevelCategory_WhenKeywordExistsButNoSuper_ShouldReturnEmptySet() {
        // Arrange
        Product product2 = new Product(
                "1704.01",
                LocalDate.of(2025, Month.APRIL, 1),
                "",
                "",
                "",
                "sugar");
        when(productRepository.findByCategoryContainingIgnoreCase(anyString()))
                .thenReturn(Optional.of(List.of(existing, product2)));

        // Act
        Set<Product> products = productService.getHighestLevelCategory("Sugar");

        // Assert
        assertEquals(new HashSet<>(), products);
    }
}
