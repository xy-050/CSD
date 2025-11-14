package app.email;

import app.security.JwtUtils;
import app.account.Account;
import app.account.AccountService;
import app.exception.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import javax.security.auth.login.AccountNotFoundException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JwtUtils jwtUtils;
    private final JavaMailSender mailSender;
    private final AccountService accountService;

    @Value("${app.email.from:no-reply@tariffics.org}")
    private String fromAddress;

    @Value("${app.frontend.url:https://tariffics.org}")
    private String frontendUrl;

    private static final String PASSWORD_RESET_SUBJECT = "Password Reset Request";
    private static final String NOTIFICATION_SUBJECT = "Notification from Tariffics";

    public EmailService(JavaMailSender mailSender, JwtUtils jwtUtils, AccountService accountService) {
        this.mailSender = mailSender;
        this.jwtUtils = jwtUtils;
        this.accountService = accountService;
    }

    /**
     * Sends password reset email to user.
     * Throws AccountNotFoundException if user doesn't exist (caught by global handler).
     * 
     * @param username Username or email of the account
     * @throws AccountNotFoundException if account doesn't exist
     * @throws EmailDeliveryExceptionException if email fails to send
     */
    public void sendPasswordResetEmail(String username) {
        Account account = accountService.getAccountByUsername(username);
        
        if (account == null) {
            logger.info("Password reset requested for non-existent user: {}", username);
            throw new UserNotFoundException(username);
        }

        String email = account.getEmail();
        
        try {
            // Create a minimal UserDetails for token generation
            UserDetails userDetails = User.builder()
                    .username(email)
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();
            
            String token = jwtUtils.generateRefreshToken(userDetails);
            String body = buildPasswordResetEmailBody(token);
            
            sendEmail(email, PASSWORD_RESET_SUBJECT, body);
            logger.info("Password reset email sent to: {}", email);
            
        } catch (MailException e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            throw new EmailDeliveryExceptionException("Failed to send password reset email", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending password reset email for: {}", username, e);
            throw new EmailServiceException("Error sending password reset email", e);
        }
    }

    /**
     * Validates reset token and resets password if valid.
     * 
     * @param email User's email
     * @param token Reset token
     * @param newPassword New password to set
     * @return true if password was successfully reset
     * @throws InvalidResetTokenException if token is invalid or expired
     * @throws TokenEmailMismatchException if token email doesn't match
     * @throws UserNotFoundException if account doesn't exist
     */
    public boolean resetPasswordWithToken(String email, String token, String newPassword) {
        // Validate token
        validateResetToken(email, token);
        
        // Get account
        Account account = accountService.getAccountByEmail(email);
        if (account == null) {
            logger.warn("Password reset attempted for non-existent email: {}", email);
            throw new UserNotFoundException(email);
        }

        try {
            accountService.resetPassword(email, newPassword);
            logger.info("Password successfully reset for: {}", email);
            return true;
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid password provided for: {}", email);
            throw new InvalidPasswordException(e.getMessage());
        } catch (Exception e) {
            logger.error("Error resetting password for: {}", email, e);
            throw new EmailServiceException("Failed to reset password", e);
        }
    }

    /**
     * Sends notification email about tariff changes.
     * 
     * @param userEmail Recipient email
     * @param htsCode HTS code of the product
     * @param oldPrice Previous tariff rate
     * @param newPrice New tariff rate
     * @return true if email was sent successfully
     * @throws EmailDeliveryExceptionException if email fails to send
     */
    public boolean sendNotificationEmail(String userEmail, String htsCode, String oldPrice, String newPrice) {
        try {
            String body = buildNotificationEmailBody(htsCode, oldPrice, newPrice);
            sendEmail(userEmail, NOTIFICATION_SUBJECT, body);
            
            logger.info("Notification email sent to: {} for HTS code: {}", userEmail, htsCode);
            return true;
            
        } catch (MailException e) {
            logger.error("Failed to send notification email to: {}", userEmail, e);
            throw new EmailDeliveryExceptionException("Failed to send notification email", e);
        } catch (Exception e) {
            logger.error("Unexpected error sending notification to: {}", userEmail, e);
            throw new EmailServiceException("Error sending notification email", e);
        }
    }

    /**
     * Validates reset token matches the email and hasn't expired.
     * 
     * @param email User's email
     * @param token Reset token to validate
     * @throws JwtException if token is invalid, expired, or email mismatch
     */
    private void validateResetToken(String email, String token) {
        if (!jwtUtils.validateToken(token)) {
            logger.warn("Token validation failed for email: {}", email);
            throw new JwtException("Token is invalid or expired");
        }

        String tokenEmail = jwtUtils.getUserNameFromJwtToken(token);
        
        if (tokenEmail == null) {
            logger.warn("Token has no subject for email: {}", email);
            throw new JwtException("Token is malformed - missing subject");
        }
        
        if (!tokenEmail.equals(email)) {
            logger.warn("Token email mismatch - Token: {}, Provided: {}", tokenEmail, email);
            throw new JwtException("Token email does not match provided email");
        }
    }

    /**
     * Sends an email using configured mail sender.
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     * @throws MailException if email fails to send
     */
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        logger.debug("Email sent to: {}", to);
    }

    /**
     * Builds password reset email body.
     */
    private String buildPasswordResetEmailBody(String token) {
        return String.format(
            "You requested a password reset for your Tariff-ic account.\n\n" +
            "Your reset token is:\n\n%s\n\n" +
            "Enter this token on the password reset page: %s/reset-password\n\n" +
            "This token will expire in 7 days.\n\n" +
            "If you did not request this reset, please ignore this email.\n\n" +
            "Best regards,\nThe Tariffics Team",
            token, frontendUrl
        );
    }

    /**
     * Builds notification email body for tariff changes.
     */
    private String buildNotificationEmailBody(String htsCode, String oldPrice, String newPrice) {
        return String.format(
            "Hello!\n\n" +
            "The tariff item %s has been updated:\n" +
            "Previous rate: %s\n" +
            "New rate: %s\n\n" +
            "View details: %s/product/%s\n\n" +
            "Best regards,\nThe Tariffics Team",
            htsCode, oldPrice, newPrice, frontendUrl, htsCode
        );
    }
}