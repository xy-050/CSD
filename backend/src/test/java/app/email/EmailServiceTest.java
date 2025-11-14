package app.email;

import app.account.Account;
import app.account.AccountService;
import app.security.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private EmailService emailService;

    private Account testAccount;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_PASSWORD = "newPassword123";

    @BeforeEach
    void setUp() {
        // Set configuration properties
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@tariffics.org");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://tariffics.org");

        // Setup test account
        testAccount = new Account();
        testAccount.setEmail(TEST_EMAIL);
        testAccount.setUsername(TEST_USERNAME);
    }

    // ==========================================
    // sendPasswordResetEmail Tests
    // ==========================================

    @Test
    void sendPasswordResetEmail_Success() {
        // Arrange
        when(accountService.getAccountByUsername(TEST_USERNAME)).thenReturn(testAccount);
        when(jwtUtils.generateRefreshToken(any(UserDetails.class))).thenReturn(TEST_TOKEN);

        // Act
        emailService.sendPasswordResetEmail(TEST_USERNAME);

        // Assert
        verify(accountService).getAccountByUsername(TEST_USERNAME);
        verify(jwtUtils).generateRefreshToken(any(UserDetails.class));
        
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(TEST_EMAIL, sentMessage.getTo()[0]);
        assertEquals("no-reply@tariffics.org", sentMessage.getFrom());
        assertEquals("Password Reset Request", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(TEST_TOKEN));
        assertTrue(sentMessage.getText().contains("https://tariffics.org/reset-password"));
    }

    @Test
    void sendPasswordResetEmail_AccountNotFound_SilentlyReturns() {
        // Arrange
        when(accountService.getAccountByUsername(TEST_USERNAME)).thenReturn(null);

        // Act
        emailService.sendPasswordResetEmail(TEST_USERNAME);

        // Assert - should silently return without sending email
        verify(accountService).getAccountByUsername(TEST_USERNAME);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(jwtUtils, never()).generateRefreshToken(any(UserDetails.class));
    }

    @Test
    void sendPasswordResetEmail_MailAuthenticationException_Thrown() {
        // Arrange
        when(accountService.getAccountByUsername(TEST_USERNAME)).thenReturn(testAccount);
        when(jwtUtils.generateRefreshToken(any(UserDetails.class))).thenReturn(TEST_TOKEN);
        doThrow(new MailAuthenticationException("Auth failed")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailAuthenticationException.class, () -> {
            emailService.sendPasswordResetEmail(TEST_USERNAME);
        });

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmail_MailSendException_Thrown() {
        // Arrange
        when(accountService.getAccountByUsername(TEST_USERNAME)).thenReturn(testAccount);
        when(jwtUtils.generateRefreshToken(any(UserDetails.class))).thenReturn(TEST_TOKEN);
        doThrow(new MailSendException("Send failed")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> {
            emailService.sendPasswordResetEmail(TEST_USERNAME);
        });

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    // ==========================================
    // resetPasswordWithToken Tests
    // ==========================================

    @Test
    void resetPasswordWithToken_Success() {
        // Arrange
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(accountService.getAccountByEmail(TEST_EMAIL)).thenReturn(testAccount);

        // Act
        boolean result = emailService.resetPasswordWithToken(TEST_EMAIL, TEST_TOKEN, TEST_PASSWORD);

        // Assert
        assertTrue(result);
        verify(jwtUtils).validateToken(TEST_TOKEN);
        verify(jwtUtils).getUserNameFromJwtToken(TEST_TOKEN);
        verify(accountService).getAccountByEmail(TEST_EMAIL);
        verify(accountService).resetPassword(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test
    void resetPasswordWithToken_InvalidToken_ThrowsJwtException() {
        // Arrange
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(false);

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            emailService.resetPasswordWithToken(TEST_EMAIL, TEST_TOKEN, TEST_PASSWORD);
        });

        verify(jwtUtils).validateToken(TEST_TOKEN);
        verify(accountService, never()).resetPassword(any(), any());
    }

    @Test
    void resetPasswordWithToken_TokenEmailNull_ThrowsJwtException() {
        // Arrange
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_TOKEN)).thenReturn(null);

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            emailService.resetPasswordWithToken(TEST_EMAIL, TEST_TOKEN, TEST_PASSWORD);
        });

        verify(jwtUtils).validateToken(TEST_TOKEN);
        verify(jwtUtils).getUserNameFromJwtToken(TEST_TOKEN);
        verify(accountService, never()).resetPassword(any(), any());
    }

    @Test
    void resetPasswordWithToken_EmailMismatch_ThrowsJwtException() {
        // Arrange
        String differentEmail = "different@example.com";
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_TOKEN)).thenReturn(differentEmail);

        // Act & Assert
        JwtException exception = assertThrows(JwtException.class, () -> {
            emailService.resetPasswordWithToken(TEST_EMAIL, TEST_TOKEN, TEST_PASSWORD);
        });

        assertTrue(exception.getMessage().contains("does not match"));
        verify(jwtUtils).validateToken(TEST_TOKEN);
        verify(jwtUtils).getUserNameFromJwtToken(TEST_TOKEN);
        verify(accountService, never()).resetPassword(any(), any());
    }

    @Test
    void resetPasswordWithToken_AccountNotFound_ReturnsFalse() {
        // Arrange
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(accountService.getAccountByEmail(TEST_EMAIL)).thenReturn(null);

        // Act
        boolean result = emailService.resetPasswordWithToken(TEST_EMAIL, TEST_TOKEN, TEST_PASSWORD);

        // Assert
        assertFalse(result);
        verify(accountService).getAccountByEmail(TEST_EMAIL);
        verify(accountService, never()).resetPassword(any(), any());
    }

    @Test
    void resetPasswordWithToken_InvalidPassword_ThrowsIllegalArgumentException() {
        // Arrange
        when(jwtUtils.validateToken(TEST_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_TOKEN)).thenReturn(TEST_EMAIL);
        when(accountService.getAccountByEmail(TEST_EMAIL)).thenReturn(testAccount);
        doThrow(new IllegalArgumentException("Password too short"))
            .when(accountService).resetPassword(TEST_EMAIL, TEST_PASSWORD);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            emailService.resetPasswordWithToken(TEST_EMAIL, TEST_TOKEN, TEST_PASSWORD);
        });

        verify(accountService).resetPassword(TEST_EMAIL, TEST_PASSWORD);
    }

    // ==========================================
    // sendNotificationEmail Tests
    // ==========================================

    @Test
    void sendNotificationEmail_Success() {
        // Arrange
        String htsCode = "1234.56.78";
        String oldPrice = "5%";
        String newPrice = "10%";

        // Act
        emailService.sendNotificationEmail(TEST_EMAIL, htsCode, oldPrice, newPrice);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals(TEST_EMAIL, sentMessage.getTo()[0]);
        assertEquals("no-reply@tariffics.org", sentMessage.getFrom());
        assertEquals("Notification from Tariffics", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(htsCode));
        assertTrue(sentMessage.getText().contains(oldPrice));
        assertTrue(sentMessage.getText().contains(newPrice));
        assertTrue(sentMessage.getText().contains("https://tariffics.org/product/" + htsCode));
    }

    @Test
    void sendNotificationEmail_NullEmail_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendNotificationEmail(null, "1234", "5%", "10%");
        });

        assertTrue(exception.getMessage().contains("email"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_EmptyEmail_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendNotificationEmail("   ", "1234", "5%", "10%");
        });

        assertTrue(exception.getMessage().contains("email"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_NullHtsCode_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendNotificationEmail(TEST_EMAIL, null, "5%", "10%");
        });

        assertTrue(exception.getMessage().contains("HTS code"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_NullOldPrice_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendNotificationEmail(TEST_EMAIL, "1234", null, "10%");
        });

        assertTrue(exception.getMessage().contains("Old price"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_NullNewPrice_ThrowsIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.sendNotificationEmail(TEST_EMAIL, "1234", "5%", null);
        });

        assertTrue(exception.getMessage().contains("New price"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_MailSendException_Thrown() {
        // Arrange
        doThrow(new MailSendException("Send failed")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> {
            emailService.sendNotificationEmail(TEST_EMAIL, "1234", "5%", "10%");
        });

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}