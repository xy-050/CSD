package app.fta;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

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

        if (ftas.isEmpty()) {
            throw new FTANotFoundException("FTA with " + country + " does not exist");
        }

        return ftas.get();
    }


   
    /**
     * Updates FTA data.
     * @param newData New FTA data in comma-separated format: id,country,htsCode,price,date
     * @return void
    */
    public void updateFTAData(String newData) {
        try {
            String[] parts = newData.split(",");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid update format. Expected: id,country,htsCode,price,date");
            }
            
            Long id = Long.parseLong(parts[0].trim());
            String country = parts[1].trim();
            String htsCode = parts[2].trim();
            double price = Double.parseDouble(parts[3].trim());
            LocalDate date = LocalDate.parse(parts[4].trim());
            
            FTA fta = ftaRepository.findById(id)
                .orElseThrow(() -> new FTANotFoundException("FTA with id " + id + " does not exist"));
            
            // Update fields
            fta.setCountry(country);
            fta.setHtsCode(htsCode);
            fta.setPrice(price);
            fta.setDate(date);
            
            ftaRepository.save(fta);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in update data");
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to update FTA data: " + e.getMessage());
        }

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


}
