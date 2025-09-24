
package app.query;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import app.account.Account;
import app.account.AccountRepository;

import java.util.List;
import java.util.Map;

@Service
public class QueryService {

	private final RestTemplate restTemplate = new RestTemplate();

	// Example: Inject the API URL from application properties
	@Value("https://data.gov.sg/api/action/datastore_search?resource_id=d_c63822ef11e6a79b8281db36b1975bef")
	private String tariffApiUrl;

	private final QueryRepository queryRepository;
	private final AccountRepository accountRepository;

	@Autowired
	public QueryService(QueryRepository queryRepository, AccountRepository accountRepository) {
		this.queryRepository = queryRepository;
		this.accountRepository = accountRepository;
	}

	/**
	 * Fetches tariff rates from an external API as a standalone feature.
	 * @return Map of rate types to their values
	 */
	public Map<String, Double> getTariffRates() {
		return restTemplate.getForObject(tariffApiUrl, Map.class);
	}

	/**
	 * Fetches tariff rates for a specific country from the external API.
	 * Assumes the API supports a country query parameter, e.g., ?country=USA
	 * @param country The country to fetch rates for
	 * @return Map of rate types to their values
	 */
	public Map<String, Double> getTariffRatesByCountry(String country) {
		String urlWithCountry = tariffApiUrl + "?country=" + country;
		return restTemplate.getForObject(urlWithCountry, Map.class);
	}

	/**
	 * Calculates the total tariff based on the quantity of goods and fetched rates.
	 * @param quantity The quantity of goods
	 * @return Calculated total tariff
	 */
	public double calculateTariffForGoods(int quantity) {
		Map<String, Double> rates = getTariffRates();
		double perUnitRate = rates.getOrDefault("perUnitRate", 0.0);
		return quantity * perUnitRate;
	}

	/**
	 * Calculates the total tariff for a specific country based on the quantity of goods.
	 * @param country The country to fetch rates for
	 * @param quantity The quantity of goods
	 * @return Calculated total tariff
	 */
	public double calculateTariffForGoodsByCountry(String country, int quantity) {
		Map<String, Double> rates = getTariffRatesByCountry(country);
		double perUnitRate = rates.getOrDefault("perUnitRate", 0.0);
		return quantity * perUnitRate;
	}

	/**
	 * Adds a new Query record to the database.
	 * @param query The Query object to be saved
	 * @return The saved Query object with generated ID
	 */	public Query addQuery(Query query) {
		return queryRepository.save(query);
	 }

	 public List<Query> getQueriesByUserId(Integer userID) {
		Account user = accountRepository.findByUserID(userID);
		if (user == null) {
			throw new IllegalArgumentException("User with ID " + userID + " not found.");
		}
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
