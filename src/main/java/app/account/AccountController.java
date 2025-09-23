package app.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import app.utility.PasswordChecker;

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

    // @GetMapping(value = "/home")
    @GetMapping(value = "/home/{id}")
    public ResponseEntity<String> home(@RequestParam(required = false, defaultValue = "") String param) {
        return ResponseEntity.ok("home " + param);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Account loginAccount) {
        Account account = accountService.getAccountByUsername(loginAccount.getUsername());

        // check if account exists and password matches
        if (account == null || !account.getPassword().equals(loginAccount.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        return ResponseEntity.ok("Login successful for user: " + account.getUsername());
    }

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

    // @PostMapping("/updateAccount")
    @PostMapping("/updateAccount/{id}")
    public ResponseEntity<String> updateAccount(@RequestBody Account updateAccount) {
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

        // update account details
        existingAccount.setPassword(updateAccount.getPassword());
        existingAccount.setEmail(updateAccount.getEmail());
        accountService.updateAccount(existingAccount);
        return ResponseEntity.ok("Account updated for user: " + existingAccount.getUsername());
    }

    // @GetMapping("/viewAccount")
    @GetMapping("/account/{id}")
    public ResponseEntity<String> viewAccount(@RequestParam(required = false, defaultValue = "") String param) {
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

    // @GetMapping("/accounts")
    @GetMapping("/account")
    public List<Account> getAccounts() {
        return accountService.getAllAccounts();
    }
}