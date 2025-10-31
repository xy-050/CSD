package app.fta;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import app.exception.FTANotFoundException;

@ExtendWith(MockitoExtension.class)
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
        double price = 10.0;
        LocalDate date = LocalDate.now();
        
        // Act
        ftaService.createFTA(country, htsCode, price, date);
        
        // Assert
        verify(ftaRepository).save(any(FTA.class));
    }
    
    @Test
    void getAllFTAs_ShouldReturnAllFTAs() {
        // Arrange
        FTA fta1 = new FTA("Singapore", "123", 10.0, LocalDate.now());
        FTA fta2 = new FTA("Malaysia", "456", 20.0, LocalDate.now());
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
        FTA fta = new FTA(country, "123", 10.0, LocalDate.now());
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
}
