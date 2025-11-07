package app.fta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import app.exception.FTANotFoundException;

@Service
public class FTAService {

    private final FTARepository ftaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate; 

    public FTAService(FTARepository ftaRepository) {
        this.ftaRepository = ftaRepository;
    }

    /**
     * Creates a new FTA with a certain country.
     * 
     * @param country Target country
     * @param prices  Updated prices
     */
    public void createFTA(String country, String htsCode, double price, LocalDate date) {
        ftaRepository.save(new FTA(country, htsCode, price, date));
    }

    /**
     * Returns all the FTAs that the US has.
     * 
     * @return List containing all the FTAs
     */
    public List<FTA> getAllFTAs() {
        return ftaRepository.findAll();
    }

    /**
     * Returns all the FTAs with a certain country. If there isn't an existing FTA
     * with
     * the country, an FTANotFoundException is thrown.
     * 
     * @param country Target county
     * @return FTA instance
     */
    public List<FTA> getFTAGivenCountry(String country) {
        Optional<List<FTA>> ftas = ftaRepository.findByCountry(country);

        if (ftas == null || !ftas.isPresent()) {
            throw new FTANotFoundException("FTA with " + country + " does not exist");
        }

        return ftas.get();
    }

    /*Prepared statement: remove if fta isnt linked to account -> remove in FTAController too*/
    public String searchFTA(String query) {
        // Only allow alphanumeric and space characters (adjust for your use case)
        if (!query.matches("^[a-zA-Z0-9\\s]+$")) {
            throw new IllegalArgumentException("Invalid characters in query");
        }

        // Use parameterized (prepared) query — NEVER concatenate strings
        String sql = "SELECT name FROM fta_table WHERE name LIKE ?";
        List<String> results = jdbcTemplate.queryForList(sql, new Object[]{"%" + query + "%"}, String.class);

        return String.join(", ", results);
    }
}
