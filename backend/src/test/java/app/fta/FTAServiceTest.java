package app.fta;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import app.exception.FTANotFoundException;

public class FTAServiceTest {
    
    @Mock
    private FTARepository ftaRepository;
    
    private FTAService ftaService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ftaService = new FTAService(ftaRepository);
    }
    
    @Test
    void createFTA_ShouldSaveNewFTA() {
        // Arrange
        String country = "Singapore";
        String htsCode = "123456";
        String price = "$10.0";
        LocalDate date = LocalDate.now();
        
        // Act
        ftaService.createFTA(country, htsCode, price, date);
        
        // Assert
        verify(ftaRepository).save(any(FTA.class));
    }
    
    @Test
    void getAllFTAs_ShouldReturnAllFTAs() {
        // Arrange
        FTA fta1 = new FTA("Singapore", "123", "$10.0", LocalDate.now());
        FTA fta2 = new FTA("Malaysia", "456", "$20.0", LocalDate.now());
        List<FTA> expectedFTAs = Arrays.asList(fta1, fta2);
        
        when(ftaRepository.findAll()).thenReturn(expectedFTAs);
        
        // Act
        List<FTA> actualFTAs = ftaService.getAllFTAs();
        
        // Assert
        assertEquals(expectedFTAs, actualFTAs);
        verify(ftaRepository).findAll();
    }
    
    @Test
    void getFTAGivenCountry_WithExistingCountry_ShouldReturnFTAs() {
        // Arrange
        String country = "Singapore";
        FTA fta = new FTA(country, "123", "$10.0", LocalDate.now());
        List<FTA> expectedFTAs = Arrays.asList(fta);
        
        when(ftaRepository.findByCountry(country)).thenReturn(Optional.of(expectedFTAs));
        
        // Act
        List<FTA> actualFTAs = ftaService.getFTAGivenCountry(country);
        
        // Assert
        assertEquals(expectedFTAs, actualFTAs);
        verify(ftaRepository).findByCountry(country);
    }
    
    @Test
    void getFTAGivenCountry_WithNonExistingCountry_ShouldThrowException() {
        // Arrange
        String country = "NonExistingCountry";
        when(ftaRepository.findByCountry(country)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(FTANotFoundException.class, () -> {
            ftaService.getFTAGivenCountry(country);
        });
        verify(ftaRepository).findByCountry(country);
    }

    @Test
    void getFuturePrices_CountryFoundAndHaveFuturePrices_ShouldReturnMapWithPrice() {
        // Arrange
        String country = "Singapore";
        String htsCode = "1234.56";
        FTA fta1 = new FTA(1L, country, htsCode, "$2.30", LocalDate.of(2026, 1, 1));
        FTA fta2 = new FTA(2L, country, htsCode, "$2.10", LocalDate.of(2028, 1, 1));
        when(ftaRepository.findByCountry(country)).thenReturn(Optional.of(List.of(fta1, fta2)));

        // Act
        Map<LocalDate, String> prices = ftaService.getFuturePrices(country, htsCode);

        // Assert
        assertEquals(prices, Map.of(
            LocalDate.of(2026, 1, 1), "$2.30",
            LocalDate.of(2028, 1, 1), "$2.10"
        ));
    }

    @Test
    void getFuturePrices_CountryFoundAndNoFuturePrices_ShouldReturnEmptyMap() {
        
    }


    @Test
    void getFuturePrices_CountryNotFound_ShouldReturnEmptyMap() {

    }
}
