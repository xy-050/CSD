
package app.query;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import app.account.Account;
import app.account.AccountService;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

@Service
public class QueryService {

    private final AccountService accountService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String HTS_SEARCH_API = "https://hts.usitc.gov/reststop/search";
    private final QueryRepository queryRepository;

    /**
     * Constructor-based injection
     * 
     * @param queryRepository The QueryRepository instance
     */
    public QueryService(QueryRepository queryRepository, AccountService accountService) {
        this.queryRepository = queryRepository;
        this.accountService = accountService;
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
     * Returns only the next level of HTS codes for a given keyword or HTS code.
     * If the query is a keyword, returns main categories (codes with no dots).
     * If the query is an HTS code, returns codes that start with the code + '.' and
     * have exactly one more dot than the query.
     * 
     * @param query The keyword or HTS code
     * @return List of next-level HTS code articles
     */
    public List<Map<String, Object>> getNextLevelHtsCodes(String query) {
        List<Map<String, Object>> rawResults = searchTariffArticles(query);
        // If query is a keyword (not a code), return only codes with no dots (main
        // categories)
        if (!query.matches("\\d+(\\.\\d+)*")) {
            return rawResults.stream()
                    .filter(item -> {
                        Object htsno = item.get("htsno");
                        return htsno != null && htsno.toString().indexOf('.') == -1;
                    })
                    .toList();
        }
        // If query is a code, return only codes that start with query + '.' and have
        // exactly one more dot (subcategory),
        // or exactly two more dots where the last part is a two-digit stat suffix
        // (e.g., 0401.20.20.00)
        int dotCount = countDots(query);
        String prefix = query + ".";
        return rawResults.stream()
                .filter(item -> {
                    Object htsno = item.get("htsno");
                    if (htsno == null)
                        return false;
                    String code = htsno.toString();
                    int codeDotCount = countDots(code);
                    if (!code.startsWith(prefix))
                        return false;
                    if (codeDotCount == dotCount + 1) {
                        return true; // direct subcategory
                    }
                    // If code has two more dots, check if last part is a two-digit stat suffix
                    if (codeDotCount == dotCount + 2) {
                        String[] parts = code.split("\\.");
                        if (parts.length > 0) {
                            String last = parts[parts.length - 1];
                            return last.matches("\\d{2}");
                        }
                    }
                    return false;
                })
                .toList();
    }

    // Helper to count dots in a string
    private static int countDots(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '.')
                count++;
        }
        return count;
    }

    /**
     * Extracts only the relevant fields (htsno, description, units, general) from
     * the raw tariff articles.
     * 
     * @param keyword The word or phrase to search for
     * @return List of simplified tariff articles
     */
    public List<Map<String, Object>> extractTariffSummary(String keyword) {
        List<Map<String, Object>> rawResults = getNextLevelHtsCodes(keyword);
        // Build a map from htsno to description for fast lookup, including all parent
        // codes
        java.util.Map<String, String> codeToDescription = new java.util.HashMap<>();
        java.util.Set<String> searchedCodes = new java.util.HashSet<>();
        // Add all codes from the current search
        for (Map<String, Object> item : searchTariffArticles(keyword)) {
            Object htsno = item.get("htsno");
            Object desc = item.get("description");
            if (htsno != null && desc != null) {
                codeToDescription.put(htsno.toString(), desc.toString());
            }
        }
        // For each result, walk up the hierarchy and fetch missing parent descriptions
        for (Map<String, Object> item : rawResults) {
            String code = item.get("htsno") != null ? item.get("htsno").toString() : null;
            if (code == null)
                continue;
            String[] parts = code.split("\\.");
            StringBuilder parent = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0)
                    parent.append(".");
                parent.append(parts[i]);
                String parentCode = parent.toString();
                if (!codeToDescription.containsKey(parentCode) && searchedCodes.add(parentCode)) {
                    // Search for this parent code and add its description if found
                    for (Map<String, Object> parentItem : searchTariffArticles(parentCode)) {
                        Object htsno = parentItem.get("htsno");
                        Object desc = parentItem.get("description");
                        if (htsno != null && desc != null && htsno.toString().equals(parentCode)) {
                            codeToDescription.put(parentCode, desc.toString());
                        }
                    }
                }
            }
        }
        List<Map<String, Object>> filteredList = rawResults.stream().map(item -> {
            Map<String, Object> filtered = new java.util.HashMap<>();
            String code = item.get("htsno") != null ? item.get("htsno").toString() : null;
            filtered.put("htsno", code);
            filtered.put("units", item.get("units"));
            // filtered.put("description", item.get("description"));
            filtered.put("general", item.get("general"));
            filtered.put("special", item.get("special"));
            // Build description chain from top category to this code
            if (code != null) {
                java.util.List<String> chain = new java.util.ArrayList<>();
                String[] parts = code.split("\\.");
                StringBuilder parent = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0)
                        parent.append(".");
                    parent.append(parts[i]);
                    String parentCode = parent.toString();
                    String desc = codeToDescription.get(parentCode);
                    if (desc != null && !desc.isEmpty()) {
                        chain.add(desc);
                    }
                }
                filtered.put("descriptionChain", chain);
            }
            return filtered;
        }).toList();
        // Sort by 'general' tariff value descending
        String keywordLower = keyword == null ? "" : keyword.toLowerCase();
        List<Map<String, Object>> sorted = filteredList.stream()
                .sorted((a, b) -> {
                    // Sort by position of keyword in description (earlier is higher)
                    String descA = a.get("descriptionChain") instanceof java.util.List<?>
                            && !((java.util.List<?>) a.get("descriptionChain")).isEmpty()
                                    ? ((java.util.List<?>) a.get("descriptionChain"))
                                            .get(((java.util.List<?>) a.get("descriptionChain")).size() - 1).toString()
                                            .toLowerCase()
                                    : "";
                    String descB = b.get("descriptionChain") instanceof java.util.List<?>
                            && !((java.util.List<?>) b.get("descriptionChain")).isEmpty()
                                    ? ((java.util.List<?>) b.get("descriptionChain"))
                                            .get(((java.util.List<?>) b.get("descriptionChain")).size() - 1).toString()
                                            .toLowerCase()
                                    : "";
                    int idxA = descA.indexOf(keywordLower);
                    int idxB = descB.indexOf(keywordLower);
                    if (idxA == -1)
                        idxA = Integer.MAX_VALUE;
                    if (idxB == -1)
                        idxB = Integer.MAX_VALUE;
                    if (idxA != idxB)
                        return Integer.compare(idxA, idxB);
                    // If same, sort by general tariff value descending
                    double valA = parseTariffValue(a.get("general"));
                    double valB = parseTariffValue(b.get("general"));
                    return Double.compare(valB, valA);
                })
                .toList();
        // // Return only the first item (highest tariff), or empty list if none
        // if (!sorted.isEmpty()) {
        // return List.of(sorted.get(0));
        // } else {
        // return List.of();
        // }
        return sorted;
    }

    // Helper to parse tariff value from Object (String or null)
    private static double parseTariffValue(Object value) {
        if (value == null)
            return Double.NEGATIVE_INFINITY;
        String str = value.toString().replaceAll("[^0-9.]+", "");
        if (str.isEmpty())
            return Double.NEGATIVE_INFINITY;
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
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
        java.util.Map<String, String> isoCountryMap = app.query.CountryCodes.isoCountryMap;
        String special = (String) item.get("special");
        String general = (String) item.get("general");
        Map<String, Object> result = new java.util.HashMap<>();
        if (special != null && !special.isEmpty()) {
            int start = special.indexOf('(');
            int end = special.indexOf(')');
            if (start != -1 && end != -1 && end > start) {
                String codesStr = special.substring(start + 1, end);
                String[] codes = codesStr.split(",");
                String specialRate = special.substring(0, start).trim();
                java.util.List<String> specialCountries = new java.util.ArrayList<>();
                for (String code : codes) {
                    code = code.trim();
                    if (isoCountryMap.containsKey(code)) {
                        String country = isoCountryMap.get(code);
                        specialCountries.add(country);
                    }
                }
                result.put("Special countries", specialCountries);
                result.put("Special rate", specialRate);
            }
        }
        // Always include the general rate (even if no special)
        if (general != null && !general.isEmpty()) {
            result.put("General rate", general);
        }
        return result;
    }

    /**
     * Returns the most queried products with details (HTS code, description, category, count).
     * 
     * @return List of QueryDTO objects containing product details and query counts
     */
    public List<QueryDTO> getMostQueried() {
        // Use the new repository method to get top 10 with product details
        return queryRepository.findTopQueriesWithDetails(PageRequest.of(0, 10));
    }

    /**
     * Adds a new Query record to the database.
     * 
     * @param query The Query object to be saved
     * @return The saved Query object with generated ID
     */
    public Query addQuery(Query query) {
        return queryRepository.save(query);
    }

    public List<Query> getQueriesByUserId(Integer userID) {
        Account user = accountService.getAccountByUserID(userID);
        return queryRepository.findByUserID(user);
    }

    public void deleteQuery(Long queryID) {
        if (!queryRepository.existsById(queryID)) {
            throw new IllegalArgumentException("Query with ID " + queryID + " not found.");

        } else {
            queryRepository.deleteById(queryID);
        }
    }

}
