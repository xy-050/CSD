package app.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ResponseEntity<String> home(@RequestParam(required = false, defaultValue = "") String param) {
        return ResponseEntity.ok("home " + param);
    }

    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestParam(required = false, defaultValue = "") String param) {
        return ResponseEntity.ok("login " + param);
    }

    @GetMapping("/signup")
    public ResponseEntity<String> signup(@RequestParam(required = false, defaultValue = "") String param) {
        return ResponseEntity.ok("signup " + param);
    }

    /**
     * Serve a JS file from resources/static/viewAccount.js
     * 
     * @param param
     * @return
     */
    @GetMapping("/viewAccount")
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

    @PostMapping("/createAccount")
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        return accountService.getAllAccounts();
    }
}