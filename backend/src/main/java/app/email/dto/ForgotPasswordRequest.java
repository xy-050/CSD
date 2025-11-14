package app.email.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for forgot password endpoint
 */


public class ForgotPasswordRequest {
    
    @NotBlank(message = "Username is required")
    private String username;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}