package app.query;

import org.springframework.stereotype.Repository;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Repository
public class QueryRepository extends JpaRepository<Query, Long> {
    // Repository implementation goes here

}
