
package app.query;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class QueryService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String HTS_SEARCH_API = "https://hts.usitc.gov/reststop/search";
    private final QueryRepository queryRepository;

    /**
     * Constructor-based injection
     * 
     * @param queryRepository The QueryRepository instance
     */
    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

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

    /**
     * Extracts only the relevant fields (htsno, description, units, general) from
     * the raw tariff articles.
     * 
     * @param keyword The word or phrase to search for
     * @return List of simplified tariff articles
     */
    public List<Map<String, Object>> extractTariffSummary(String keyword) {
        List<Map<String, Object>> rawResults = searchTariffArticles(keyword);
        return rawResults.stream().map(item -> {
            Map<String, Object> filtered = new java.util.HashMap<>();
            filtered.put("htsno", item.get("htsno"));
            filtered.put("description", item.get("description"));
            filtered.put("units", item.get("units"));
            filtered.put("general", item.get("general"));
            return filtered;
        }).toList();
    }

    /**
     * Given a tariff article (as a Map), returns a map of country names to their
     * special tariff rate.
     * Countries not listed in 'special' get the 'general' or 'other' rate.
     * 
     * @param item The tariff article map (from the API)
     * @return Map of country name to tariff rate string
     */
    public Map<String, Object> extractCountryTariffs(Map<String, Object> item) {
        Map<String, String> countryToRate = new java.util.HashMap<>();
        String special = (String) item.get("special");
        String general = (String) item.get("general");
        String other = (String) item.get("other");
        java.util.Map<String, String> isoCountryMap = app.query.CountryCodes.isoCountryMap;

        // Assign special rates
        String specialRate = null;
        java.util.Set<String> specialCountries = new java.util.HashSet<>();
        if (special != null && !special.isEmpty()) {
            int start = special.indexOf('(');
            int end = special.indexOf(')');
            if (start != -1 && end != -1 && end > start) {
                String codesStr = special.substring(start + 1, end);
                String[] codes = codesStr.split(",");
                specialRate = special.substring(0, start).trim();
                for (String code : codes) {
                    code = code.trim();
                    if (isoCountryMap.containsKey(code)) {
                        String country = isoCountryMap.get(code);
                        countryToRate.put(country, specialRate);
                        specialCountries.add(country);
                    }
                }
            }
        }

        // Assign general/other rates to the rest
        String defaultRate = general != null && !general.isEmpty() ? general
                : (other != null && !other.isEmpty() ? other : "N/A");
        for (String code : isoCountryMap.keySet()) {
            String country = isoCountryMap.get(code);
            if (!countryToRate.containsKey(country)) {
                countryToRate.put(country, defaultRate);
            }
        }

        // Group countries by rate
        java.util.Map<String, java.util.List<String>> rateToCountries = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : countryToRate.entrySet()) {
            rateToCountries.computeIfAbsent(entry.getValue(), k -> new java.util.ArrayList<>()).add(entry.getKey());
        }

        Map<String, Object> result = new java.util.HashMap<>();
        if (rateToCountries.size() == 1) {
            // All countries have the same rate
            String rate = rateToCountries.keySet().iterator().next();
            result.put("All countries", rate);
        } else if (rateToCountries.size() == 2 && specialRate != null) {
            // One special rate, one default rate
            final String specialRateFinal = specialRate;
            java.util.List<String> specialList = rateToCountries.get(specialRateFinal);
            java.util.List<String> otherList = rateToCountries.entrySet().stream()
                    .filter(e -> !e.getKey().equals(specialRateFinal))
                    .flatMap(e -> e.getValue().stream()).toList();
            result.put("Special countries", specialList);
            result.put("Special rate", specialRateFinal);
            result.put("Other Countries", otherList);
            result.put("Other rate", defaultRate);
        } else {
            // Multiple rates, group by rate
            for (Map.Entry<String, java.util.List<String>> entry : rateToCountries.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Returns the most queried product.
     * TODO: currently returns HTS codes, should we change to the name of the product? if so, need to map HTS codes to product names
     * 
     * @return List of most queried product codes (HTS codes)
     */
    public List<String> getMostQueried() {
        return queryRepository.findMostQueried();
    }
}
