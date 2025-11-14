package app.fta;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class FTAController {

    private final FTAService ftaService;

    public FTAController (FTAService ftaService) {
        this.ftaService = ftaService;
    }

    // Logic to delete FTA data by id
    @DeleteMapping("/fta/{id}")
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
}
