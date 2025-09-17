package app.query;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface QueryRepository extends JpaRepository <Query, Long> {

}
