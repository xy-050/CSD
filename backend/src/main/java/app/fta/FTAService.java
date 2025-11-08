package app.fta;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import app.favourites.FavouritesService;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import app.exception.FTANotFoundException;

@Service
public class FTAService {

    // private final FavouritesService favouritesService;

    private final FTARepository ftaRepository;

    // public FTAService(FTARepository ftaRepository, FavouritesService
    // favouritesService) {
    // this.ftaRepository = ftaRepository;
    // this.favouritesService = favouritesService;
    // }

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
     * Updates FTA data.
     * 
     * @param newData New FTA data in comma-separated format:
     *                id,country,htsCode,price,date
     * @return void
     */
    // public void updateFTAData(String newData) {
    // try {
    // String[] parts = newData.split(",");
    // if (parts.length != 5) {
    // throw new IllegalArgumentException("Invalid update format. Expected:
    // id,country,htsCode,price,date");
    // }

    // Long id = Long.parseLong(parts[0].trim());
    // String country = parts[1].trim();
    // String htsCode = parts[2].trim();
    // double price = Double.parseDouble(parts[3].trim());
    // LocalDate date = LocalDate.parse(parts[4].trim());

    // FTA fta = ftaRepository.findById(id)
    // .orElseThrow(() -> new FTANotFoundException("FTA with id " + id + " does not
    // exist"));

    // // Update fields
    // fta.setCountry(country);
    // fta.setHtsCode(htsCode);
    // fta.setPrice(price);
    // fta.setDate(date);

    // ftaRepository.save(fta);
    // } catch (NumberFormatException e) {
    // throw new IllegalArgumentException("Invalid number format in update data");
    // } catch (Exception e) {
    // throw new IllegalArgumentException("Failed to update FTA data: " +
    // e.getMessage());
    // }
    // }

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
