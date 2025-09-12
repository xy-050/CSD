// package controller;
// import jakarta.servlet.http.HttpSession;
// import app.model.Account;
// import service.AccountService;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;


// import jakarta.servlet.http.HttpServletRequest;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestMethod;




// @Controller
// public class AccountController {

//     private List<String> accounts;

//     @Autowired
//     private AccountService accountService;
    
//     @RequestMapping("/home", method=RequestMethod.GET)
//     public String requestMethodName(@RequestParam String param) {
//         return new String();
//     }
    
//     @GetMapping("/login")
//     public String Login(@RequestParam String param) {
//         return new String();
//     }
    
//     @GetMapping("/signup")
//     public String Signup(@RequestParam String param) {
//         return new String();
//     }

//     //todo: find out the js file name + return it 
//     @GetMapping("/viewAccount")
//     public String viewAccount(@RequestParam String param) {
//         ClassPathResource resource = new ClassPathResource("static/viewAccount.html");
//         String jsContent = Files.readString(resource.getFile().toPath());
//         return ResponseEntity.ok()
//                 .header(HttpHeaders.CONTENT_TYPE, "application/javascript")
//                 .body(jsContent);
//     }
    
//     @PostMapping("/createAccount")
//     public Account createAccount(@RequestBody Account account) {
//         return accountService.saveAccount(account);
//     }
    
//     @GetMapping
//     public List<Account> getAccounts() {
//         return accountService.getAllAccounts();
//     }
// }

package app.account;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;

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

    // Serve a JS file from resources/static/viewAccount.js
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
        return accountService.saveAccount(account);
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts() {
        return accountService.getAllAccounts();
    }
}