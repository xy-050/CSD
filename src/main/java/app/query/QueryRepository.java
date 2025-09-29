package app.query;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    // TODO: should we rename our Query class so that we can just use the Query annotation?
    @org.springframework.data.jpa.repository.Query("SELECT q.htsCode FROM Query q GROUP BY q.htsCode HAVING COUNT(*) = (SELECT COUNT(*) FROM Query q2 GROUP BY q2.htsCode ORDER BY COUNT(*) DESC LIMIT 1)")
    public List<String> findMostQueried();
}
