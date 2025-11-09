package app.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController controller;

    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
    }

    @Test
    void testForgotPassword() {
        // Arrange
        doNothing().when(emailService).sendPasswordResetEmail(anyString());

        // Act
        ResponseEntity<?> response = controller.forgotPassword(Map.of("email", testEmail));

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(emailService, times(1)).sendPasswordResetEmail(eq(testEmail));
    }

    @Test
    void testGetNotifications() {
        // Act
        ResponseEntity<?> response = controller.getNotifications();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testForgotPassword_MissingEmail() {
        // Act
        ResponseEntity<?> response = controller.forgotPassword(Map.of());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
