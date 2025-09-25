package app.email;

// Importing required classes
import app.account.Account;
import app.account.AccountService;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class EmailController {
    //Autowiring the EmailService class
    @Autowired private EmailService emailService;
    @Autowired private AccountService accountService;

    //Static fields
    public static final SecureRandom secureRandom = new SecureRandom(); 
    public static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); 

    /**Account Password Reset Email 
    user clicks on forget password -> enter email -> check if email exists in db
    if not, redirect to /signup */
    @GetMapping("/forgetPassword")
    public ResponseEntity<String> PasswordReset(@RequestParam String email) {
    
        //check if email exists in db
        Account account = accountService.getAccountByEmail(email);

        //if not, redirect to /signup
        if (account == null) {
            return ResponseEntity
            .status(302)
            .header("Location", "/signup")
            .build();
        }
        
        //generate and update token
        String newToken = generateNewToken(); 
        String resetLink = "http://localhost:8080/resetPassword?token=" + newToken;
        emailService.updateToken(email, newToken);

        //email content 
        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Password Reset Request");
        details.setMsgBody("Click the link to reset your password: " + resetLink);
        String status = emailService.sendSimpleMail(details);

        //change the display page to message 
        return ResponseEntity.ok("Please check your email for the link");
    }
    /*Controller calls the account method to update the password */
    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String token, @RequestParam String newPassword) {

        Email emailEntry = emailService.getUserByEmail(email);

        //check if token exists and is not expired
        if (emailEntry == null || System.currentTimeMillis() > emailEntry.getExpirationdate()) {
            return ResponseEntity.status(400).body("Invalid or expired token");
        }
        //if valid, update password 
        Account account = accountService.getAccountByEmail(emailEntry.getEmail());
        if (account == null) {
            return ResponseEntity.status(404).body("Account not found");
        }

        accountService.updatePassword(account.getUserID(), newPassword);

        //delete entry once password is reset 
        emailService.deleteByID(emailEntry.getTempID());
        return ResponseEntity.ok("Password has been reset successfully");
    }

    //Method to generate token
    private static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    //Password reset page
    @GetMapping("")

    

}

/*TO DO: 
 * NEED TO SOMEHOW CREATE AN EXPIRATION TIME FOR THE TOKEN -> DELETE ENTRY USING REPO 
 
*/

/*I think what email controller does right now is delete the entire entry so they have to type in 
 * their email every time to get a new token. Possible ammendment: add a button called 
 * "not working? click here to resend link" that just sends the email again + new token 
 * [not that impt, can add l8r]
*/