package app.query;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

import app.account.Account;
import jakarta.transaction.Transactional;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {

    /**
     * Return HTS code and count, ordered by count descending.
     * Use a Pageable parameter to limit results (e.g. PageRequest.of(0,10) for top
     * 10).
     * The method returns a list of Object[] where index 0 = htsCode (String) and
     * index 1 = count (Long/Number).
     */
    @org.springframework.data.jpa.repository.Query("SELECT q.htsCode, COUNT(q) FROM Query q GROUP BY q.htsCode ORDER BY COUNT(q) DESC")
    List<java.lang.Object[]> findTopHtsCodes(org.springframework.data.domain.Pageable pageable);

    /**
     * Return top queried HTS codes with product details (description, category).
     * Uses LEFT JOIN with Product table to fetch the most recent product
     * information.
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
    List<QueryDTO> findTopQueriesWithDetails(org.springframework.data.domain.Pageable pageable);

    List<Query> findByAccount(Account account);
}
