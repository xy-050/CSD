package app.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {

    @Mock
    private emailService emailService;

    @InjectMocks
    private emailController controller;

    private String testEmail;
    private String testSubject;
    private String testBody;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testSubject = "Password Reset";
        testBody = "Click here to reset your password";
    }

    @Test
    void testPasswordReset() {
        // Act
        ResponseEntity<?> response = controller.PasswordReset();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
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
    void testSendPasswordReset_Success() throws MessagingException {
        // Arrange
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<?> response = controller.sendPasswordReset(testEmail);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Email sent successfully"));
        
        // Verify that emailService.sendEmail was called once
        verify(emailService, times(1)).sendEmail(eq(testEmail), anyString(), anyString());
    }

    @Test
    void testSendPasswordReset_MessagingException() throws MessagingException {
        // Arrange
        String errorMessage = "Failed to send email";
        doThrow(new MessagingException(errorMessage))
            .when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<?> response = controller.sendPasswordReset(testEmail);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
        
        // Verify that emailService.sendEmail was called once
        verify(emailService, times(1)).sendEmail(eq(testEmail), anyString(), anyString());
    }

    @Test
    void testSendPasswordReset_WithNullEmail() throws MessagingException {
        // Arrange
        doNothing().when(emailService).sendEmail(isNull(), anyString(), anyString());

        // Act
        ResponseEntity<?> response = controller.sendPasswordReset(null);

        // Assert
        assertNotNull(response);
        // This should ideally validate the email before sending
        verify(emailService, times(1)).sendEmail(isNull(), anyString(), anyString());
    }

    @Test
    void testSendPasswordReset_WithEmptyEmail() throws MessagingException {
        // Arrange
        String emptyEmail = "";
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        // Act
        ResponseEntity<?> response = controller.sendPasswordReset(emptyEmail);

        // Assert
        assertNotNull(response);
        verify(emailService, times(1)).sendEmail(eq(emptyEmail), anyString(), anyString());
    }
}