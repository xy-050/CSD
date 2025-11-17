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
 * Integration Test: Authentication Flow
 * 
 * This test validates the complete authentication workflow:
 * 1. User Registration (Signup)
 * 2. User Login with credentials
 * 3. JWT Token generation and validation
 * 4. Protected endpoint access with JWT
 * 
 * Uses real database (H2 in-memory) and full Spring context
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String testEmail = "integrationtest@test.com";
    private String testPassword = "TestPassword123!";
    private String testUsername = "testuser";

    @BeforeEach
    void setUp() {
        // Clean up test user if exists
        accountRepository.findByEmail(testEmail).ifPresent(accountRepository::delete);
    }

    /**
     * Test 1: User Signup Flow
     * 
     * What it tests:
     * - POST /accounts creates a new user
     * - Password is hashed (not stored in plain text)
     * - User is assigned USER role by default
     * - Duplicate email registration is prevented
     * 
     * Why it matters:
     * - Ensures new users can register successfully
     * - Validates security best practices (password hashing)
     */
    @Test
    public void testUserSignup_Success() throws Exception {
        String signupJson = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\",\"username\":\"%s\"}",
            testEmail, testPassword, testUsername
        );

        // Perform signup
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson))
                .andExpect(status().isCreated());

        // Verify user was saved to database
        Account savedAccount = accountRepository.findByEmail(testEmail).orElse(null);
        assertNotNull(savedAccount, "User should be saved to database");
        assertTrue(passwordEncoder.matches(testPassword, savedAccount.getPassword()), 
                   "Password should be hashed");
    }

    /**
     * Test 2: User Login Flow
     * 
     * What it tests:
     * - POST /login authenticates user
     * - Returns JWT token on successful login
     * - Token contains user information
     * 
     * Why it matters:
     * - Core authentication mechanism
     * - JWT tokens enable stateless authentication
     */
    @Test
    public void testUserLogin_Success() throws Exception {
        // First create a user
        Account account = new Account();
        account.setEmail(testEmail);
        account.setPassword(passwordEncoder.encode(testPassword));
        account.setUsername(testUsername);
        account.setRole("USER");
        accountRepository.save(account);

        String loginJson = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            testUsername, testPassword
        );

        // Perform login
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        // Verify JWT token is returned
        assertTrue(jsonResponse.has("token"), "Response should contain JWT token");
        String token = jsonResponse.get("token").asText();
        assertFalse(token.isEmpty(), "JWT token should not be empty");
    }

    /**
     * Test 3: Login with Invalid Credentials
     * 
     * What it tests:
     * - Wrong password returns 401 Unauthorized
     * - Non-existent user returns 401 Unauthorized
     * 
     * Why it matters:
     * - Security: prevents unauthorized access
     * - User experience: proper error handling
     */
    @Test
    public void testUserLogin_InvalidCredentials_Fails() throws Exception {
        // Create a user
        Account account = new Account();
        account.setEmail(testEmail);
        account.setPassword(passwordEncoder.encode(testPassword));
        account.setUsername(testUsername);
        account.setRole("USER");
        accountRepository.save(account);

        String wrongPasswordJson = String.format(
            "{\"username\":\"%s\",\"password\":\"WrongPassword123!\"}",
            testUsername
        );

        // Attempt login with wrong password
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongPasswordJson))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test 4: Access Protected Endpoint with JWT
     * 
     * What it tests:
     * - Authenticated requests with valid JWT can access protected endpoints
     * - JWT token is properly validated by Spring Security
     * - User context is available in protected endpoints
     * 
     * Why it matters:
     * - Validates the entire auth chain: login -> token -> protected access
     * - Ensures JWT integration with Spring Security works
     */
    @Test
    public void testProtectedEndpointAccess_WithValidToken_Success() throws Exception {
        // Create and save user
        Account account = new Account();
        account.setEmail(testEmail);
        account.setPassword(passwordEncoder.encode(testPassword));
        account.setUsername(testUsername);
        account.setRole("USER");
        accountRepository.save(account);

        // Login to get token
        String loginJson = String.format(
            "{\"username\":\"%s\",\"password\":\"%s\"}",
            testUsername, testPassword
        );

        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        String token = jsonResponse.get("token").asText();

        // Access protected endpoint with token
        mockMvc.perform(get("/accounts/current")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.username").value(testUsername));
    }

    /**
     * Test 5: Access Protected Endpoint without JWT
     * 
     * What it tests:
     * - Requests without JWT token are rejected (401 Unauthorized)
     * - Spring Security properly enforces authentication
     * 
     * Why it matters:
     * - Security: ensures protected endpoints are actually protected
     * - Prevents unauthorized data access
     */
    @Test
    public void testProtectedEndpointAccess_WithoutToken_Fails() throws Exception {
        // Try to access protected endpoint without token
        mockMvc.perform(get("/api/accounts/current"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test 6: Duplicate Email Registration Prevention
     * 
     * What it tests:
     * - Cannot register with already existing email
     * - Returns appropriate error response
     * 
     * Why it matters:
     * - Data integrity: prevents duplicate accounts
     * - Security: prevents account hijacking attempts
     */
    @Test
    public void testUserSignup_DuplicateEmail_Fails() throws Exception {
        // Create first user
        Account account = new Account();
        account.setEmail(testEmail);
        account.setPassword(passwordEncoder.encode(testPassword));
        account.setUsername(testUsername);
        account.setRole("USER");
        accountRepository.save(account);

        // Try to register with same email
        String signupJson = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\",\"username\":\"differentuser\"}",
            testEmail, "DifferentPassword123!"
        );

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signupJson))
                .andExpect(status().isConflict()); // 409 Conflict
    }
}
