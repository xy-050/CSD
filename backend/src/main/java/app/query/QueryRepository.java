package app.query;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    // Repository implementation goes here

}
