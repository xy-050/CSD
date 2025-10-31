package app.account;

import java.util.Map;
import java.util.Set;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import app.exception.UserConflictException;
import app.exception.UserNotFoundException;

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
            "favouriteHtsCodesCount", currentUser.getFavourites().size()
        );
        
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
        try {
            accountService.createAccount(signupAccount);
        } catch (UserConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } 
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
    public ResponseEntity<String> updatePassword(@PathVariable Integer userID, @RequestBody PasswordUpdateRequest passwordRequest) {
        try {
            accountService.changePassword(userID, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());
        } catch (UserNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } 
        
        return ResponseEntity.ok().body("Password successfully updated!");
    }

    /**
     * Deletes a user. 
     * 
     * @param userID Target user ID. 
     * @return ResponseEntity with status and message. 
     */
    @DeleteMapping("/account/{userID}")
    public ResponseEntity<String> deleteAccount(@PathVariable Integer userID) {
        try {
            accountService.deleteAccount(userID);
        } catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Successfully deleted user.");
    }

}