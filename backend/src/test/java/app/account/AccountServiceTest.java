package app.account;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.exception.UserConflictException;
import app.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    AccountService accountService;

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
    @Test
    public void updateDetails_WhenNoUsernameOrEmailChange_ShouldUpdateSuccessfully() {
        Account existingAccount = new Account();
        existingAccount.setUserID(1);
        existingAccount.setUsername("existingUser");
        existingAccount.setEmail("existing@email.com");

        Account newAccount = new Account();
        newAccount.setUserID(1);
        newAccount.setUsername("existingUser"); // Same username
        newAccount.setEmail("existing@email.com"); // Same email
        newAccount.setPassword("newPassword123");

        when(accountRepository.findByUserID(1)).thenReturn(existingAccount);

        assertDoesNotThrow(() -> accountService.updateDetails(1, newAccount));

        verify(accountRepository).save(existingAccount);
        verify(accountRepository, never()).findByUsername(anyString());
        verify(accountRepository, never()).findByEmail(anyString());
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
}
