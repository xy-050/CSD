package app.email;

import app.security.JwtUtils;
import app.account.Account;
import app.account.AccountRepository;
import app.product.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class EmailService {

    private final JwtUtils jwtUtils;
    private final JavaMailSender mailSender;
    private final AccountRepository accountRepository;
    
    @Autowired
    public ProductService productService; 
    
    private String fromAddress = "no-reply@tariffics.org";
    private String PasswordResetSubject = "Password Reset Request";
    private String frontendUrl = "http://localhost:3000/reset-password"; // Update to your frontend URL
    private String notificationSubject = "Notification from Tariffics";

    /**
     * Constructor-based injection.
     */
    @Autowired
    public EmailService(JavaMailSender mailSender, JwtUtils jwtUtils, AccountRepository accountRepository) {
        this.mailSender = mailSender;
        this.jwtUtils = jwtUtils;
        this.accountRepository = accountRepository;
    }

    /**
     * Helper method to send email
     */
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Email sent via SES SMTP to: " + to);
    }

    /**
     * Send password reset email with token
     */
    public void sendPasswordResetEmail(String userEmail) {
        // Create authentication object with email
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userEmail, null, Collections.emptyList()
        );

        // Generate JWT token
        String token = jwtUtils.generateJwtToken(auth);

        // Build email body with token
        String body = "You requested a password reset for your Tariff-ic account.\n\n" +
                     "Your reset token is:\n\n" + token + "\n\n" +
                     "Enter this token on the password reset page: " + frontendUrl + "\n\n" +
                     "This token will expire in 24 hours.\n\n" +
                     "If you did not request this reset, please ignore this email.\n\n" +
                     "Best regards,\nThe Tariffics Team";

        sendEmail(userEmail, PasswordResetSubject, body);
    }

    /**
     * Validate the reset token and return the email if valid.
     * The actual password reset will be handled by AccountService.
     * 
     * @return The email from the token if valid, null otherwise
     */
    public String validateResetToken(String email, String token) {
        try {
            // Validate the JWT token
            if (!jwtUtils.validateToken(token)) {
                System.out.println("Invalid token");
                return null;
            }

            // Extract email from token
            String tokenEmail = jwtUtils.getUserNameFromJwtToken(token);
            
            // Verify the email from token matches the provided email
            if (!tokenEmail.equals(email)) {
                System.out.println("Email mismatch: token=" + tokenEmail + ", provided=" + email);
                return null;
            }

            // Verify user exists
            Account userOpt = accountRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                System.out.println("User not found: " + email);
                return null;
            }

            System.out.println("Token validated successfully for: " + email);
            return email;
            
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Email notifications sent every Monday (refer to Product Service)
     */
    public void sendNotificationEmail(String userEmail, String htsCode, String oldPrice, String newPrice) {
        String body = String.format(
            "Hello!\n\n" +
            "The tariff item %s has been updated:\n" +
            "Previous rate: %s\n" +
            "New rate: %s\n\n" +
            "View details: https://tariffics.org/product/%s\n\n" +
            "Best regards,\nThe Tariffics Team",
            htsCode, oldPrice, newPrice, htsCode
        );
        
        sendEmail(userEmail, notificationSubject, body);
    }
}

