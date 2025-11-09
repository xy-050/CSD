package app.query;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import app.exception.TariffNotFoundException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TariffService {
    private final TariffApiClient apiClient;

    public TariffService(TariffApiClient apiClient) {
        this.apiClient = apiClient;
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
        List<Map<String, Object>> rawResults = apiClient.searchTariffArticles(query);
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
        int dotCount = TariffUtils.countDots(query);
        String prefix = query + ".";
        return rawResults.stream()
                .filter(item -> {
                    Object htsno = item.get("htsno");
                    if (htsno == null)
                        return false;
                    String code = htsno.toString();
                    int codeDotCount = TariffUtils.countDots(code);
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
        for (Map<String, Object> item : apiClient.searchTariffArticles(keyword)) {
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
                    for (Map<String, Object> parentItem : apiClient.searchTariffArticles(parentCode)) {
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
                    double valA = TariffUtils.parseTariffValue(a.get("general"));
                    double valB = TariffUtils.parseTariffValue(b.get("general"));
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

    /**
     * Given a tariff article (as a Map), returns a map of country names to their
     * special tariff rate.
     * Countries not listed in 'special' get the 'general' or 'other' rate.
     * 
     * @param item The tariff article map (from the API)
     * @return Map of country name to tariff rate string
     */
    public Map<String, Object> extractCountryTariffs(Map<String, Object> item) {
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

                // Get set of valid ISO country codes
                java.util.Set<String> validCodes = java.util.Set.of(Locale.getISOCountries());

                for (String code : codes) {
                    code = code.trim();
                    // Check if code is valid
                    if (validCodes.contains(code)) {
                        // Get country name from code
                        String countryName = new Locale("", code).getDisplayCountry();
                        specialCountries.add(countryName);
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

    public List<Map<String, Object>> searchAndSortTariffs(String keyword) {
        List<Map<String, Object>> results = extractTariffSummary(keyword);
        return results.stream()
                .sorted((a, b) -> {
                    double valA = TariffUtils.parseTariffValue(a.get("general"));
                    double valB = TariffUtils.parseTariffValue(b.get("general"));
                    return Double.compare(valB, valA); // descending
                })
                .toList();
    }

    public Map<String, Object> searchByHtsNo(String htsno) {
        List<Map<String, Object>> results = apiClient.searchTariffArticles(htsno);
        Map<String, Object> item = results.stream()
                .filter(map -> htsno.equals(map.get("htsno")))
                .findFirst()
                .orElse(null);

        if (item == null) {
            throw new TariffNotFoundException("No tariff information found for " + htsno);
        }

        return item;
    }
}