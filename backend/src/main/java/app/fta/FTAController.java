package app.fta;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
public class FTAController {

    private final FTAService ftaService;

    public FTAController (FTAService ftaService) {
        this.ftaService = ftaService;
    }

    // Logic to update FTA data
    // For example, save newData to the database
    @PostMapping("/fta/update")
    public ResponseEntity<String> updateFTAData(@RequestBody String newData) {
        if (newData == null || newData.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid FTA data");
        }
        ftaService.updateFTAData(newData);
        return ResponseEntity.ok("FTA data updated successfully");
    }

    // Logic to delete FTA data by id
    @DeleteMapping("/fta/delete/{id}")
    public ResponseEntity<String> deleteFTAData(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Invalid FTA ID");
        }
        try {
            ftaService.deleteFTAData(id);
            return ResponseEntity.ok("FTA data with id " + id + " deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    
    }
    
    // Logic to list all FTA entries
    /*NEED TO UPDATE THE ftaENTRIES*/
    @GetMapping("/fta/list")
    public ResponseEntity<List<FTA>> listAllFTAEntries() {
        List<FTA> ftaEntries = ftaService.getAllFTAs();
        if (ftaEntries == null || ftaEntries.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ftaEntries);
    }

    // Logic to search FTA data based on query
    @GetMapping("/fta/search")
    public ResponseEntity<String> searchFTA(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid search query");
        }

        try {
            String result = ftaService.searchFTA(query.trim());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.internalServerError().body("Database error occurred");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    // Logic to create a new FTA entry
    @PostMapping("/fta/create")
    public ResponseEntity<String> createFTAEntry(@Valid @RequestBody FTARequest request) {
        try {
        ftaService.createFTA(request.getCountry(), request.getHtsCode(), 
                            request.getPrice(), request.getDate());
        return ResponseEntity.ok("New FTA entry created successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Create failed: " + e.getMessage());
        }
    }
}
