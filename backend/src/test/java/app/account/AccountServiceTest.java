package app.account;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    // --------------------- updateDetails() -------------------------- 
    // ----------------------------------------------------------------

    // Test 1: User does not exist
    @Test
    public void updateDetails_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        Account newAccount = new Account();
        newAccount.setUsername("newUsername");
        newAccount.setEmail("new@email.com");

        when(accountRepository.findByUserID(999)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            accountService.updateDetails(999, newAccount);
        });

        assertEquals("User with user ID 999 does not exist", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // Test 2: Successfully update details (no username/email change)
    // @Test
    // public void updateDetails_WhenNoUsernameOrEmailChange_ShouldUpdateSuccessfully() {
    //     Account existingAccount = new Account();
    //     existingAccount.setUserID(1);
    //     existingAccount.setUsername("existingUser");
    //     existingAccount.setEmail("existing@email.com");

    //     Account newAccount = new Account();
    //     newAccount.setUserID(1);
    //     newAccount.setUsername("existingUser"); // Same username
    //     newAccount.setEmail("existing@email.com"); // Same email
    //     newAccount.setPassword("newPassword123");

    //     when(accountRepository.findByUserID(1)).thenReturn(existingAccount);

    //     assertDoesNotThrow(() -> accountService.updateDetails(1, newAccount));

    //     verify(accountRepository).save(existingAccount);
    //     verify(accountRepository, never()).findByUsername(anyString());
    //     verify(accountRepository, never()).findByEmail(anyString());
    // }

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

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);
        when(accountRepository.findByUsername("newUniqueUsername")).thenReturn(null);

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

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);
        when(accountRepository.findByUsername("takenUsername")).thenReturn(conflictingAccount);

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

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);
        when(accountRepository.findByEmail("new@email.com")).thenReturn(null);

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

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);
        when(accountRepository.findByEmail("taken@email.com")).thenReturn(conflictingAccount);

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            accountService.updateDetails(1, newAccount);
        });

        assertEquals("Email taken@email.com already associated with another account, please use another one!",
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

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);
        when(accountRepository.findByUsername("newUsername")).thenReturn(null);
        when(accountRepository.findByEmail("new@email.com")).thenReturn(null);

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

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);
        when(accountRepository.findByUsername("newUsername")).thenReturn(null);
        when(accountRepository.findByEmail("taken@email.com")).thenReturn(conflictingAccount);

        UserConflictException exception = assertThrows(UserConflictException.class, () -> {
            accountService.updateDetails(1, newAccount);
        });

        assertTrue(exception.getMessage().contains("Email taken@email.com already associated"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ----------------------------------------------------------------
    // ---------------------- changePassword() ------------------------ 
    // ----------------------------------------------------------------

    // Test 1: User does not exist
    // @Test
    // public void changePassword_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
    //     when(accountRepository.findByUserID(999)).thenReturn(null);

    //     UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
    //         accountService.changePassword(999, "oldPass123", "newPass456");
    //     });

    //     assertEquals("Account not found.", exception.getMessage());
    //     verify(accountRepository, never()).save(any(Account.class));
    //     verify(passwordEncoder, never()).encode(anyString());
    // }

    // Test 2: Previous password is incorrect
    // @Test
    // public void changePassword_WhenPreviousPasswordIncorrect_ShouldThrowIllegalArgumentException() {
    //     Account account = new Account();
    //     account.setUserID(1);
    //     account.setPassword("encodedPassword123");

    //     when(accountRepository.findByUserID(1)).thenReturn(account);
    //     when(passwordEncoder.matches("wrongPassword", "encodedPassword123")).thenReturn(false);

    //     IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
    //         accountService.changePassword(1, "wrongPassword", "newPass456");
    //     });

    //     assertEquals("Previous password is incorrect.", exception.getMessage());
    //     verify(accountRepository, never()).save(any(Account.class));
    //     verify(passwordEncoder, never()).encode(anyString());
    // }

    // Test 3: New password is same as previous password
    // @Test
    // public void changePassword_WhenNewPasswordSameAsPrevious_ShouldThrowIllegalArgumentException() {
    //     Account account = new Account();
    //     account.setUserID(1);
    //     account.setPassword("encodedPassword123");

    //     when(accountRepository.findByUserID(1)).thenReturn(account);
    //     when(passwordEncoder.matches("samePassword", "encodedPassword123")).thenReturn(true);

    //     IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
    //         accountService.changePassword(1, "samePassword", "samePassword");
    //     });

    //     assertEquals("New password must be different from the previous password.", exception.getMessage());
    //     verify(accountRepository, never()).save(any(Account.class));
    // }

    // Test 4: Successfully change password
    // @Test
    // public void changePassword_WhenValidPasswordChange_ShouldUpdatePassword() {
    //     Account account = new Account();
    //     account.setUserID(1);
    //     account.setPassword("encodedOldPassword");

    //     when(accountRepository.findByUserID(1)).thenReturn(account);
    //     when(passwordEncoder.matches("oldPass123", "encodedOldPassword")).thenReturn(true);
    //     when(passwordEncoder.matches("newPass456", "encodedOldPassword")).thenReturn(false);
    //     when(passwordEncoder.encode("newPass456")).thenReturn("encodedNewPassword");

    //     assertDoesNotThrow(() -> accountService.changePassword(1, "oldPass123", "newPass456"));

    //     verify(passwordEncoder).matches("oldPass123", "encodedOldPassword");
    //     verify(passwordEncoder).matches("newPass456", "encodedOldPassword");
    //     verify(passwordEncoder).encode("newPass456");
    //     verify(accountRepository).save(account);
    //     assertEquals("encodedNewPassword", account.getPassword());
    // }

    // Test 5: Password matches on first check but not second (edge case validation)
    // @Test
    // public void changePassword_WhenPreviousPasswordCorrectAndNewPasswordDifferent_ShouldSucceed() {
    //     Account account = new Account();
    //     account.setUserID(1);
    //     account.setPassword("encodedCurrentPassword");

    //     when(accountRepository.findByUserID(1)).thenReturn(account);
    //     // First matches call - checking if previous password is correct
    //     when(passwordEncoder.matches("correctOldPass", "P@ssw0rd")).thenReturn(true);
    //     // Second matches call - checking if new password is different
    //     when(passwordEncoder.matches("differentNewPass", "P@ssw0rd")).thenReturn(false);
    //     when(passwordEncoder.encode("differentNewPass")).thenReturn("encodedDifferentNewPass");

    //     assertDoesNotThrow(() -> accountService.changePassword(1, "correctOldPass", "differentNewPass"));

    //     verify(passwordEncoder, times(1)).matches("correctOldPass", "encodedCurrentPassword");
    //     verify(passwordEncoder, times(1)).matches("differentNewPass", "encodedCurrentPassword");
    //     verify(passwordEncoder).encode("differentNewPass");
    //     verify(accountRepository).save(account);
    // }

    // Test 6: Verify the order of validation (previous password checked before new
    // password)
    // @Test
    // public void changePassword_WhenPreviousPasswordWrong_ShouldNotCheckNewPassword() {
    //     Account account = new Account();
    //     account.setUserID(1);
    //     account.setPassword("encodedPassword");

    //     when(accountRepository.findByUserID(1)).thenReturn(account);
    //     when(passwordEncoder.matches("wrongPrevious", "encodedPassword")).thenReturn(false);

    //     assertThrows(IllegalArgumentException.class, () -> {
    //         accountService.changePassword(1, "wrongPrevious", "anyNewPassword");
    //     });

    //     // Verify that we only checked the previous password, not the new one
    //     verify(passwordEncoder, times(1)).matches("wrongPrevious", "encodedPassword");
    //     verify(passwordEncoder, never()).matches("anyNewPassword", "encodedPassword");
    //     verify(passwordEncoder, never()).encode(anyString());
    // }

    // Test 7: Verify account is saved with updated password
    // @Test
    // public void changePassword_WhenSuccessful_ShouldSaveAccountWithEncodedPassword() {
    //     Account account = new Account();
    //     account.setUserID(1);
    //     account.setUsername("testUser");
    //     account.setEmail("test@email.com");
    //     account.setPassword("oldEncodedPassword");

    //     when(accountRepository.findByUserID(1)).thenReturn(account);
    //     when(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).thenReturn(true);
    //     when(passwordEncoder.matches("newPassword", "oldEncodedPassword")).thenReturn(false);
    //     when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

    //     assertDoesNotThrow(() -> accountService.changePassword(1, "oldPassword", "newPassword"));

    //     ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    //     verify(accountRepository).save(accountCaptor.capture());

    //     Account savedAccount = accountCaptor.getValue();
    //     assertEquals("newEncodedPassword", savedAccount.getPassword());
    //     assertEquals(1, savedAccount.getUserID());
    //     assertEquals("testUser", savedAccount.getUsername());
    //     assertEquals("test@email.com", savedAccount.getEmail());
    // }
}
