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
        existing = new Product();
        existing.setHtsCode("1704.90.35");
        existing.setFetchDate(LocalDate.of(2025, Month.APRIL, 1));
        existing.setPrice(5.6);

        response = new Product();
        response.setHtsCode("1704.90.35");
        response.setFetchDate(LocalDate.now());
        response.setPrice(5.6);

        response = new Product();
        response.setHtsCode("1704.90.35");
        response.setFetchDate(LocalDate.now());
        response.setPrice(6.0);
        response.setCategory("sugar");
        response.setDescription("Brown sugar");
    }

    // -------------------------------------------------------------------
    // --------------- testing fetchDaily() method -----------------------
    // -------------------------------------------------------------------

    @Test
    void fetchDailyData_WhenNoExistingRecord_ShouldSaveData() {
        Map<String, Object> map = new HashMap<>();
        map.put("hts_code", "1704.90.35");
        map.put("general", 5.6);

        when(queryService.searchTariffArticles(any())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.empty());

        productService.fetchDaily();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals(5.6, saved.getPrice());
            assertNotNull(saved.getFetchDate());
        }
    }

    @Test
    void fetchDailyData_WhenRecordUpdated_ShouldSaveData() {
        Map<String, Object> map = new HashMap<>();
        map.put("hts_code", "1704.90.35");
        map.put("general", 6.0);

        when(queryService.searchTariffArticles(any())).thenReturn(List.of(map));

        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        productService.fetchDaily();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(5)).save(captor.capture());

        for (Product saved : captor.getAllValues()) {
            assertEquals("1704.90.35", saved.getHtsCode());
            assertEquals(6.0, saved.getPrice());
        }
    }

    @Test
    void fetchDailyData_WhenRecordIsSame_ShouldNotSaveData() {
        Map<String, Object> map = new HashMap<>();
        map.put("hts_code", "1704.90.35");
        map.put("general", 5.6);

        when(queryService.searchTariffArticles(any())).thenReturn(List.of(map));
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        productService.fetchDaily();

        verify(productRepository, never()).save(any(Product.class));
    }

    // -------------------------------------------------------------------
    // ------------- testing getProductPrice() method --------------------
    // -------------------------------------------------------------------
    @Test
    void getProductPrice_WhenRecordExists_ShouldReturnValue() {
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.of(existing));

        Optional<Product> product = productService.getProductPrice("1704.90.35");

        assertTrue(product.isPresent());
        assertEquals(product.get().getPrice(), existing.getPrice());
    }

    @Test
    void getProductPrice_WhenRecordDoesNotExists_ShouldNotReturnValue() {
        when(productRepository.findTopByHtsCodeOrderByFetchDateDesc(anyString())).thenReturn(Optional.empty());

        Optional<Product> product = productService.getProductPrice("1704.90.35");

        assertFalse(product.isPresent());
    }

    // -------------------------------------------------------------------
    // ---------- testing getProductPriceAtTime() method -----------------
    // -------------------------------------------------------------------
    @Test
    void getProductPriceAtTime_WhenRecordExists_ShouldReturnValue() {
        when(productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(anyString(),
                any(LocalDate.class))).thenReturn(Optional.of(existing));

        Optional<Product> product = productService.getProductPriceAtTime("1704.90.35", LocalDate.now());

        assertTrue(product.isPresent());
        assertEquals(product.get().getPrice(), existing.getPrice());
    }

    @Test
    void getProductPriceAtTime_WhenRecordDoesNotExists_ShouldNotReturnValue() {
        when(productRepository.findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(anyString(),
                any(LocalDate.class))).thenReturn(Optional.empty());

        Optional<Product> product = productService.getProductPriceAtTime("1704.90.35", LocalDate.now());

        assertFalse(product.isPresent());
    }

    @Test
    void getProductPrice_WhenRecordExistsByCategory_ShouldReturnValue() {
        when(productRepository.findByCategory(anyString())).thenReturn(Optional.of(List.of(existing)));

        Optional<List<Product>> products = productService.getProductsByCategory("sugar");

        assertTrue(products.isPresent());
        assertEquals(1, products.get().size());
        assertEquals(products.get().get(0).getPrice(), existing.getPrice());
        assertEquals(products.get().get(0).getHtsCode(), existing.getHtsCode());
        assertEquals(products.get().get(0).getFetchDate(), existing.getFetchDate());
        assertEquals(products.get().get(0).getCategory(), existing.getCategory());
    }


}
