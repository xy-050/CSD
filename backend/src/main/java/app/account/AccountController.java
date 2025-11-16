package app.account;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/accounts")
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
    @GetMapping("/current")
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
                "favouriteHtsCodesCount", currentUser.getFavourites().size());

        return ResponseEntity.ok(userDetails);
    }

    /**
     * Signup endpoint to create a new account.
     * 
     * @param signupAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody Account signupAccount) {
        accountService.createAccount(signupAccount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Signup successful for user: " + signupAccount.getUsername());
    }

    /**
     * Update the user's username.
     * 
     * @param userID        User ID from path variable.
     * @param updateAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PutMapping("/{userID}")
    public ResponseEntity<String> updateUser(@PathVariable Integer userID, @RequestBody Account updateAccount) {
        accountService.updateDetails(userID, updateAccount);
        return ResponseEntity.ok().body("Successfully updated user!");
    }

    /**
     * Update the user's password.
     * 
     * @param userID        User ID from path variable.
     * @param updateAccount Account object from request body.
     * @return ResponseEntity with status and message.
     */
    @PutMapping("/{userID}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Integer userID,
            @RequestBody PasswordUpdateRequest passwordRequest) {
        accountService.updatePassword(userID, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());
        return ResponseEntity.ok().body("Password successfully updated!");
    }

    /**
     * Deletes a user.
     * 
     * @param userID Target user ID.
     * @return ResponseEntity with status and message.
     */
    @DeleteMapping("/{userID}")
    public ResponseEntity<String> deleteAccount(@PathVariable Integer userID) {
        accountService.deleteAccount(userID);
        return ResponseEntity.ok().body("Successfully deleted user.");
    }

    @PostMapping("/password-resets")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
        accountService.resetPassword(passwordResetRequest.getEmail(), passwordResetRequest.getNewPassword());
        return ResponseEntity.ok().body("Successfully reset password!");
    }
}