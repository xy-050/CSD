package app.account;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import app.controller.AdminController.RoleUpdateRequest;
import app.exception.InvalidPasswordException;
import app.exception.UserConflictException;
import app.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    AccountService accountService;

    // ----------------------------------------------------------------
    // -------------- testing updateDetails() method ------------------
    // ----------------------------------------------------------------

    // Test 1: User does not exist
    @Test
    public void updateDetails_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        Account newAccount = new Account();
        newAccount.setUsername("newUsername");
        newAccount.setEmail("new@email.com");

        when(accountRepository.findByUserID(999)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            accountService.updateDetails(999, newAccount);
        });

        assertEquals("Account with user ID 999 not found!", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // Test 2: Successfully update details (no username/email change)
    @Test
    public void updateDetails_WhenNoUsernameOrEmailChange_ShouldThrowIllegalArgumentException() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("existingUser");
        existingAccount.setEmail("existing@email.com");

        Account newAccount = new Account();
        newAccount.setUserID(1);
        newAccount.setUsername("existingUser"); // Same username
        newAccount.setEmail("existing@email.com"); // Same email
        newAccount.setPassword("newPassword123");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.updateDetails(1, newAccount);
        });
        assertEquals("Nothing to update.", exception.getMessage());
    }

    // Test 3: Update username to new unique username
    @Test
    public void updateDetails_WhenUsernameChangedToUnique_ShouldUpdateSuccessfully() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("oldUsername");
        existingAccount.setEmail("user@email.com");

        Account newAccount = new Account();
        newAccount.setUserID(1);
        newAccount.setUsername("newUniqueUsername");
        newAccount.setEmail("user@email.com");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByUsername("newUniqueUsername")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountService.updateDetails(1, newAccount));

        verify(accountRepository).findByUsername("newUniqueUsername");
        verify(accountRepository).save(existingAccount);
    }

    // Test 4: Update username to already taken username
    @Test
    public void updateDetails_WhenUsernameAlreadyTaken_ShouldThrowUserConflictException() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("oldUsername");
        existingAccount.setEmail("user@email.com");

        Account newAccount = new Account();
        newAccount.setUsername("takenUsername");
        newAccount.setEmail("user@email.com");

        Account conflictingAccount = new Account();
        conflictingAccount.setUserID(2);
        conflictingAccount.setUsername("takenUsername");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByUsername("takenUsername")).thenReturn(Optional.of(conflictingAccount));

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            accountService.updateDetails(1, newAccount);
        });

        assertEquals("Username takenUsername already taken, please try another one!", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // Test 5: Update email to new unique email
    @Test
    public void updateDetails_WhenEmailChangedToUnique_ShouldUpdateSuccessfully() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("username");
        existingAccount.setEmail("old@email.com");

        Account newAccount = new Account();
        newAccount.setUserID(1);
        newAccount.setUsername("username");
        newAccount.setEmail("new@email.com");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountService.updateDetails(1, newAccount));

        verify(accountRepository).findByEmail("new@email.com");
        verify(accountRepository).save(existingAccount);
    }

    // Test 6: Update email to already taken email
    @Test
    public void updateDetails_WhenEmailAlreadyTaken_ShouldThrowUserConflictException() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("username");
        existingAccount.setEmail("old@email.com");

        Account newAccount = new Account();
        newAccount.setUsername("username");
        newAccount.setEmail("taken@email.com");

        Account conflictingAccount = new Account();
        conflictingAccount.setUserID(2);
        conflictingAccount.setEmail("taken@email.com");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(conflictingAccount));

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            accountService.updateDetails(1, newAccount);
        });

        assertEquals("Email taken@email.com already associated with another account, please try another one!",
                exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // Test 7: Update both username and email to unique values
    @Test
    public void updateDetails_WhenBothUsernameAndEmailChangedToUnique_ShouldUpdateSuccessfully() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("oldUsername");
        existingAccount.setEmail("old@email.com");

        Account newAccount = new Account();
        newAccount.setUserID(1);
        newAccount.setUsername("newUsername");
        newAccount.setEmail("new@email.com");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(accountRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountService.updateDetails(1, newAccount));

        verify(accountRepository).findByUsername("newUsername");
        verify(accountRepository).findByEmail("new@email.com");
        verify(accountRepository).save(existingAccount);
    }

    // Test 8: Update username (unique) but email is taken
    @Test
    public void updateDetails_WhenUsernameUniqueButEmailTaken_ShouldThrowUserConflictException() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("oldUsername");
        existingAccount.setEmail("old@email.com");

        Account newAccount = new Account();
        newAccount.setUsername("newUsername");
        newAccount.setEmail("taken@email.com");

        Account conflictingAccount = new Account();
        conflictingAccount.setUserID(2);
        conflictingAccount.setEmail("taken@email.com");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(accountRepository.findByEmail("taken@email.com")).thenReturn(Optional.of(conflictingAccount));

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            accountService.updateDetails(1, newAccount);
        });

        assertTrue(exception.getMessage().contains("Email taken@email.com already associated"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ----------------------------------------------------------------
    // --------------- testing updatePassword() method ----------------
    // ----------------------------------------------------------------

    @Test
    void updatePassword_WhenValidPasswordChange_ShouldUpdateSuccessfully() {
        Account account = new Account();
        account.setUserID(1);
        account.setPassword("encodedOldPassword");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123!", "encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123!")).thenReturn("encodedNewPassword");

        assertDoesNotThrow(() -> accountService.updatePassword(1, "oldPassword", "newPassword123!"));

        verify(accountRepository).save(account);
        assertEquals("encodedNewPassword", account.getPassword());
    }

    @Test
    void updatePassword_WhenPreviousPasswordIncorrect_ShouldThrowInvalidPasswordException() {
        Account account = new Account();
        account.setUserID(1);
        account.setPassword("encodedOldPassword");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> accountService.updatePassword(1, "wrongPassword", "newPassword123!"));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updatePassword_WhenNewPasswordSameAsPrevious_ShouldThrowInvalidPasswordException() {
        Account account = new Account();
        account.setUserID(1);
        account.setPassword("encodedPassword");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("samePassword", "encodedPassword")).thenReturn(true);

        assertThrows(
                InvalidPasswordException.class,
                () -> accountService.updatePassword(1, "samePassword", "samePassword"));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updatePassword_WhenNewPasswordInvalid_ShouldThrowInvalidPasswordException() {
        Account account = new Account();
        account.setUserID(1);
        account.setPassword("encodedOldPassword");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.matches("weak", "encodedOldPassword")).thenReturn(false);

        assertThrows(
                InvalidPasswordException.class,
                () -> accountService.updatePassword(1, "oldPassword", "weak"));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updatePassword_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        when(accountRepository.findByUserID(999)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> accountService.updatePassword(999, "oldPassword", "newPassword123!"));

        verify(accountRepository, never()).save(any());
    }

    // ----------------------------------------------------------------
    // ----------------- testing updateRole() method ------------------
    // ----------------------------------------------------------------
    @Test
    void updateRole_WhenRoleIsUSER_ShouldUpdateSuccessfully() {
        Account account = new Account();
        account.setUserID(1);
        account.setRole("ADMIN");

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("USER");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.updateRole(1, request);

        assertEquals("USER", result.getRole());
        verify(accountRepository).save(account);
    }

    @Test
    void updateRole_WhenRoleIsADMIN_ShouldUpdateSuccessfully() {
        Account account = new Account();
        account.setUserID(1);
        account.setRole("USER");

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("ADMIN");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.updateRole(1, request);

        assertEquals("ADMIN", result.getRole());
        verify(accountRepository).save(account);
    }

    @Test
    void updateRole_WhenRoleIsInvalid_ShouldThrowIllegalArgumentException() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("SUPERUSER");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateRole(1, request));

        assertEquals("Role SUPERUSER does not exist!", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateRole_WhenRoleIsLowercase_ShouldThrowIllegalArgumentException() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("user");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateRole(1, request));

        assertEquals("Role user does not exist!", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateRole_WhenRoleIsNull_ShouldThrowIllegalArgumentException() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateRole(1, request));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateRole_WhenRoleIsEmpty_ShouldThrowIllegalArgumentException() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateRole(1, request));

        assertEquals("Role  does not exist!", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateRole_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("USER");

        when(accountRepository.findByUserID(999)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> accountService.updateRole(999, request));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateRole_WhenRoleUnchanged_ShouldStillSave() {
        Account account = new Account();
        account.setUserID(1);
        account.setRole("USER");

        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("USER");

        when(accountRepository.findByUserID(1)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.updateRole(1, request);

        assertEquals("USER", result.getRole());
        verify(accountRepository).save(account);
    }

    // Add these tests to the existing AccountServiceTest class

    // -------------------------------------------------------------------
    // ----------------- testing createAccount() method ------------------
    // -------------------------------------------------------------------

    @Test
    void createAccount_WhenValidAccount_ShouldCreateSuccessfully() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setUsername("newuser");
        newAccount.setEmail("newuser@example.com");
        newAccount.setPassword("ValidPass123!");

        when(accountRepository.findByUsername("newuser"))
                .thenThrow(new UserNotFoundException("Account with username newuser not found!"));
        when(accountRepository.findByEmail("newuser@example.com"))
                .thenThrow(new UserNotFoundException("Account with email newuser@example.com not found!"));
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("encodedPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(newAccount);

        // Act
        Account result = accountService.createAccount(newAccount);

        // Assert
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("ValidPass123!");
        verify(accountRepository, times(1)).save(newAccount);
    }

    @Test
    void createAccount_WhenUsernameAlreadyExists_ShouldThrowUserConflictException() {
        // Arrange
        Account existingAccount = new Account();
        existingAccount.setUsername("existinguser");
        existingAccount.setEmail("existing@example.com");

        Account newAccount = new Account();
        newAccount.setUsername("existinguser");
        newAccount.setEmail("new@example.com");
        newAccount.setPassword("ValidPass123!");

        when(accountRepository.findByUsername("existinguser"))
                .thenReturn(Optional.of(existingAccount));

        // Act & Assert
        UserConflictException exception = assertThrows(
                UserConflictException.class,
                () -> accountService.createAccount(newAccount));
        assertEquals("Username existinguser already exists!", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_WhenEmailAlreadyExists_ShouldThrowUserConflictException() {
        // Arrange
        Account existingAccount = new Account();
        existingAccount.setUsername("existinguser");
        existingAccount.setEmail("existing@example.com");

        Account newAccount = new Account();
        newAccount.setUsername("newuser");
        newAccount.setEmail("existing@example.com");
        newAccount.setPassword("ValidPass123!");

        when(accountRepository.findByUsername("newuser"))
                .thenThrow(new UserNotFoundException("Account with username newuser not found!"));
        when(accountRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(existingAccount));

        // Act & Assert
        UserConflictException exception = assertThrows(
                UserConflictException.class,
                () -> accountService.createAccount(newAccount));
        assertEquals("Email already associated with a different account!", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_WhenInvalidPassword_ShouldThrowInvalidPasswordException() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setUsername("newuser");
        newAccount.setEmail("newuser@example.com");
        newAccount.setPassword("weak");

        when(accountRepository.findByUsername("newuser"))
                .thenThrow(new UserNotFoundException("Account with username newuser not found!"));
        when(accountRepository.findByEmail("newuser@example.com"))
                .thenThrow(new UserNotFoundException("Account with email newuser@example.com not found!"));

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> accountService.createAccount(newAccount));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_WhenPasswordValid_ShouldEncodePassword() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setUsername("newuser");
        newAccount.setEmail("newuser@example.com");
        newAccount.setPassword("ValidPass123!");

        when(accountRepository.findByUsername("newuser"))
                .thenThrow(new UserNotFoundException("Account with username newuser not found!"));
        when(accountRepository.findByEmail("newuser@example.com"))
                .thenThrow(new UserNotFoundException("Account with email newuser@example.com not found!"));
        when(passwordEncoder.encode("ValidPass123!")).thenReturn("encodedPassword");
        when(accountRepository.save(newAccount)).thenReturn(newAccount);

        // Act
        accountService.createAccount(newAccount);

        // Assert
        assertEquals("encodedPassword", newAccount.getPassword());
        verify(passwordEncoder, times(1)).encode("ValidPass123!");
    }

    // -------------------------------------------------------------------
    // ------------ testing updatePasswordViaEmail() method --------------
    // -------------------------------------------------------------------

    @Test
    void updatePasswordViaEmail_WhenValidEmail_ShouldUpdateSuccessfully() {
        // Arrange
        Account account = new Account();
        account.setUserID(1);
        account.setEmail("test@example.com");
        account.setPassword("oldEncodedPassword");

        when(accountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");

        // Act
        accountService.updatePasswordViaEmail("test@example.com", "NewPassword123!");

        // Assert
        assertEquals("newEncodedPassword", account.getPassword());
        verify(passwordEncoder, times(1)).encode("NewPassword123!");
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void updatePasswordViaEmail_WhenEmailNotFound_ShouldThrowUserNotFoundException() {
        // Arrange
        when(accountRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UserNotFoundException.class,
                () -> accountService.updatePasswordViaEmail("nonexistent@example.com", "NewPassword123!"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updatePasswordViaEmail_WhenNullEmail_ShouldThrowUserNotFoundException() {
        // Arrange
        when(accountRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UserNotFoundException.class,
                () -> accountService.updatePasswordViaEmail(null, "NewPassword123!"));
        verify(accountRepository, never()).save(any());
    }
}