package app.query;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariffs")
public class QueryController {

	private final QueryService queryService;

	public QueryController(QueryService queryService) {
		this.queryService = queryService;
	}

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

	@GetMapping("/most-queried") 
	public ResponseEntity<List<QueryDTO>> getMostQueriedHTSCodes() {
		List<QueryDTO> results = queryService.getMostQueried();
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
