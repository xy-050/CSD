package app.query;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import app.account.Account;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    // TODO: should we rename our Query class so that we can just use the Query annotation?
    // @org.springframework.data.jpa.repository.Query("SELECT q.htsCode FROM Query q GROUP BY q.htsCode HAVING COUNT(*) = (SELECT COUNT(*) FROM Query q2 GROUP BY q2.htsCode ORDER BY COUNT(*) DESC LIMIT 1)")
    // public List<String> findMostQueried();
    // List<Query> findByUserID(Account userID);

    /**
     * Return HTS code and count, ordered by count descending.
     * Use a Pageable parameter to limit results (e.g. PageRequest.of(0,10) for top 10).
     * The method returns a list of Object[] where index 0 = htsCode (String) and index 1 = count (Long/Number).
     */
    @org.springframework.data.jpa.repository.Query("SELECT q.htsCode, COUNT(q) FROM Query q GROUP BY q.htsCode ORDER BY COUNT(q) DESC")
    public java.util.List<java.lang.Object[]> findTopHtsCodes(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Return top queried HTS codes with product details (description, category).
     * Uses LEFT JOIN with Product table to fetch the most recent product information.
     */
    @org.springframework.data.jpa.repository.Query("""
            SELECT new app.query.QueryDTO(
                q.htsCode,
                COALESCE(p.description, 'No description available'),
                COALESCE(p.category, 'Unknown'),
                COUNT(q)
            )
            FROM Query q
            LEFT JOIN Product p ON p.htsCode = q.htsCode
            WHERE p.fetchDate IS NULL OR p.fetchDate = (
                SELECT MAX(p2.fetchDate)
                FROM Product p2
                WHERE p2.htsCode = q.htsCode
            )
            GROUP BY q.htsCode, p.description, p.category
            ORDER BY COUNT(q) DESC
            """)
    java.util.List<QueryDTO> findTopQueriesWithDetails(org.springframework.data.domain.Pageable pageable);
    
    List<Query> findByUserID(Account userID);
}
