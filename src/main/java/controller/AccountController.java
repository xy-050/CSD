package controller;
import jakarta.servlet.http.HttpSession;
import model.Account;
import service.AccountService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;




@Controller
public class AccountController {

    private List<String> accounts;

    @Autowired
    private AccountService accountService;
    
    @RequestMapping("home", method=RequestMethod.GET)
    public String requestMethodName(@RequestParam String param) {
        return new String();
    }
    
    @GetMapping("/login")
    public String Login(@RequestParam String param) {
        return new String();
    }
    
    @GetMapping("/signup")
    public String Signup(@RequestParam String param) {
        return new String();
    }

    //todo: find out the js file name + return it 
    @GetMapping("/viewAccount")
    public String viewAccount(@RequestParam String param) {
        ClassPathResource resource = new ClassPathResource("static/viewAccount.html");
        String jsContent = Files.readString(resource.getFile().toPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/javascript")
                .body(jsContent);
    }
    
    @PostMapping("/createAccount")
    public Account createAccount(@RequestBody Account account) {
        return accountService.saveAccount(account);
    }
    
    @GetMapping
    public List<Account> getAccounts() {
        return accountService.getAllAccounts();
    }

    
}
