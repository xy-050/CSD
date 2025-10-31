package app.favourites;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FavouritesController {

    private final FavouritesService favouritesService;

    public FavouritesController(FavouritesService favouritesService) {
        this.favouritesService = favouritesService;
    }

    @PostMapping("/account/{userID}/favourites")
    public ResponseEntity<?> addFavourite(@PathVariable Integer userID, @RequestParam String htsCode) {
        favouritesService.addFavourite(userID, htsCode);
        return ResponseEntity.ok("Added favourite: " + htsCode);
    }

    @DeleteMapping("/account/{userID}/favourites")
    public ResponseEntity<?> removeFavourite(@PathVariable Integer userID, @RequestParam String htsCode) {
        favouritesService.removeFavourite(userID, htsCode);
        return ResponseEntity.ok("Removed favourite: " + htsCode);
    }

    @GetMapping("/account/{userID}/favourites")
    public ResponseEntity<Set<String>> getFavourites(@PathVariable Integer userID) {
        return ResponseEntity.ok(favouritesService.getFavouritesHtsCodes(userID));
    }

}