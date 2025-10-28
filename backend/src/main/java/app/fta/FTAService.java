package app.fta;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import app.exception.FTANotFoundException;

public class FTAService {

    private final FTARepository ftaRepository;

    public FTAService(FTARepository ftaRepository) {
        this.ftaRepository = ftaRepository;
    }

    /**
     * Saves a new FTA into the database.
     * 
     * @param newFTA New FTA to save.
     */
    public void newFTA(FTA newFTA) {
        ftaRepository.save(newFTA);
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
     * Returns the FTA with a certain country. If there isn't an existing FTA with
     * the country, an FTANotFoundException is thrown.
     * 
     * @param country Target county
     * @return FTA instance
     */
    public FTA getFTAGivenCountry(String country) {
        Optional<FTA> curr = ftaRepository.findById(country);

        if (curr == null || !curr.isPresent()) {
            throw new FTANotFoundException("Error! FTA with " + country + " does not exist.");
        }

        return curr.get();
    }

    /**
     * Updates the FTA with a certain country. 
     * 
     * @param country Target country
     * @param prices Updated prices
     */
    public void updateFTA(String country, Map<String, Map<LocalDate, Double>> prices) {
        try {
            FTA curr = getFTAGivenCountry(country);
            curr.setPrices(prices);
            ftaRepository.save(curr);
        } catch (FTANotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

}
