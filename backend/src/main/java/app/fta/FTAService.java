package app.fta;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import app.exception.FTANotFoundException;

@Service
public class FTAService {

    private final FTARepository ftaRepository;

    public FTAService(FTARepository ftaRepository) {
        this.ftaRepository = ftaRepository;
    }

    /**
     * Creates a new FTA with a certain country.
     * 
     * @param country Target country
     * @param prices  Updated prices
     */
    public void createFTA(String country, String htsCode, String price, LocalDate date) {
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

        if (ftas.isEmpty()) {
            throw new FTANotFoundException("FTA with " + country + " does not exist");
        }

        return ftas.get();
    }

    /**
     * Deletes FTA data by ID.
     * 
     * @param id FTA ID to delete
     */
    public void deleteFTAData(Long id) {
        if (!ftaRepository.existsById(id)) {
            throw new FTANotFoundException("FTA with id " + id + " does not exist");
        }
        ftaRepository.deleteById(id);
    }

    /**
     * Returns the future prices of a product for a specific country.
     * 
     * @param country Target country
     * @param htsCode HTS code corresponding to target product
     * @return Map of date to price 
     */
    public Map<LocalDate, String> getFuturePrices(String country, String htsCode) {
        Optional<List<FTA>> ftas = ftaRepository.findByCountryAndHtsCode(country, htsCode);

        if (!ftas.isPresent()) {
            return new HashMap<>();
        }

        return ftas.get().stream()
                .filter(fta -> fta.getDate().isAfter(LocalDate.now()))
                .collect(Collectors.toMap(FTA::getDate, FTA::getPrice));
    }
}
