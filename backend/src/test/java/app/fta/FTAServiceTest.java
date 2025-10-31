package app.fta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.exception.FTANotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class FTAServiceTest {
    @Mock
    private FTARepository ftaRepository;

    @InjectMocks
    private FTAService ftaService;

    private FTA fta1;
    private FTA fta2;
    private List<FTA> ftas;

    @BeforeEach
    public void SetUp() {
        fta1 = new FTA();
        fta1.setCountry("Singapore");
        fta1.setPrices(Map.of(
                "1704.1.1", Map.of(
                        LocalDate.of(2004, Month.JANUARY, 1), 5.0),
                "1704.1.2", Map.of(
                        LocalDate.of(2007, Month.JANUARY, 1), 4.5,
                        LocalDate.of(2009, Month.JANUARY, 1), 4.0)));

        fta2 = new FTA();
        fta2.setCountry("Australia");
        fta2.setPrices(Map.of(
                "1704.1.1", Map.of(
                        LocalDate.of(2010, Month.JANUARY, 1), 6.0),
                "1704.1.2", Map.of(
                        LocalDate.of(2011, Month.JANUARY, 1), 5.5,
                        LocalDate.of(2014, Month.JANUARY, 1), 3.0)));

        ftas = List.of(fta1, fta2);
    }

    @Test
    public void getAllFTAs_ShouldHaveTwoEntries() {
        when(ftaRepository.findAll()).thenReturn(ftas);

        List<FTA> allFTAs = ftaService.getAllFTAs();

        assertEquals(allFTAs.size(), 2);
        verify(ftaRepository).findAll();
    }

    @Test
    public void getAllFTAs_WhenNoFTAs_ShouldHaveZeroEntries() {
        when(ftaRepository.findAll()).thenReturn(new ArrayList<>());

        List<FTA> allFTAs = ftaService.getAllFTAs();

        assertEquals(allFTAs.size(), 0);
        verify(ftaRepository).findAll();
    }

    @Test
    public void getFTAGivenCountry_WhenCountryExists_ShouldReturnFTA() {
        when(ftaRepository.findById(any(String.class))).thenReturn(Optional.of(fta1));

        FTA fta = ftaService.getFTAGivenCountry("Singapore");

        assertEquals(fta, fta1);
        verify(ftaRepository).findById("Singapore");
    }

    @Test
    public void getFTAGivenCountry_WhenCountryDoesNotExist_ShouldThrowFTANotFoundException() {
        when(ftaRepository.findById(any(String.class))).thenReturn(Optional.empty());

        FTANotFoundException exception = assertThrows(FTANotFoundException.class,
                () -> ftaService.getFTAGivenCountry("Malaysia"));

        assertNotNull(exception);
        assertEquals("Error! FTA with Malaysia does not exist.", exception.getMessage());
        verify(ftaRepository).findById("Malaysia");
    }

    @Test
    public void updateFTA_WhenCountryExists_ShouldUpdate() {
        Map<String, Map<LocalDate, Double>> newPrices = Map.of(
                "1704.1.1", Map.of(
                        LocalDate.of(2004, Month.JANUARY, 1), 5.0,
                        LocalDate.of(2010, Month.APRIL, 1), 2.0),
                "1704.1.2", Map.of(
                        LocalDate.of(2007, Month.JANUARY, 1), 4.5,
                        LocalDate.of(2009, Month.JANUARY, 1), 4.0,
                        LocalDate.of(2015, Month.SEPTEMBER, 1), 0.0));

        when(ftaRepository.findById(any(String.class))).thenReturn(Optional.of(fta1));

        ftaService.updateFTA("Singapore", newPrices);
        assertEquals(fta1.getPrices(), newPrices);
        assertTrue(fta1.getPrices().containsKey("1704.1.1"));
        assertTrue(fta1.getPrices().containsKey("1704.1.2"));
        assertEquals(fta1.getPrices().get("1704.1.1").get(LocalDate.of(2010, Month.APRIL, 1)), 2.0);
        assertEquals(fta1.getPrices().get("1704.1.2").get(LocalDate.of(2015, Month.SEPTEMBER, 1)), 0.0);

        verify(ftaRepository).findById("Singapore");
    }

    @Test
    public void updateFTA_WhenCountryDoesNotExist_ShouldThrowFTANotFoundException() {
        Map<String, Map<LocalDate, Double>> newPrices = Map.of(
                "1704.1.1", Map.of(
                        LocalDate.of(2004, Month.JANUARY, 1), 5.0,
                        LocalDate.of(2010, Month.APRIL, 1), 2.0),
                "1704.1.2", Map.of(
                        LocalDate.of(2007, Month.JANUARY, 1), 4.5,
                        LocalDate.of(2009, Month.JANUARY, 1), 4.0,
                        LocalDate.of(2015, Month.SEPTEMBER, 1), 0.0));
        when(ftaRepository.findById(any(String.class))).thenReturn(Optional.empty());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        ftaService.updateFTA("Malaysia", newPrices);

        String output = outputStream.toString().trim();
        assertEquals("Error! FTA with Malaysia does not exist.", output);

        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        verify(ftaRepository).findById("Malaysia");
    }

}
