package app.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import app.exception.UserConflictException;
import app.exception.UserNotFoundException;
import app.security.PasswordChecker;

@RestController
public class AccountController {

    private final AccountService accountService;

    /**
     * Constructor-based injection.
     * 
     * @param accountService Service dependency.
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Returns the current user's user ID, username, email, roles and the number of
     * favourited HTS codes.
     * 
     * @return User details
     */
    @GetMapping("/currentUserDetails")
    public ResponseEntity<Map<String, Object>> getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> userDetails = Map.of(
                "userId", currentUser.getUserID(),
                "username", currentUser.getUsername(),
                "email", currentUser.getEmail(),
                "roles", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                "favouriteHtsCodesCount", currentUser.getFavouriteHtsCodes().size());

        return ResponseEntity.ok(userDetails);
    }

    /**
     * Signup endpoint to create a new account.
     * 
     * @param signupAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Account signupAccount) {
        Account existingAccount = accountService.getAccountByUsername(signupAccount.getUsername());

        // check if account already exists
        if (existingAccount != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        // check strength of password
        if (!PasswordChecker.isValidPassword(signupAccount.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password does not meet the minimum requirements");
        }

        // create account
        accountService.createAccount(signupAccount);
        return ResponseEntity.ok("Signup successful for user: " + signupAccount.getUsername());
    }

    /**
     * Update the user's username.
     * 
     * @param userID        User ID from path variable.
     * @param updateAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PostMapping("/updateUser/{userID}")
    public ResponseEntity<String> updateUser(@PathVariable Integer userID, @RequestBody Account updateAccount) {
        try {
            accountService.updateDetails(userID, updateAccount);
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        return ResponseEntity.ok().body("Successfully updated user!");
    }

    /**
     * Update the user's password.
     * 
     * @param userID        User ID from path variable.
     * @param updateAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PostMapping("/updatePassword/{userID}")
    public ResponseEntity<String> updatePassword(@PathVariable Integer userID, @RequestBody Account updateAccount) {
        // perform authentication check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (!currentUser.getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own account");
        }

        String newPassword = updateAccount.getPassword();

        // check strength of new password
        if (!PasswordChecker.isValidPassword(newPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password does not meet the minimum requirements");
        }
        currentUser.setPassword(newPassword);
        accountService.updateAccount(currentUser);
        return ResponseEntity.ok("Account updated for user: " + currentUser.getUserID());
    }

    /**
     * View account endpoint to serve a JavaScript resource.
     * 
     * @param userID User ID from path variable.
     * @param param  Optional query parameter.
     * @return ResponseEntity with JavaScript content or error status.
     */
    @GetMapping("/account/{userID}")
    public ResponseEntity<String> viewAccount(@PathVariable Integer userID,
            @RequestParam(required = false, defaultValue = "") String param) {
        // perform authentication check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (!currentUser.getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only view your own account");
        }

        Resource resource = new ClassPathResource("static/viewAccount.js");
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("application/javascript"))
                    .body(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "viewAccount.js not found", e);
        }
    }

    /*
     * Managing favourite hts codes saved by users
     */
    @PostMapping("/account/{userID}/favourites")
    public ResponseEntity<?> addFavourite(@PathVariable Integer userID, @RequestParam String htsCode) {
        accountService.addFavouriteHtsCode(userID, htsCode);
        return ResponseEntity.ok("Added favourite: " + htsCode);
    }

    @DeleteMapping("/account/{userID}/favourites")
    public ResponseEntity<?> removeFavourite(@PathVariable Integer userID, @RequestParam String htsCode) {
        accountService.removeFavouriteHtsCode(userID, htsCode);
        return ResponseEntity.ok("Removed favourite: " + htsCode);
    }

    @GetMapping("/account/{userID}/favourites")
    public ResponseEntity<Set<String>> getFavourites(@PathVariable Integer userID) {
        return ResponseEntity.ok(accountService.getFavouriteHtsCodes(userID));
    }

}