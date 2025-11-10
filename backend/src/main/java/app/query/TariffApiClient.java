package app.query;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TariffApiClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String HTS_SEARCH_API = "https://hts.usitc.gov/reststop/search";

    /**
     * Searches the US HTS REST API for tariff articles containing the given
     * keyword.
     * Returns up to the first 100 matching articles in JSON format.
     * 
     * @param keyword The word or phrase to search for
     * @return List of matching tariff articles (as Maps), or empty list if none
     *         found
     */
    public List<Map<String, Object>> searchTariffArticles(String keyword) {
        String url = HTS_SEARCH_API + "?keyword=" + keyword;
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            // System.out.println("Raw API response: " + response);
            List<Map<String, Object>> rawResults = null;
            if (response instanceof List<?> resultsList) {
                // noinspection unchecked
                rawResults = (List<Map<String, Object>>) resultsList;
            } else if (response instanceof Map<?, ?> map) {
                Object resultsObj = map.get("results");
                if (resultsObj instanceof List<?> resultsList) {
                    // noinspection unchecked
                    rawResults = (List<Map<String, Object>>) resultsList;
                }
            }
            if (rawResults == null)
                return List.of();
            return rawResults;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}