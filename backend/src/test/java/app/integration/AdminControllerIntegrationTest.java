package app.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.account.Account;
import app.account.AccountRepository;

/**
 * Integration Test: Admin Controller Operations
 * 
 * This test validates admin-specific functionality:
 * 1. Admin can view all users in the system
 * 2. Admin can delete user accounts
 * 3. Admin can view system statistics
 * 4. Admin can update user roles (promote/demote)
 * 5. Regular users are blocked from admin endpoints
 * 6. Admin operations properly enforce authorization
 * 
 * Tests the @PreAuthorize("hasRole('ADMIN')") security on AdminController
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Account adminUser;
    private Account regularUser1;
    private Account regularUser2;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up existing test data
        accountRepository.findByEmail("admin@admintest.com").ifPresent(accountRepository::delete);
        accountRepository.findByEmail("user1@admintest.com").ifPresent(accountRepository::delete);
        accountRepository.findByEmail("user2@admintest.com").ifPresent(accountRepository::delete);

        // Create admin user
        adminUser = new Account();
        adminUser.setEmail("admin@admintest.com");
        adminUser.setPassword(passwordEncoder.encode("AdminPass123!"));
        adminUser.setUsername("adminuser");
        adminUser.setRole("ADMIN");
        adminUser = accountRepository.save(adminUser);

        // Create regular users
        regularUser1 = new Account();
        regularUser1.setEmail("user1@admintest.com");
        regularUser1.setPassword(passwordEncoder.encode("UserPass123!"));
        regularUser1.setUsername("user1");
        regularUser1.setRole("USER");
        regularUser1 = accountRepository.save(regularUser1);

        regularUser2 = new Account();
        regularUser2.setEmail("user2@admintest.com");
        regularUser2.setPassword(passwordEncoder.encode("UserPass123!"));
        regularUser2.setUsername("user2");
        regularUser2.setRole("USER");
        regularUser2 = accountRepository.save(regularUser2);

        // Get admin token
        String adminLoginJson = "{\"username\":\"adminuser\",\"password\":\"AdminPass123!\"}";
        MvcResult adminLoginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminLoginJson))
                .andReturn();
        JsonNode adminResponse = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString());
        adminToken = adminResponse.get("token").asText();

        // Get regular user token
        String userLoginJson = "{\"username\":\"user1\",\"password\":\"UserPass123!\"}";
        MvcResult userLoginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userLoginJson))
                .andReturn();
        JsonNode userResponse = objectMapper.readTree(userLoginResult.getResponse().getContentAsString());
        userToken = userResponse.get("token").asText();
    }

    /**
     * Test 1: Admin Can View All Users
     * 
     * What it tests:
     * - GET /admin/users returns all user accounts
     * - Response includes all users (admin + regular users)
     * - User details are properly serialized
     * 
     * Why it matters:
     * - Core admin functionality for user management
     * - Allows admin to see all registered users
     * - Foundation for user administration features
     */
    @Test
    public void testAdminGetAllUsers_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode users = objectMapper.readTree(responseBody);

        // Should have at least 3 users (admin + 2 regular users)
        assertTrue(users.size() >= 3, "Should return at least 3 users");

        // Verify admin user is in the list
        boolean foundAdmin = false;
        for (JsonNode user : users) {
            if (user.get("email").asText().equals("admin@admintest.com")) {
                foundAdmin = true;
                assertEquals("ADMIN", user.get("role").asText());
            }
        }
        assertTrue(foundAdmin, "Admin user should be in the list");
    }

    /**
     * Test 2: Regular User Cannot Access Admin Endpoints
     * 
     * What it tests:
     * - Non-admin users are blocked from /admin/* endpoints
     * - Returns 403 Forbidden status
     * - @PreAuthorize security annotation works
     * 
     * Why it matters:
     * - Security: prevents privilege escalation
     * - Role-based access control enforcement
     * - Protects sensitive admin operations
     */
    @Test
    public void testRegularUserCannotAccessAdminEndpoints_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 3: Unauthenticated User Cannot Access Admin Endpoints
     * 
     * What it tests:
     * - Admin endpoints require authentication
     * - Returns 401 Unauthorized without token
     * 
     * Why it matters:
     * - Security: admin endpoints must require login
     * - Prevents anonymous access to sensitive data
     */
    @Test
    public void testUnauthenticatedUserCannotAccessAdminEndpoints_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test 4: Admin Can Delete User Account
     * 
     * What it tests:
     * - DELETE /admin/users/{userId} removes user
     * - User is actually deleted from database
     * - Returns success message
     * 
     * Why it matters:
     * - User management: ability to remove accounts
     * - Data cleanup and moderation
     * - Account lifecycle management
     */
    @Test
    public void testAdminDeleteUser_Success() throws Exception {
        Integer userIdToDelete = regularUser2.getUserID();

        mockMvc.perform(delete("/admin/users/" + userIdToDelete)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        // Verify user was deleted from database
        assertFalse(accountRepository.existsById(userIdToDelete),
                "User should be deleted from database");
    }

    /**
     * Test 5: Admin Cannot Delete Non-Existent User
     * 
     * What it tests:
     * - Attempting to delete non-existent user returns 404
     * - Error handling for invalid user IDs
     * 
     * Why it matters:
     * - Proper error handling
     * - Prevents silent failures
     * - User experience: clear error messages
     */
    @Test
    public void testAdminDeleteNonExistentUser_ReturnsNotFound() throws Exception {
        Integer nonExistentUserId = 99999;

        mockMvc.perform(delete("/admin/users/" + nonExistentUserId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 6: Regular User Cannot Delete Accounts
     * 
     * What it tests:
     * - Non-admin users blocked from deleting users
     * - Returns 403 Forbidden
     * 
     * Why it matters:
     * - Security: prevents unauthorized account deletion
     * - Role-based access control
     */
    @Test
    public void testRegularUserCannotDeleteUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/admin/users/" + regularUser2.getUserID())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Verify user was NOT deleted
        assertTrue(accountRepository.existsById(regularUser2.getUserID()),
                "User should still exist");
    }

    /**
     * Test 7: Admin Can View System Statistics
     * 
     * What it tests:
     * - GET /admin/stats returns system metrics
     * - Statistics include total users and admin count
     * - Counts are accurate
     * 
     * Why it matters:
     * - Dashboard functionality
     * - System monitoring and reporting
     * - Admin insights into user base
     */
    @Test
    public void testAdminGetSystemStats_Success() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode stats = objectMapper.readTree(responseBody);

        // Should have totalUsers and totalAdmins fields
        assertTrue(stats.has("totalUsers"), "Should have totalUsers field");
        assertTrue(stats.has("totalAdmins"), "Should have totalAdmins field");

        // Verify counts are positive
        assertTrue(stats.get("totalUsers").asLong() >= 3, "Should have at least 3 users");
        assertTrue(stats.get("totalAdmins").asLong() >= 1, "Should have at least 1 admin");
    }

    /**
     * Test 8: Regular User Cannot View System Statistics
     * 
     * What it tests:
     * - Non-admin blocked from viewing stats
     * - Returns 403 Forbidden
     * 
     * Why it matters:
     * - Security: stats may reveal sensitive information
     * - Admin-only feature enforcement
     */
    @Test
    public void testRegularUserCannotViewStats_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/admin/stats")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 9: Admin Can Promote User to Admin
     * 
     * What it tests:
     * - PUT /admin/users/{userId}/role changes user role
     * - User role is updated to ADMIN
     * - Changes are persisted to database
     * 
     * Why it matters:
     * - User management: role assignment
     * - Ability to grant admin privileges
     * - Permission management
     */
    @Test
    public void testAdminPromoteUserToAdmin_Success() throws Exception {
        String roleUpdateJson = "{\"role\":\"ADMIN\"}";

        mockMvc.perform(put("/admin/users/" + regularUser1.getUserID() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleUpdateJson)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Verify role change in database
        Account updatedUser = accountRepository.findById(regularUser1.getUserID()).orElseThrow();
        assertEquals("ADMIN", updatedUser.getRole(), "User should now be ADMIN");
    }

    /**
     * Test 10: Admin Can Demote Admin to User
     * 
     * What it tests:
     * - Admin can change ADMIN role back to USER
     * - Role changes work bidirectionally
     * 
     * Why it matters:
     * - Flexible permission management
     * - Ability to revoke admin privileges
     * - Complete role lifecycle management
     */
    @Test
    public void testAdminDemoteAdminToUser_Success() throws Exception {
        // First promote user1 to admin
        regularUser1.setRole("ADMIN");
        accountRepository.save(regularUser1);

        // Then demote back to USER
        String roleUpdateJson = "{\"role\":\"USER\"}";

        mockMvc.perform(put("/admin/users/" + regularUser1.getUserID() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleUpdateJson)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        // Verify role change
        Account updatedUser = accountRepository.findById(regularUser1.getUserID()).orElseThrow();
        assertEquals("USER", updatedUser.getRole(), "User should be demoted to USER");
    }

    /**
     * Test 11: Regular User Cannot Change Roles
     * 
     * What it tests:
     * - Non-admin users blocked from changing roles
     * - Returns 403 Forbidden
     * 
     * Why it matters:
     * - Security: prevents privilege escalation
     * - Users can't promote themselves to admin
     */
    @Test
    public void testRegularUserCannotChangeRoles_ReturnsForbidden() throws Exception {
        String roleUpdateJson = "{\"role\":\"ADMIN\"}";

        mockMvc.perform(put("/admin/users/" + regularUser2.getUserID() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleUpdateJson)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Verify role was NOT changed
        Account user = accountRepository.findById(regularUser2.getUserID()).orElseThrow();
        assertEquals("USER", user.getRole(), "Role should remain USER");
    }

    /**
     * Test 12: Admin Cannot Update Role for Non-Existent User
     * 
     * What it tests:
     * - Updating role for invalid user ID returns error
     * - Proper error handling
     * 
     * Why it matters:
     * - Data integrity
     * - Clear error messages
     */
    @Test
    public void testAdminUpdateRoleNonExistentUser_ReturnsError() throws Exception {
        Integer nonExistentUserId = 99999;
        String roleUpdateJson = "{\"role\":\"ADMIN\"}";

        mockMvc.perform(put("/admin/users/" + nonExistentUserId + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleUpdateJson)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Test 13: System Stats Show Correct Admin Count After Role Change
     * 
     * What it tests:
     * - Stats update in real-time after role changes
     * - Admin count increases when user is promoted
     * - End-to-end data consistency
     * 
     * Why it matters:
     * - Data integrity across operations
     * - Real-time dashboard accuracy
     * - Validates database triggers/updates
     */
    @Test
    public void testSystemStatsUpdateAfterRoleChange_Success() throws Exception {
        // Get initial stats
        MvcResult initialResult = mockMvc.perform(get("/admin/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andReturn();
        JsonNode initialStats = objectMapper.readTree(initialResult.getResponse().getContentAsString());
        long initialAdminCount = initialStats.get("totalAdmins").asLong();

        // Promote user to admin
        String roleUpdateJson = "{\"role\":\"ADMIN\"}";
        mockMvc.perform(put("/admin/users/" + regularUser1.getUserID() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleUpdateJson)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Get updated stats
        MvcResult updatedResult = mockMvc.perform(get("/admin/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andReturn();
        JsonNode updatedStats = objectMapper.readTree(updatedResult.getResponse().getContentAsString());
        long updatedAdminCount = updatedStats.get("totalAdmins").asLong();

        // Admin count should increase by 1
        assertEquals(initialAdminCount + 1, updatedAdminCount,
                "Admin count should increase after promotion");
    }

    /**
     * Test 14: Admin List Shows Updated Roles Immediately
     * 
     * What it tests:
     * - User list reflects role changes immediately
     * - No caching issues
     * - Data consistency
     * 
     * Why it matters:
     * - Real-time UI updates
     * - Ensures admin sees accurate data
     * - Tests transaction consistency
     */
    @Test
    public void testAdminListShowsUpdatedRoles_Success() throws Exception {
        // Promote user to admin
        String roleUpdateJson = "{\"role\":\"ADMIN\"}";
        mockMvc.perform(put("/admin/users/" + regularUser1.getUserID() + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleUpdateJson)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Get all users
        MvcResult result = mockMvc.perform(get("/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode users = objectMapper.readTree(responseBody);

        // Find the promoted user in the list
        boolean foundPromotedUser = false;
        for (JsonNode user : users) {
            if (user.get("userID").asInt() == regularUser1.getUserID()) {
                foundPromotedUser = true;
                assertEquals("ADMIN", user.get("role").asText(),
                        "User should show ADMIN role in list");
            }
        }
        assertTrue(foundPromotedUser, "Promoted user should be in the list");
    }
}
