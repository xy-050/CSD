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
import app.fta.FTAService;
import app.query.TariffApiClient;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private TariffApiClient apiClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FTAService ftaService;

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
    // --------------- testing fetchExternal() method --------------------
    // -------------------------------------------------------------------

    @Test
    void fetchDailyData_WhenNoExistingRecord_ShouldSaveData() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("htsno", "1704.90.35");
        map.put("description", "Brown sugar");
        map.put("general", "5.5¢/t");
        map.put("special", "Free (AU, SG)");

        when(apiClient.searchTariffArticles(anyString())).thenReturn(List.of(map));
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

        when(apiClient.searchTariffArticles(anyString())).thenReturn(List.of(map));
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

        when(apiClient.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchExternal();

        // Assert
        verify(productRepository, never()).save(any(Product.class));
    }

    // -------------------------------------------------------------------
    // ------------ testing findProductByHtsCode() method ----------------
    // -------------------------------------------------------------------
    @Test
    void findProductByHtsCode_WhenProductFoundInDatabase_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.of(existing));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.35", result.getHtsCode());
        assertEquals("Brown sugar", result.getDescription());
        verify(productRepository, times(1)).findTopByHtsCodeOrderByFetchDateDesc("1704.90.35");
        verify(productRepository, never()).findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString());
        verify(apiClient, never()).searchTariffArticles(anyString());
    }

    @Test
    void findProductByHtsCode_WhenNotInDatabaseButFoundViaCategory_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.of(List.of(existing)));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.35", result.getHtsCode());
        verify(productRepository, times(1)).findTopByHtsCodeOrderByFetchDateDesc("1704.90.35");
        verify(productRepository, times(1)).findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35");
        verify(apiClient, never()).searchTariffArticles(anyString());
    }

    @Test
    void findProductByHtsCode_WhenNotFoundLocallyButFoundInApi_ShouldReturnProduct() {
        // Arrange
        Map<String, Object> apiData = new HashMap<>();
        apiData.put("htsno", "1704.90.35");
        apiData.put("description", "Brown sugar");
        apiData.put("general", "5.5¢/t");
        apiData.put("special", "Free (AU, SG)");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.35", result.getHtsCode());
        assertEquals("Brown sugar", result.getDescription());
        verify(apiClient, times(1)).searchTariffArticles("1704.90.35");
    }

    @Test
    void findProductByHtsCode_WhenNotFoundThroughAnyStrategy_ShouldThrowException() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("9999.99.99"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("9999.99.99", "9999.99.99"))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("9999.99.99"))
                .thenReturn(List.of());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findProductByHtsCode("9999.99.99"));
        assertEquals("No product found with HTS code: 9999.99.99", exception.getMessage());
    }

    @Test
    void findProductByHtsCode_WhenNullHtsCode_ShouldHandleGracefully() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(null))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(null, null))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles(null))
                .thenReturn(List.of());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.findProductByHtsCode(null));
        assertEquals("No product found with HTS code: null", exception.getMessage());
    }

    // -------------------------------------------------------------------
    // ------------- testing searchByCategory() method -------------------
    // -------------------------------------------------------------------

    @Test
    void searchByCategory_WhenExactMatchFound_ShouldReturnExactMatch() {
        // Arrange
        Product exact = new Product("1704.90.35", LocalDate.now(), "Exact match", "5.5¢/t", "Free", "sugar");
        Product other = new Product("1704.90.36", LocalDate.now(), "Other", "6.0¢/t", "Free", "sugar");

        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.of(List.of(other, exact)));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.35", result.getHtsCode());
        assertEquals("Exact match", result.getDescription());
    }

    @Test
    void searchByCategory_WhenNoExactMatchButResultsExist_ShouldReturnFirstResult() {
        // Arrange
        Product first = new Product("1704.90.36", LocalDate.now(), "First", "5.5¢/t", "Free", "sugar");
        Product second = new Product("1704.90.37", LocalDate.now(), "Second", "6.0¢/t", "Free", "sugar");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.of(List.of(first, second)));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.36", result.getHtsCode());
        assertEquals("First", result.getDescription());
    }

    @Test
    void searchByCategory_WhenEmptyResults_ShouldReturnEmpty() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.of(List.of()));
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of());

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.findProductByHtsCode("1704.90.35"));
    }

    // -------------------------------------------------------------------
    // ----------- testing fetchFromExternalApi() method -----------------
    // -------------------------------------------------------------------

    @Test
    void fetchFromExternalApi_WhenExactMatchFoundInApi_ShouldReturnExactMatch() {
        // Arrange
        Map<String, Object> exact = Map.of("htsno", "1704.90.35", "description", "Exact");
        Map<String, Object> other = Map.of("htsno", "1704.90.36", "description", "Other");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(other, exact));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.35", result.getHtsCode());
        assertEquals("Exact", result.getDescription());
    }

    @Test
    void fetchFromExternalApi_WhenNoExactMatchInApi_ShouldReturnFirstResult() {
        // Arrange
        Map<String, Object> first = Map.of("htsno", "1704.90.36", "description", "First");
        Map<String, Object> second = Map.of("htsno", "1704.90.37", "description", "Second");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(first, second));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNotNull(result);
        assertEquals("1704.90.36", result.getHtsCode());
    }

    @Test
    void fetchFromExternalApi_WhenApiReturnsNull_ShouldThrowException() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(null);

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.findProductByHtsCode("1704.90.35"));
    }

    @Test
    void fetchFromExternalApi_WhenApiThrowsException_ShouldThrowProductNotFoundException() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith("1704.90.35", "1704.90.35"))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.findProductByHtsCode("1704.90.35"));
    }

    // -------------------------------------------------------------------
    // --------------- testing mapToProduct() method ---------------------
    // -------------------------------------------------------------------

    @Test
    void mapToProduct_WhenAllFieldsPresent_ShouldMapAllFields() {
        // Arrange
        Map<String, Object> apiData = Map.of(
                "htsno", "1704.90.35",
                "description", "Brown sugar",
                "general", "5.5¢/t",
                "special", "Free (AU, SG)");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString()))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertEquals("1704.90.35", result.getHtsCode());
        assertEquals("Brown sugar", result.getDescription());
        assertEquals("5.5¢/t", result.getGeneral());
        assertEquals("Free (AU, SG)", result.getSpecial());
    }

    @Test
    void mapToProduct_WhenMissingHtsnoField_ShouldUseFallback() {
        // Arrange
        Map<String, Object> apiData = Map.of(
                "description", "Brown sugar",
                "general", "5.5¢/t",
                "special", "Free (AU, SG)");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString()))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertEquals("1704.90.35", result.getHtsCode());
    }

    @Test
    void mapToProduct_WhenMissingDescriptionField_ShouldSetNull() {
        // Arrange
        Map<String, Object> apiData = Map.of(
                "htsno", "1704.90.35",
                "general", "5.5¢/t",
                "special", "Free (AU, SG)");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString()))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNull(result.getDescription());
    }

    @Test
    void mapToProduct_WhenMissingGeneralField_ShouldSetNull() {
        // Arrange
        Map<String, Object> apiData = Map.of(
                "htsno", "1704.90.35",
                "description", "Brown sugar",
                "special", "Free (AU, SG)");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString()))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNull(result.getGeneral());
    }

    @Test
    void mapToProduct_WhenMissingSpecialField_ShouldSetNull() {
        // Arrange
        Map<String, Object> apiData = Map.of(
                "htsno", "1704.90.35",
                "description", "Brown sugar",
                "general", "5.5¢/t");

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString()))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertNull(result.getSpecial());
    }

    @Test
    void mapToProduct_WhenAllFieldsMissing_ShouldUseFallbackAndNulls() {
        // Arrange
        Map<String, Object> apiData = new HashMap<>();

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString()))
                .thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCaseOrHtsCodeStartingWith(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(apiClient.searchTariffArticles("1704.90.35"))
                .thenReturn(List.of(apiData));

        // Act
        Product result = productService.findProductByHtsCode("1704.90.35");

        // Assert
        assertEquals("1704.90.35", result.getHtsCode());
        assertNull(result.getDescription());
        assertNull(result.getGeneral());
        assertNull(result.getSpecial());
    }

    // -------------------------------------------------------------------
    // ------------- testing getProductByHtsCode() method ----------------
    // -------------------------------------------------------------------

    @Test
    void getProductByHtsCode_WhenSingleProductFound_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findByHtsCode("1704.90.35"))
                .thenReturn(Optional.of(List.of(existing)));

        // Act
        Optional<Product> result = productService.getProductByHtsCode("1704.90.35");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1704.90.35", result.get().getHtsCode());
    }

    @Test
    void getProductByHtsCode_WhenMultipleProductsFound_ShouldReturnMostRecent() {
        // Arrange
        Product older = new Product("1704.90.35", LocalDate.of(2025, 4, 1), "Older", "5.5¢/t", "Free", "sugar");
        Product newer = new Product("1704.90.35", LocalDate.of(2025, 12, 1), "Newer", "6.0¢/t", "Free", "sugar");

        when(productRepository.findByHtsCode("1704.90.35"))
                .thenReturn(Optional.of(List.of(older, newer)));

        // Act
        Optional<Product> result = productService.getProductByHtsCode("1704.90.35");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2025, 12, 1), result.get().getFetchDate());
        assertEquals("Newer", result.get().getDescription());
    }

    @Test
    void getProductByHtsCode_WhenNoProductsFound_ShouldReturnEmpty() {
        // Arrange
        when(productRepository.findByHtsCode("9999.99.99"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductByHtsCode("9999.99.99");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getProductByHtsCode_WhenNullHtsCode_ShouldReturnEmpty() {
        // Arrange
        when(productRepository.findByHtsCode(null))
                .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductByHtsCode(null);

        // Assert
        assertFalse(result.isPresent());
    }

    // -------------------------------------------------------------------
    // ---------- testing getMostRecentProductPrice() method -------------
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
    // ------- testing getMostRecentProductPriceAtTime() method ----------
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
    // ----------------- testing getPrices() method ----------------------
    // -------------------------------------------------------------------

    @Test
    void getPrices_WhenBothHistoricalAndFuturePricesExist_ShouldCombineBoth() {
        // Arrange
        Map<LocalDate, String> futurePrices = Map.of(
                LocalDate.of(2026, 1, 1), "4.0¢/t",
                LocalDate.of(2026, 6, 1), "3.5¢/t");

        when(productRepository.findByHtsCode("1704.90.35"))
                .thenReturn(Optional.of(products));
        when(ftaService.getFuturePrices("SG", "1704.90.35"))
                .thenReturn(futurePrices);

        // Act
        Map<LocalDate, String> result = productService.getPrices("1704.90.35", "SG");

        // Assert
        assertEquals(4, result.size());
        assertEquals("Free", result.get(LocalDate.of(2025, 4, 1)));
        assertEquals("Free", result.get(LocalDate.of(2025, 12, 1)));
        assertEquals("4.0¢/t", result.get(LocalDate.of(2026, 1, 1)));
        assertEquals("3.5¢/t", result.get(LocalDate.of(2026, 6, 1)));
    }

    @Test
    void getPrices_WhenOnlyHistoricalPricesExist_ShouldReturnHistoricalOnly() {
        // Arrange
        when(productRepository.findByHtsCode("1704.90.35"))
                .thenReturn(Optional.of(products));
        when(ftaService.getFuturePrices("SG", "1704.90.35"))
                .thenReturn(new HashMap<>());

        // Act
        Map<LocalDate, String> result = productService.getPrices("1704.90.35", "SG");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey(LocalDate.of(2025, 4, 1)));
        assertTrue(result.containsKey(LocalDate.of(2025, 12, 1)));
    }

    @Test
    void getPrices_WhenOnlyFuturePricesExist_ShouldReturnFutureOnly() {
        // Arrange
        Map<LocalDate, String> futurePrices = Map.of(LocalDate.of(2026, 1, 1), "4.0¢/t");

        when(productRepository.findByHtsCode("1704.90.35"))
                .thenReturn(Optional.of(List.of()));
        when(ftaService.getFuturePrices("SG", "1704.90.35"))
                .thenReturn(futurePrices);

        // Act
        Map<LocalDate, String> result = productService.getPrices("1704.90.35", "SG");

        // Assert
        assertEquals(1, result.size());
        assertEquals("4.0¢/t", result.get(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void getPrices_WhenNoPricesFound_ShouldReturnEmptyMap() {
        // Arrange
        when(productRepository.findByHtsCode("9999.99.99"))
                .thenThrow(new ProductNotFoundException("Error: Product with HTS Code 9999.99.99 not found!"));

        // Act & Assert
        assertThrows(ProductNotFoundException.class,
                () -> productService.getPrices("9999.99.99", "SG"));
    }

    @Test
    void getPrices_WhenDateConflictsBetweenHistoricalAndFuture_ShouldOverwriteWithFuture() {
        // Arrange
        Map<LocalDate, String> futurePrices = Map.of(
                LocalDate.of(2025, 4, 1), "OVERWRITTEN");

        when(productRepository.findByHtsCode("1704.90.35"))
                .thenReturn(Optional.of(products));
        when(ftaService.getFuturePrices("SG", "1704.90.35"))
                .thenReturn(futurePrices);

        // Act
        Map<LocalDate, String> result = productService.getPrices("1704.90.35", "SG");

        // Assert
        assertEquals("OVERWRITTEN", result.get(LocalDate.of(2025, 4, 1)));
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
