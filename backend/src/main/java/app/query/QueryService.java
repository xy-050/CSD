package app.query;

import org.springframework.stereotype.Service;
import app.account.Account;
import app.account.AccountService;
import app.exception.QueryNotFoundException;

import org.springframework.data.domain.PageRequest;

import java.util.List;

@Service
public class QueryService {

    private final AccountService accountService;

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
     * Returns the most queried products with details (HTS code, description,
     * category, count).
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

    /**
     * Returns all the queries made by a user
     * 
     * @param userID
     * @return
     */
    public List<Query> getQueriesByUserId(Integer userID) {
        Account user = accountService.getAccountByUserID(userID);
        return queryRepository.findByAccount(user);
    }

    /**
     * Deletes a query
     * 
     * @param queryID
     */
    public void deleteQuery(Long queryID) {
        if (!queryRepository.existsById(queryID)) {
            throw new QueryNotFoundException("Query with ID " + queryID + " not found.");
        } else {
            queryRepository.deleteById(queryID);
        }
    }
}
