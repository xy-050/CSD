package app.query;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.exception.TariffNotFoundException;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock
    private TariffApiClient apiClient;

    @InjectMocks
    private TariffService tariffService;

    // ==================== getNextLevelHtsCodes Tests ====================

    @Test
    void getNextLevelHtsCodes_WhenQueryIsKeyword_ShouldReturnMainCategories() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704", "description", "Sugar confectionery"),
            Map.of("htsno", "1704.01", "description", "Chewing gum"),
            Map.of("htsno", "1705", "description", "Other sugar")
        );
        
        when(apiClient.searchTariffArticles("sugar")).thenReturn(apiResults);

        List<Map<String, Object>> result = tariffService.getNextLevelHtsCodes("sugar");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> "1704".equals(m.get("htsno"))));
        assertTrue(result.stream().anyMatch(m -> "1705".equals(m.get("htsno"))));
        assertFalse(result.stream().anyMatch(m -> "1704.01".equals(m.get("htsno"))));
    }

    @Test
    void getNextLevelHtsCodes_WhenQueryIsHtsCode_ShouldReturnSubcategories() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704", "description", "Sugar confectionery"),
            Map.of("htsno", "1704.01", "description", "Chewing gum"),
            Map.of("htsno", "1704.02", "description", "Other"),
            Map.of("htsno", "1704.01.10", "description", "Containing cocoa")
        );
        
        when(apiClient.searchTariffArticles("1704")).thenReturn(apiResults);

        List<Map<String, Object>> result = tariffService.getNextLevelHtsCodes("1704");

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(m -> "1704.01".equals(m.get("htsno"))));
        assertTrue(result.stream().anyMatch(m -> "1704.02".equals(m.get("htsno"))));
        assertTrue(result.stream().anyMatch(m -> "1704.01.10".equals(m.get("htsno"))));
        assertFalse(result.stream().anyMatch(m -> "1704".equals(m.get("htsno"))));
    }

    @Test
    void getNextLevelHtsCodes_WhenQueryIsHtsCodeWithStatSuffix_ShouldReturnCodesWithTwoDigitSuffix() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704.01", "description", "Chewing gum"),
            Map.of("htsno", "1704.01.10", "description", "Containing cocoa"),
            Map.of("htsno", "1704.01.10.00", "description", "Stat suffix"),
            Map.of("htsno", "1704.01.10.123", "description", "Invalid suffix")
        );
        
        when(apiClient.searchTariffArticles("1704.01")).thenReturn(apiResults);

        List<Map<String, Object>> result = tariffService.getNextLevelHtsCodes("1704.01");

        assertTrue(result.stream().anyMatch(m -> "1704.01.10".equals(m.get("htsno"))));
        assertTrue(result.stream().anyMatch(m -> "1704.01.10.00".equals(m.get("htsno"))));
        assertFalse(result.stream().anyMatch(m -> "1704.01.10.123".equals(m.get("htsno"))));
    }

    @Test
    void getNextLevelHtsCodes_WhenNoResultsFromApi_ShouldReturnEmptyList() {
        when(apiClient.searchTariffArticles("nonexistent")).thenReturn(List.of());

        List<Map<String, Object>> result = tariffService.getNextLevelHtsCodes("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void getNextLevelHtsCodes_WhenHtsnoIsNull_ShouldFilterOut() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("description", "No HTS code"),
            Map.of("htsno", "1704", "description", "Has HTS code")
        );
        
        when(apiClient.searchTariffArticles("test")).thenReturn(apiResults);

        List<Map<String, Object>> result = tariffService.getNextLevelHtsCodes("test");

        assertEquals(1, result.size());
        assertEquals("1704", result.get(0).get("htsno"));
    }

    // ==================== extractCountryTariffs Tests ====================

    @Test
    void extractCountryTariffs_WhenSpecialAndGeneralPresent_ShouldReturnBoth() {
        Map<String, Object> item = Map.of(
            "special", "$1.00 (AU, SG)",
            "general", "$2.50"
        );

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertEquals("$1.00", result.get("Special rate"));
        assertEquals("$2.50", result.get("General rate"));
        assertTrue(result.get("Special countries") instanceof List);
        
        List<?> countries = (List<?>) result.get("Special countries");
        assertEquals(2, countries.size());
        // Verify country names are returned (using Locale)
        String auName = new Locale("", "AU").getDisplayCountry();
        String sgName = new Locale("", "SG").getDisplayCountry();
        assertTrue(countries.contains(auName));
        assertTrue(countries.contains(sgName));
    }

    @Test
    void extractCountryTariffs_WhenOnlyGeneralPresent_ShouldReturnGeneral() {
        Map<String, Object> item = Map.of("general", "$2.50");

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertEquals("$2.50", result.get("General rate"));
        assertNull(result.get("Special rate"));
        assertNull(result.get("Special countries"));
    }

    @Test
    void extractCountryTariffs_WhenSpecialHasNoParentheses_ShouldOnlyReturnGeneral() {
        Map<String, Object> item = Map.of(
            "special", "Free",
            "general", "$2.50"
        );

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertEquals("$2.50", result.get("General rate"));
        assertNull(result.get("Special rate"));
    }

    @Test
    void extractCountryTariffs_WhenSpecialIsNull_ShouldReturnGeneralOnly() {
        Map<String, Object> item = Map.of("general", "$2.50");

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertEquals("$2.50", result.get("General rate"));
        assertNull(result.get("Special rate"));
    }

    @Test
    void extractCountryTariffs_WhenBothNull_ShouldReturnEmptyMap() {
        Map<String, Object> item = Map.of();

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertTrue(result.isEmpty());
    }

    @Test
    void extractCountryTariffs_WhenInvalidCountryCode_ShouldFilterOut() {
        Map<String, Object> item = Map.of(
            "special", "$1.00 (XX, AU, ZZ)",
            "general", "$2.50"
        );

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        List<?> countries = (List<?>) result.get("Special countries");
        // Only AU should be valid, XX and ZZ are invalid ISO codes
        assertEquals(1, countries.size());
        String auName = new Locale("", "AU").getDisplayCountry();
        assertTrue(countries.contains(auName));
    }

    @Test
    void extractCountryTariffs_WhenMultipleValidCountries_ShouldIncludeAll() {
        Map<String, Object> item = Map.of(
            "special", "Free (US, GB, CA, JP)",
            "general", "$3.00"
        );

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertEquals("Free", result.get("Special rate"));
        assertEquals("$3.00", result.get("General rate"));
        
        List<?> countries = (List<?>) result.get("Special countries");
        assertEquals(4, countries.size());
    }

    @Test
    void extractCountryTariffs_WhenSpecialRateHasSpaces_ShouldTrimCorrectly() {
        Map<String, Object> item = Map.of(
            "special", "  $0.50  (AU, SG)  ",
            "general", "$2.00"
        );

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        assertEquals("$0.50", result.get("Special rate"));
    }

    @Test
    void extractCountryTariffs_WhenCountryCodesHaveSpaces_ShouldTrimAndMatch() {
        Map<String, Object> item = Map.of(
            "special", "$1.00 ( AU , SG , NZ )",
            "general", "$2.50"
        );

        Map<String, Object> result = tariffService.extractCountryTariffs(item);

        List<?> countries = (List<?>) result.get("Special countries");
        assertEquals(3, countries.size());
    }

    // ==================== searchAndSortTariffs Tests ====================

    @Test
    void searchAndSortTariffs_ShouldSortByGeneralTariffDescending() {
        List<Map<String, Object>> unsortedResults = List.of(
            Map.of("htsno", "1704.01", "general", "$1.00"),
            Map.of("htsno", "1704.02", "general", "$5.00"),
            Map.of("htsno", "1704.03", "general", "$3.00")
        );
        
        when(apiClient.searchTariffArticles("1704")).thenReturn(unsortedResults);

        List<Map<String, Object>> result = tariffService.searchAndSortTariffs("1704");

        assertEquals("$5.00", result.get(0).get("general"));
        assertEquals("$3.00", result.get(1).get("general"));
        assertEquals("$1.00", result.get(2).get("general"));
    }

    @Test
    void searchAndSortTariffs_WhenGeneralIsNull_ShouldHandleGracefully() {
        List<Map<String, Object>> results = List.of(
            Map.of("htsno", "1704.01", "general", "$2.00"),
            Map.of("htsno", "1704.02"),
            Map.of("htsno", "1704.03", "general", "$4.00")
        );
        
        when(apiClient.searchTariffArticles("1704")).thenReturn(results);

        List<Map<String, Object>> result = tariffService.searchAndSortTariffs("1704");

        assertEquals(3, result.size());
        assertEquals("$4.00", result.get(0).get("general"));
    }

    @Test
    void searchAndSortTariffs_WhenEmptyResults_ShouldReturnEmptyList() {
        when(apiClient.searchTariffArticles("nonexistent")).thenReturn(List.of());

        List<Map<String, Object>> result = tariffService.searchAndSortTariffs("nonexistent");

        assertTrue(result.isEmpty());
    }

    // ==================== searchByHtsNo Tests ====================

    @Test
    void searchByHtsNo_WhenHtsNoExists_ShouldReturnMatchingItem() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704.01", "description", "Chewing gum"),
            Map.of("htsno", "1704.02", "description", "Other")
        );
        
        when(apiClient.searchTariffArticles("1704.01")).thenReturn(apiResults);

        Map<String, Object> result = tariffService.searchByHtsNo("1704.01");

        assertNotNull(result);
        assertEquals("1704.01", result.get("htsno"));
        assertEquals("Chewing gum", result.get("description"));
    }

    @Test
    void searchByHtsNo_WhenHtsNoDoesNotExist_ShouldThrowTariffNotFoundException() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704.02", "description", "Other")
        );
        
        when(apiClient.searchTariffArticles("1704.01")).thenReturn(apiResults);

        TariffNotFoundException exception = assertThrows(
            TariffNotFoundException.class,
            () -> tariffService.searchByHtsNo("1704.01")
        );

        assertEquals("No tariff information found for 1704.01", exception.getMessage());
    }

    @Test
    void searchByHtsNo_WhenApiReturnsEmpty_ShouldThrowTariffNotFoundException() {
        when(apiClient.searchTariffArticles("9999.99")).thenReturn(List.of());

        assertThrows(
            TariffNotFoundException.class,
            () -> tariffService.searchByHtsNo("9999.99")
        );
    }

    @Test
    void searchByHtsNo_WhenMultipleResultsButOnlyOneMatches_ShouldReturnExactMatch() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704", "description", "Parent"),
            Map.of("htsno", "1704.01", "description", "Target"),
            Map.of("htsno", "1704.02", "description", "Other")
        );
        
        when(apiClient.searchTariffArticles("1704.01")).thenReturn(apiResults);

        Map<String, Object> result = tariffService.searchByHtsNo("1704.01");

        assertEquals("1704.01", result.get("htsno"));
        assertEquals("Target", result.get("description"));
    }

    // ==================== extractTariffSummary Tests ====================

    @Test
    void extractTariffSummary_WhenValidKeyword_ShouldReturnSortedResults() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704", "description", "Sugar confectionery", 
                   "general", "$2.00", "units", "kg")
        );
        
        when(apiClient.searchTariffArticles("sugar")).thenReturn(apiResults);

        List<Map<String, Object>> result = tariffService.extractTariffSummary("sugar");

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).containsKey("htsno"));
        assertTrue(result.get(0).containsKey("descriptionChain"));
        assertTrue(result.get(0).containsKey("general"));
    }

    @Test
    void extractTariffSummary_ShouldBuildDescriptionChain() {
        List<Map<String, Object>> apiResults = List.of(
            Map.of("htsno", "1704", "description", "Sugar confectionery"),
            Map.of("htsno", "1704.01", "description", "Chewing gum")
        );
        
        when(apiClient.searchTariffArticles(anyString())).thenReturn(apiResults);

        List<Map<String, Object>> result = tariffService.extractTariffSummary("sugar");

        assertFalse(result.isEmpty());
        Object descChain = result.get(0).get("descriptionChain");
        assertTrue(descChain instanceof List);
    }
}