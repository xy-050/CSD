package app.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import app.security.PasswordChecker;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
     * Home endpoint to verify user identity.
     * 
     * @param userID User ID from path variable.
     * @return ResponseEntity with status and message.
     */
    @GetMapping(value = "/home/{userID}")
    public ResponseEntity<String> home(@PathVariable Integer userID) {
        // perform authentication check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (!currentUser.getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only access your own data");
        }

        return ResponseEntity.ok("home " + userID);
    }

    /**
     * Login endpoint to authenticate user.
     * 
     * @param loginAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestBody Account loginAccount) {
        return ResponseEntity.ok("Login successful for user: " + loginAccount.getUsername());
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
     * Update account endpoint to modify existing account details.
     * 
     * @param userID        User ID from path variable.
     * @param updateAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PostMapping("/updateAccount/{userID}")
    public ResponseEntity<String> updateAccount(@PathVariable Integer userID, @RequestBody Account updateAccount) {
        // perform authentication check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (!currentUser.getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own account");
        }

        Account existingAccount = accountService.getAccountByUsername(updateAccount.getUsername());

        // check if account exists
        if (existingAccount == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        // check strength of new password
        if (!PasswordChecker.isValidPassword(updateAccount.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password does not meet the minimum requirements");
        }

        // update account details-
        existingAccount.setPassword(updateAccount.getPassword());
        existingAccount.setEmail(updateAccount.getEmail());
        accountService.updateAccount(existingAccount);
        return ResponseEntity.ok("Account updated for user: " + existingAccount.getUsername());
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

    /**
     * Get all accounts endpoint for administrative purposes.
     * 
     * @return List of all accounts.
     */
    @GetMapping("/account")
    public List<Account> getAccounts() {
        return accountService.getAllAccounts();
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