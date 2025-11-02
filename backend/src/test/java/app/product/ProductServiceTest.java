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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import app.query.QueryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private QueryService queryService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductService productService;

    private Product existing;

    @BeforeEach
    void setUp() {
        existing = new Product(
                "1704.90.35",
                LocalDate.of(2025, Month.APRIL, 1),
                "Brown sugar",
                "5.5¢/t",
                "(AU, SG)",
                "sugar");
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
        map.put("special", "(AU, SG)");

        when(queryService.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.empty());

        // Act
        productService.fetchDaily();

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals("Brown sugar", saved.getDescription());
            assertEquals("5.5¢/t", saved.getGeneral());
            assertEquals("(AU, SG)", saved.getSpecial());
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
        map.put("special", "(AU, SG, NZ)");

        when(queryService.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchDaily();

        // Assert
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals("Brown sugar - Updated", saved.getDescription());
            assertEquals("6.0¢/t", saved.getGeneral());
            assertEquals("(AU, SG, NZ)", saved.getSpecial());
        }
    }

    @Test
    void fetchDailyData_WhenRecordIsSame_ShouldNotSaveData() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("htsno", "1704.90.35");
        map.put("description", "Brown sugar");
        map.put("general", "5.5¢/t");
        map.put("special", "(AU, SG)");

        when(queryService.searchTariffArticles(anyString())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchDaily();

        // Assert
        verify(productRepository, never()).save(any(Product.class));
    }

    // -------------------------------------------------------------------
    // ------------- testing getProductPrice() method --------------------
    // -------------------------------------------------------------------

    @Test
    void getProductPrice_WhenRecordExists_ShouldReturnValue() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("1704.90.35"))
                .thenReturn(Optional.of(existing));

        // Act
        Optional<Product> result = productService.getProductPrice("1704.90.35");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1704.90.35", result.get().getHtsCode());
        assertEquals("Brown sugar", result.get().getDescription());
        assertEquals("5.5¢/t", result.get().getGeneral());
        assertEquals("(AU, SG)", result.get().getSpecial());
        assertEquals("sugar", result.get().getCategory());
        verify(productRepository, times(1)).findTopByHtsCodeOrderByFetchDateDesc("1704.90.35");
    }

    @Test
    void getProductPrice_WhenRecordDoesNotExists_ShouldNotReturnValue() {
        // Arrange
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc("9999.99.99"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductPrice("9999.99.99");

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findTopByHtsCodeOrderByFetchDateDesc("9999.99.99");
    }

    // -------------------------------------------------------------------
    // ---------- testing getProductPriceAtTime() method -----------------
    // -------------------------------------------------------------------

    @Test
    void getProductPriceAtTime_WhenRecordExists_ShouldReturnValue() {
        // Arrange
        LocalDate queryDate = LocalDate.of(2025, Month.MAY, 1);
        when(productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(
                "1704.90.35", queryDate)).thenReturn(Optional.of(existing));

        // Act
        Optional<Product> result = productService.getProductPriceAtTime("1704.90.35", queryDate);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1704.90.35", result.get().getHtsCode());
        assertEquals("Brown sugar", result.get().getDescription());
        assertEquals("5.5¢/t", result.get().getGeneral());
        verify(productRepository, times(1))
                .findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc("1704.90.35", queryDate);
    }

    @Test
    void getProductPriceAtTime_WhenRecordDoesNotExists_ShouldNotReturnValue() {
        // Arrange
        LocalDate queryDate = LocalDate.of(2025, Month.JANUARY, 1);
        when(productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(
                "1704.90.35", queryDate)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductPriceAtTime("1704.90.35", queryDate);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository, times(1))
                .findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc("1704.90.35", queryDate);
    }

    @Test
    void getProductPrice_WhenRecordExistsByCategory_ShouldReturnValue() {
        // Arrange
        Product product2 = new Product(
                "1704.90.40",
                LocalDate.of(2025, Month.APRIL, 1),
                "White sugar",
                "4.5¢/t",
                "(CA)",
                "sugar");

        List<Product> products = List.of(existing, product2);
        when(productRepository.findByCategory("sugar")).thenReturn(Optional.of(products));

        // Act
        Optional<List<Product>> result = productService.getProductsByCategory("sugar");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("1704.90.35", result.get().get(0).getHtsCode());
        assertEquals("1704.90.40", result.get().get(1).getHtsCode());
        assertEquals("sugar", result.get().get(0).getCategory());
        assertEquals("sugar", result.get().get(1).getCategory());
        verify(productRepository, times(1)).findByCategory("sugar");
    }
}
