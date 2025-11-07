package app.fta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FTARepository extends JpaRepository<FTA, Long> {
    public Optional<List<FTA>> findByCountry(String country);
    public Optional<List<FTA>> findByCountryAndHtsCode(String country, String htsCode);
}
