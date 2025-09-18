package app.query;

import org.springframework.stereotype.Repository;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    // Repository implementation goes here

}
