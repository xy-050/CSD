package app.email;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestEmailController {

    private final emailService emailService;

    public TestEmailController(emailService emailService) {
        this.emailService = emailService;
    }

    // Example: GET /send-email?to=someone@example.com&subject=Hello&body=Hi
    @GetMapping("/send-email")
    public String sendEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {

        try {
            emailService.sendEmail(to, subject, body);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
}

