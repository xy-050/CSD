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

	// 1. View tariff rates (all or by country)
	@GetMapping("/rates")
	public ResponseEntity<Map<String, Double>> getTariffRates(@RequestParam(value = "country", required = false) String country) {
		if (country != null && !country.isEmpty()) {
			return ResponseEntity.ok(queryService.getTariffRatesByCountry(country));
		} else {
			return ResponseEntity.ok(queryService.getTariffRates());
		}
	}

	// 2. Calculate tariff for a quantity of goods (optionally by country)
	@GetMapping("/calculate")
	public ResponseEntity<Map<String, Object>> calculateTariff(
			@RequestParam int quantity,
			@RequestParam(value = "country", required = false) String country) {
		double result;
		if (country != null && !country.isEmpty()) {
			result = queryService.calculateTariffForGoodsByCountry(country, quantity);
		} else {
			result = queryService.calculateTariffForGoods(quantity);
		}
		Map<String, Object> response = new HashMap<>();
		response.put("tariff", result);
		response.put("quantity", quantity);
		if (country != null && !country.isEmpty()) response.put("country", country);
		return ResponseEntity.ok(response);
	}

	// 3. Compare tariff prices/rates across multiple countries
	@GetMapping("/compare")
	public ResponseEntity<List<Map<String, Object>>> compareTariffs(
			@RequestParam int quantity,
			@RequestParam List<String> countries) {
		List<Map<String, Object>> results = countries.stream().map(country -> {
			Map<String, Double> rates = queryService.getTariffRatesByCountry(country);
			double perUnitRate = rates.getOrDefault("perUnitRate", 0.0);
			double totalTariff = quantity * perUnitRate;
			Map<String, Object> map = new HashMap<>();
			map.put("country", country);
			map.put("perUnitRate", perUnitRate);
			map.put("totalTariff", totalTariff);
			return map;
		}).toList();
		return ResponseEntity.ok(results);
	}

	// 4. Add a new query record
	@PostMapping("/queries")
	public ResponseEntity<Query> addQuery(@RequestBody Query query) {
		Query savedQuery = queryService.addQuery(query);
		return ResponseEntity.ok(savedQuery);

	}

	// 5. Filter queries by user ID
	@GetMapping("/queries")
	public ResponseEntity<List<Query>> getQueriesByUserId(@RequestParam Integer userID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		return ResponseEntity.ok(queries);
	}

	//6. Remove records from query database by queryID
	@DeleteMapping("/queries/{queryID}")
	public ResponseEntity<Void> deleteQuery(@PathVariable Long queryID) {
		queryService.deleteQuery(queryID);
		return ResponseEntity.noContent().build();
	}

	//7. Option to clear one query or all queries from the database from one user

	@DeleteMapping("/queries/user/{userID}")
	public ResponseEntity<Void> deleteQueriesByUserId(@PathVariable Integer userID) {
		List<Query> queries = queryService.getQueriesByUserId(userID);
		for (Query query : queries) {
			queryService.deleteQuery(query.getQueryID());
		}
		return ResponseEntity.noContent().build();
	}

	//8. Clear one query from one user by queryID and userID
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
