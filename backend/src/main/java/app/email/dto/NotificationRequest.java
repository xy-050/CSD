package app.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * Request DTO for sending notification emails
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    
    @NotBlank(message = "User email is required")
    @Email(message = "Must be a valid email address")
    private String userEmail;
    
    @NotBlank(message = "HTS code is required")
    private String htsCode;
    
    @NotBlank(message = "Old price is required")
    private String oldPrice;
    
    @NotBlank(message = "New price is required")
    private String newPrice;

    // Constructor, getters, and setters...
}
