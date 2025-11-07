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

    // Logic to get FTA status
    @GetMapping("/fta/status")
    public ResponseEntity<String> getFTAStatus() {
        String status = "FTA system is operational";
        return ResponseEntity.ok(status);
    }

    // Logic to update FTA data
    // For example, save newData to the database
    @PostMapping("/fta/update")
    public ResponseEntity<String> updateFTAData(@RequestBody String newData) {
        return ResponseEntity.ok("FTA data updated successfully");
    }

    // Logic to delete FTA data by id
    @DeleteMapping("/fta/delete/{id}")
    public ResponseEntity<String> deleteFTAData(@PathVariable Long id) {
        return ResponseEntity.ok("FTA data with id " + id + " deleted successfully");
    }
    
    // Logic to list all FTA entries
    /*NEED TO UPDATE THE ftaENTRIES*/
    @GetMapping("/fta/list")
    public ResponseEntity<List<String>> listAllFTAEntries() {
        List<String> ftaEntries = List.of("FTA Entry 1", "FTA Entry 2", "FTA Entry 3");
        return ResponseEntity.ok(ftaEntries);
    }

    // Logic to search FTA data based on query
    @GetMapping("/fta/search")
    public ResponseEntity<String> searchFTA(@RequestParam String query) {
        String result = "Search results for query: " + query;
        return ResponseEntity.ok(result);
    }

    // Logic to create a new FTA entry
    @PostMapping("/fta/create")
    public ResponseEntity<String> createFTAEntry(@RequestBody String entryData) {
        return ResponseEntity.ok("New FTA entry created successfully");
    }
}
