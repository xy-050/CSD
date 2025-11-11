package app.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.account.Account;
import app.account.AccountService;
import app.security.JwtUtils;

import java.util.Map;

@RestController
public class EmailController {

    @Autowired
    private final JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountService accountService; 

    public EmailController(EmailService emailService, JwtUtils jwtUtils, AccountService accountService) {
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.accountService = accountService;
    }

    /**
     * Step 1: User requests password reset by providing email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            // Send reset email (will check if user exists inside service)
            String token = emailService.sendPasswordResetEmail(email);

            // Always return success to prevent email enumeration attacks
            return ResponseEntity.ok().body(
                    Map.of("message", "If an account exists for this email, we've sent a reset link.",
                            "token", token));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Failed to send reset email. Please try again later."));
        }
    }

    /**
     * Step 2: User submits token and new password to reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        // Validate inputs
        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email, token, and password are required"));
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Password must be at least 8 characters long"));
        }

        try {
            // get email from token
            String email = jwtUtils.getUserNameFromJwtToken(token);

            if (email == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid or expired token"));
            }

            // Validate token and reset password
            boolean success = emailService.resetPasswordWithToken(email, token, newPassword);

            if (success) {
                return ResponseEntity.ok().body(
                        Map.of("message", "Password reset successful!"));
            } else {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid or expired token"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("message", "Failed to reset password. Please try again."));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(
            @RequestParam int userId,
            @RequestParam String htsCode,
            @RequestParam String price) {
        try {
            Account account = accountService.getAccountByUserID(userId);
            String useremail = account.getEmail();
            boolean success = emailService.sendNotificationEmail(useremail, htsCode, price);

            if (success) {
                return ResponseEntity.ok().body(Map.of("message", "Notifications endpoint"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(500).body(
                Map.of("message", "Failed to reset password. Please try again."));
    }

}