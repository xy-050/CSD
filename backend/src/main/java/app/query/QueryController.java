package app.query;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/tariffs")
public class QueryController {

	private final QueryService queryService;

	@Autowired
	public QueryController(QueryService queryService) {
		this.queryService = queryService;
	}

	// // 1. View tariff rates (all or by country)
	// @GetMapping("/rates")
	// public ResponseEntity<Map<String, Double>> getTariffRates(@RequestParam(value = "country", required = false) String country) {
	// 	if (country != null && !country.isEmpty()) {
	// 		return ResponseEntity.ok(queryService.getTariffRatesByCountry(country));
	// 	} else {
	// 		return ResponseEntity.ok(queryService.getTariffRates());
	// 	}
	// }

	// // 2. Calculate tariff for a quantity of goods (optionally by country)
	// @GetMapping("/calculate")
	// public ResponseEntity<Map<String, Object>> calculateTariff(
	// 		@RequestParam int quantity,
	// 		@RequestParam(value = "country", required = false) String country) {
	// 	double result;
	// 	if (country != null && !country.isEmpty()) {
	// 		result = queryService.calculateTariffForGoodsByCountry(country, quantity);
	// 	} else {
	// 		result = queryService.calculateTariffForGoods(quantity);
	// 	}
	// 	Map<String, Object> response = new HashMap<>();
	// 	response.put("tariff", result);
	// 	response.put("quantity", quantity);
	// 	if (country != null && !country.isEmpty()) response.put("country", country);
	// 	return ResponseEntity.ok(response);
	// }

	// // 3. Compare tariff prices/rates across multiple countries
	// @GetMapping("/compare")
	// public ResponseEntity<List<Map<String, Object>>> compareTariffs(
	// 		@RequestParam int quantity,
	// 		@RequestParam List<String> countries) {
	// 	List<Map<String, Object>> results = countries.stream().map(country -> {
	// 		Map<String, Double> rates = queryService.getTariffRatesByCountry(country);
	// 		double perUnitRate = rates.getOrDefault("perUnitRate", 0.0);
	// 		double totalTariff = quantity * perUnitRate;
	// 		Map<String, Object> map = new HashMap<>();
	// 		map.put("country", country);
	// 		map.put("perUnitRate", perUnitRate);
	// 		map.put("totalTariff", totalTariff);
	// 		return map;
	// 	}).toList();
	// 	return ResponseEntity.ok(results);
	// }
	// 4. Search US HTS tariff articles by keyword (returns only summary fields)
	@GetMapping("/search")
	public ResponseEntity<List<Map<String, Object>>> searchTariffArticles(@RequestParam String keyword) {
		List<Map<String, Object>> results = queryService.extractTariffSummary(keyword);
		// Sort by 'general' tariff value, highest to lowest
		List<Map<String, Object>> sortedResults = results.stream()
			.sorted((a, b) -> {
				double valA = parseTariffValue(a.get("general"));
				double valB = parseTariffValue(b.get("general"));
				return Double.compare(valB, valA); // descending
			})
			.toList();
		return ResponseEntity.ok(sortedResults);
	}

	// Helper to parse tariff value from Object (String or null)
	private static double parseTariffValue(Object value) {
		if (value == null) return Double.NEGATIVE_INFINITY;
		String str = value.toString().replaceAll("[^0-9.]+", "");
		if (str.isEmpty()) return Double.NEGATIVE_INFINITY;
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return Double.NEGATIVE_INFINITY;
		}
	}

	// 5. Compare tariff rates for a given HTS number across all countries (no keyword needed)
	@GetMapping("/compare-countries")
	public ResponseEntity<Map<String, Object>> compareCountryTariffs(@RequestParam String htsno) {
		// Use the htsno as the keyword to maximize chance of finding the correct article
		List<Map<String, Object>> results = queryService.searchTariffArticles(htsno);
		Map<String, Object> item = results.stream()
			.filter(map -> htsno.equals(map.get("htsno")))
			.findFirst()
			.orElse(null);
		if (item == null) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> countryTariffs = queryService.extractCountryTariffs(item);
		return ResponseEntity.ok(countryTariffs);
	}

	@GetMapping("/most-queried")  // TODO: should the /api/tariff mapping be moved to each part so this does not become /api/tariffs/most-queried?
	public ResponseEntity<List<String>> getMostQueriedHTSCodes() {
		List<String> results = queryService.getMostQueried();
		return ResponseEntity.ok(results);
	}
	
	// 6. Add a new query record
	@PostMapping("/queries")
	public ResponseEntity<Query> addQuery(@RequestBody Query query) {
		Query savedQuery = queryService.addQuery(query);
		return ResponseEntity.ok(savedQuery);

	}

	// 7. Filter queries by user ID
	@GetMapping("/queries")
	public ResponseEntity<List<Query>> getQueriesByUserId(@RequestParam Integer userID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		return ResponseEntity.ok(queries);
	}

	//8. Remove records from query database by queryID
	@DeleteMapping("/queries/{queryID}")
	public ResponseEntity<Void> deleteQuery(@PathVariable Long queryID) {
		queryService.deleteQuery(queryID);
		return ResponseEntity.noContent().build();
	}

	//9. Option to clear one query or all queries from the database from one user

	@DeleteMapping("/queries/user/{userID}")
	public ResponseEntity<Void> deleteQueriesByUserId(@PathVariable Integer userID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		for (Query query : queries) {
			queryService.deleteQuery(query.getQueryID());
		}
		return ResponseEntity.noContent().build();
	}

	//10. Clear one query from one user by queryID and userID
	@DeleteMapping("/queries/user/{userID}/query/{queryID}")
	public ResponseEntity<Void> deleteQueryByUserIdAndQueryId(@PathVariable Integer userID, @PathVariable Long queryID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		boolean found = false;
		for (Query query : queries) {
			if (query.getQueryID().equals(queryID)) {
				queryService.deleteQuery(queryID);
				found = true;
				break;
			}
		}
		if (!found) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}
}
