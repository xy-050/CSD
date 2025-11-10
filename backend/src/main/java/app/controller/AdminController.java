package app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import app.account.Account;
import app.account.AccountService;
import app.exception.UserNotFoundException;

/**
 * Admin-only endpoints for managing users and system data.
 * 
 * ACCESS: Only users with ADMIN role can access these endpoints.
 * 
 * AVAILABLE OPERATIONS:
 * - View all user accounts
 * - Delete user accounts
 * - View system statistics
 * - Manage user roles
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // All endpoints require ADMIN role
public class AdminController {

    private final AccountService accountService;

    public AdminController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * GET /api/admin/users
     * Lists all user accounts in the system.
     * 
     * @return List of all accounts
     */
    @GetMapping("/users")
    public ResponseEntity<List<Account>> getAllUsers() {
        List<Account> users = accountService.getAllAccounts();
        return ResponseEntity.ok(users);
    }

    /**
     * DELETE /api/admin/users/{userId}
     * deletes a user account.
     * 
     * @param userId The user ID to delete
     * @return Success message
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        try {
            accountService.deleteAccount(userId);
            return ResponseEntity.ok("User deleted successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/admin/stats
     * returns system statistics (user count, query count, etc.)
     * 
     * @return Statistics object
     */
    @GetMapping("/stats")
    public ResponseEntity<SystemStats> getSystemStats() {
        long userCount = accountService.getAllAccounts().size();

        SystemStats stats = new SystemStats();
        stats.setTotalUsers(userCount);
        stats.setTotalAdmins(accountService.getAllAccounts().stream()
                .filter(account -> "ADMIN".equals(account.getRole()))
                .count());

        return ResponseEntity.ok(stats);
    }

    /**
     * PUT /api/admin/users/{userId}/role
     * updates a user's role (promote to admin or demote to user).
     * 
     * @param userId  The user ID to update
     * @param newRole The new role (USER or ADMIN)
     * @return Updated account
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Account> updateUserRole(@PathVariable Integer userId,
            @RequestBody RoleUpdateRequest request) {
        Account account = accountService.updateRole(userId, request);
        return ResponseEntity.ok(account);
    }

    // ========== fata transfer object (stores all data) ==========

    public static class SystemStats {
        private long totalUsers;
        private long totalAdmins;

        public long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public long getTotalAdmins() {
            return totalAdmins;
        }

        public void setTotalAdmins(long totalAdmins) {
            this.totalAdmins = totalAdmins;
        }
    }

    public static class RoleUpdateRequest {
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
