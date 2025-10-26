package app.product;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findTopByHtsCodeOrderByFetchDateDesc(String htsCode); 
    Optional<Product> findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(String htsCode, LocalDate date);
}
