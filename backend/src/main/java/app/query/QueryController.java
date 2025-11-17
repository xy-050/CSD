package app.query;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariffs")
public class QueryController {

	private final QueryService queryService;
	private final TariffService tariffService;

	public QueryController(QueryService queryService, TariffService tariffService) {
		this.queryService = queryService;
		this.tariffService = tariffService;
	}

	/**
	 * Searches US HTS tariff articles by keyword 
	 * 
	 * @param keyword
	 * @return
	 */
	@GetMapping("/search")
	public ResponseEntity<List<Map<String, Object>>> searchTariffArticles(@RequestParam String keyword) {
		List<Map<String, Object>> results = tariffService.searchAndSortTariffs(keyword);
		return ResponseEntity.ok(results);
	}

	/**
	 * Compare tariff rates for a given HTS number across all countries 
	 * 
	 * @param htsno
	 * @return
	 */
	@GetMapping("/countries")
	public ResponseEntity<Map<String, Object>> compareCountryTariffs(@RequestParam String htsno) {
		Map<String, Object> item = tariffService.searchByHtsNo(htsno);
		Map<String, Object> countryTariffs = tariffService.extractCountryTariffs(item);
		return ResponseEntity.ok(countryTariffs);
	}

	/**
	 * Retrieves the top 10 most queried products
	 * 
	 * @return
	 */
	@GetMapping("/most-queried") 
	public ResponseEntity<List<QueryDTO>> getMostQueriedHTSCodes() {
		List<QueryDTO> results = queryService.getMostQueried();
		return ResponseEntity.ok(results);
	}
	
	/**
	 * Adds a new query record
	 * 
	 * @param query
	 * @return
	 */
	@PostMapping("/queries")
	public ResponseEntity<Query> addQuery(@RequestBody Query query) {
		Query savedQuery = queryService.addQuery(query);
		return ResponseEntity.ok(savedQuery);
	}

	/**
	 * Filter queries by user ID
	 * 
	 * @param userID
	 * @return
	 */
	@GetMapping("/queries")
	public ResponseEntity<List<Query>> getQueriesByUserId(@RequestParam Integer userID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		return ResponseEntity.ok(queries);
	}

	/**
	 * Remove records from query database by queryID
	 * 
	 * @param queryID
	 * @return
	 */
	@DeleteMapping("/queries/{queryID}")
	public ResponseEntity<Void> deleteQuery(@PathVariable Long queryID) {
		queryService.deleteQuery(queryID);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Option to clear one query or all queries from the database from one user
	 * 
	 * @param userID
	 * @return
	 */
	@DeleteMapping("/queries/user/{userID}")
	public ResponseEntity<Void> deleteQueriesByUserId(@PathVariable Integer userID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		for (Query query : queries) {
			queryService.deleteQuery(query.getQueryID());
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * Clear one query from one user by queryID and userID
	 * 
	 * @param userID
	 * @param queryID
	 * @return
	 */
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
