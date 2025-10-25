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

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private QueryService queryService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks 
    private ProductService productService;

    private Product existing;
    private Product response;

    @BeforeEach
    void setUp() {
        // Initialize products used across tests
        existing = new Product();
        existing.setHtsCode("1704.90.35");
        existing.setFetchDate(LocalDate.of(2025, Month.APRIL, 1));
        existing.setPrice(5.6);

        response = new Product();
        response.setHtsCode("1704.90.35");
        response.setFetchDate(LocalDate.now());
        response.setPrice(5.6);
    }

    @Test
    void fetchDailyData_WhenNoExistingRecord_ShouldSaveData() {
        // Arrange: API returns one entry for any keyword
        Map<String, Object> map = new HashMap<>();
        map.put("hts_code", "1704.90.35");
        map.put("general", 5.6);

        when(queryService.searchTariffArticles(any())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.empty());

        // Act
        productService.fetchDaily();

        // Assert: there are 5 keywords in the service list -> save called 5 times
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        // Verify captured products have expected fields
        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals(5.6, saved.getPrice());
            assertNotNull(saved.getFetchDate());
        }
    }

    @Test
    void fetchDailyData_WhenRecordUpdated_ShouldSaveData() {
        // Arrange: API returns same hts code but different price than existing
        Map<String, Object> map = new HashMap<>();
        map.put("hts_code", "1704.90.35");
        map.put("general", 6.0);

        when(queryService.searchTariffArticles(any())).thenReturn(List.of(map));

        // existing has price 5.6 (set in setUp)
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchDaily();

        // Assert: since price changed, save should be called for each keyword
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals(6.0, saved.getPrice());
        }
    }

    @Test
    void fetchDailyData_WhenRecordIsSame_ShouldNotSaveData() {
        // Arrange: API returns same hts code and same price as existing
        Map<String, Object> map = new HashMap<>();
        map.put("hts_code", "1704.90.35");
        map.put("general", 5.6);

        when(queryService.searchTariffArticles(any())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        // Act
        productService.fetchDaily();

        // Assert: because latest record equals the new product (htsCode and price), save should not be called
        verify(productRepository, never()).save(any(Product.class));
    }
}
