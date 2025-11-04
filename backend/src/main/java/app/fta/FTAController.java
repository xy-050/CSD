

@RestController
public class FTAController {
    /*whenever the client makes a call to FTA*/
    
    @GetMapping("/fta/status")
    public ResponseEntity<String> getFTAStatus() {
        // Logic to get the status of FTA
        String status = "FTA is operational";
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/fta/data")
    public ResponseEntity<String> getFTAData() {    
        // Logic to get FTA data
        String data = "FTA data payload";
        return ResponseEntity.ok(data);
    }

}
