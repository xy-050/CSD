package app.email;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.email.dto.ForgotPasswordRequest;
import app.email.dto.ResetPasswordRequest;
import app.email.dto.NotificationRequest;
import app.email.dto.ApiResponse;
import app.security.JwtUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class EmailController {

    private final EmailService emailService;
    private final JwtUtils jwtUtils;

    public EmailController(EmailService emailService, JwtUtils jwtUtils) {
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Initiates password reset process by sending reset email.
     * 
     * Possible exceptions (handled by GlobalExceptionHandler):
     * - MethodArgumentNotValidException: Invalid request body (400)
     * - AccountNotFoundException: Account doesn't exist (200 with generic message to prevent enumeration)
     * - EmailDeliveryException: Email failed to send (503)
     * - MailException: Mail server error (503)
     * - EmailServiceException: General service error (500)
     * 
     * @param request Contains username/email for password reset
     * @return Success message (always, to prevent user enumeration)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // Exceptions are thrown and handled by GlobalExceptionHandler
        emailService.sendPasswordResetEmail(request.getUsername());
        
        return ResponseEntity.ok(new ApiResponse(
            "If an account exists for this email, we've sent a reset link.",
            true
        ));
    }

    /**
     * Completes password reset using token and new password.
     * 
     * Possible exceptions (handled by GlobalExceptionHandler):
     * - MethodArgumentNotValidException: Invalid request body (400)
     * - JwtException: JWT parsing/validation error (401)
     * - InvalidResetTokenException: Token invalid or expired (400)
     * - TokenEmailMismatchException: Token email doesn't match (400)
     * - AccountNotFoundException: Account doesn't exist (404)
     * - InvalidPasswordException: Password doesn't meet requirements (400)
     * - EmailServiceException: General service error (500)
     * 
     * @param request Contains token and new password
     * @return Success or error message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Extract and validate email from token first
        String email = jwtUtils.getUserNameFromJwtToken(request.getToken());

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse("Invalid or expired token", false));
        }
        
        // Exceptions are thrown and handled by GlobalExceptionHandler
        emailService.resetPasswordWithToken(email, request.getToken(), request.getPassword());

        return ResponseEntity.ok(new ApiResponse("Password reset successful!", true));
    }

    /**
     * Sends tariff change notification email.
     * 
     * Possible exceptions (handled by GlobalExceptionHandler):
     * - MailAuthenticationException: Mailjet credentials invalid (503)
     * - MailSendException: Email send failed (503)
     * - MailParseException: Malformed email address (400)
     * - SocketTimeoutException: Mailjet connection timeout (504)
     * - IllegalArgumentException: Invalid input parameters (400)
     * 
     * @param userEmail Recipient email address
     * @param htsCode HTS code of the tariff item
     * @param oldPrice Previous tariff rate
     * @param newPrice New tariff rate
     * @return Success message
     */
    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        
        // Exceptions are thrown and handled by GlobalExceptionHandler
        emailService.sendNotificationEmail(
            request.getUserEmail(), 
            request.getHtsCode(), 
            request.getOldPrice(), 
            request.getNewPrice()
        );

        return ResponseEntity.ok(new ApiResponse(
            "Notification email sent successfully",
            true
        ));
    }
}



