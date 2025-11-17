package app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import app.account.Account;
import app.account.AccountService;
import app.controller.AdminController.RoleUpdateRequest;
import app.controller.AdminController.SystemStats;
import app.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getAllUsers_ReturnsAccountsFromService() {
        Account account = new Account();
        account.setUsername("alice");
        when(accountService.getAllAccounts()).thenReturn(List.of(account));

        ResponseEntity<List<Account>> response = adminController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(accountService).getAllAccounts();
    }

    @Test
    void deleteUser_ReturnsOkWhenServiceSucceeds() {
        ResponseEntity<String> response = adminController.deleteUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
        verify(accountService).deleteAccount(1);
    }

    @Test
    void deleteUser_ReturnsNotFoundWhenUserMissing() {
        doThrow(new UserNotFoundException("missing")).when(accountService).deleteAccount(2);

        ResponseEntity<String> response = adminController.deleteUser(2);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getSystemStats_ComputesCountsFromAccounts() {
        Account admin = new Account();
        admin.setRole("ADMIN");
        Account user = new Account();
        user.setRole("USER");
        when(accountService.getAllAccounts()).thenReturn(List.of(admin, user));

        ResponseEntity<SystemStats> response = adminController.getSystemStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalUsers());
        assertEquals(1, response.getBody().getTotalAdmins());
        verify(accountService, times(2)).getAllAccounts();
    }

    @Test
    void updateUserRole_ReturnsUpdatedAccount() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRole("ADMIN");

        Account updated = new Account();
        updated.setRole("ADMIN");

        when(accountService.updateRole(10, request)).thenReturn(updated);

        ResponseEntity<Account> response = adminController.updateUserRole(10, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ADMIN", response.getBody().getRole());
    }
}



