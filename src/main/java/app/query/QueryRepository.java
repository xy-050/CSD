package app.query;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import app.account.Account;


@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    // Repository implementation goes here
    List<Query> findByUserID(Account userID);
}
