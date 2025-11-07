package app.email; 

import javax.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
public class emailController {
    private emailService emailService;

    public emailController(emailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/forget-password")
    public void PasswordReset() {
        return ResponseEntity.ok();
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        return ResponseEntity.ok();
    }

    @PostMapping("/send-password-reset-email")
    public void sendPasswordReset(@RequestParam String to) {
        try {
            emailService.sendEmail(to, subject, body);
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Email sent successfully! Please check your inbox and click on the link within ");
    }



}