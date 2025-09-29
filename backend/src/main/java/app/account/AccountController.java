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
     * Performs an authentication check.
     * 
     * @param auth
     * @return Current user's username.
     */
    @GetMapping("/authStatus")
    public ResponseEntity<String> authStatus(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok(auth.getName());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not logged in.");
        }
    }

    /**
     * Returns the email of the current user.
     * 
     * @param username Username of the current user.
     * @return Email of the current user.
     */
    @GetMapping("/currentEmail")
    public ResponseEntity<String> currentEmail(@RequestParam String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (!currentUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only access your own account");
        }

        return ResponseEntity.ok(currentUser.getEmail());
    }

    /**
     * Returns the user ID of the current user.
     * 
     * @param username Username of the current user.
     * @return User ID of the current user.
     */
    @GetMapping("/currentID")
    public ResponseEntity<String> currentID(@RequestParam String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (currentUser != null && !currentUser.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only access your own account");
        }

        return ResponseEntity.ok("" + currentUser.getUserID());
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
    // @PostMapping("/updateUsername/{userID}")
    // public ResponseEntity<String> updateUsername(@PathVariable Integer userID, @RequestBody Account updateAccount) {
    //     // perform authentication check
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     Account currentUser = accountService.getAccountByUsername(auth.getName());
    //     if (!currentUser.getUserID().equals(userID)) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //                 .body("Access denied: You can only update your own account");
    //     }

    //     String newUsername = updateAccount.getUsername();
    //     if (newUsername.compareTo("") == 0) {
    //         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username cannot be empty.");
    //     } else if (accountService.getAccountByUsername(newUsername) != null) {
    //         return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
    //     }
    //     currentUser.setUsername(newUsername);
    //     accountService.updateAccount(currentUser);
    //     return ResponseEntity.ok("Account updated for user: " + currentUser.getUserID());
    // }

    /**
     * Update the user's email.
     * 
     * @param userID
     * @param updateAccount
     * @return
     */
    @PostMapping("/updateEmail/{userID}")
    public ResponseEntity<String> updateEmail(@PathVariable Integer userID, @RequestBody Account updateAccount) {
        // perform authentication check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account currentUser = accountService.getAccountByUsername(auth.getName());
        if (!currentUser.getUserID().equals(userID)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: You can only update your own account");
        }

        String newEmail = updateAccount.getEmail();
        if (newEmail.compareTo("") != 0)
            currentUser.setEmail(newEmail);
        accountService.updateAccount(currentUser);
        return ResponseEntity.ok("Account updated for user: " + currentUser.getUserID());
    }

    /**
     * Update the user's password.
     * 
     * @param userID
     * @param updateAccount
     * @return
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