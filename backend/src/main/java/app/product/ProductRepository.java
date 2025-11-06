package app.product;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, ProductId> {
    Optional<Product> findTopByHtsCodeOrderByFetchDateDesc(String htsCode); 
    Optional<Product> findTopByHtsCodeAndFetchDateLessThanEqualOrderByFetchDateDesc(String htsCode, LocalDate date);
    Optional<List<Product>> findByCategory(String category);
    Optional<List<Product>> findByHtsCodeStartingWith(String htsCode);
    Optional<List<Product>> findByCategoryIgnoreCaseOrHtsCodeStartingWith(String category, String htsCodePrefix);
    Optional<List<Product>> findByHtsCode(String htsCode);
}
