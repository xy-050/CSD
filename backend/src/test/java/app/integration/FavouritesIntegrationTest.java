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
import app.favourites.Favourites;
import app.favourites.FavouritesRepository;

/**
 * Integration Test: Favourites Management Flow
 * 
 * This test validates the favourites/bookmarking functionality:
 * 1. User adds HTS codes to favourites
 * 2. User retrieves their favourites list
 * 3. User removes items from favourites
 * 4. Multiple users have isolated favourites
 * 
 * Tests the ManyToMany relationship between Account and Favourites
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class FavouritesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FavouritesRepository favouritesRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testUser;
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        favouritesRepository.deleteAll();
        accountRepository.findByEmail("favtest@test.com").ifPresent(accountRepository::delete);

        // Create test user
        testUser = new Account();
        testUser.setEmail("favtest@test.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setUsername("favuser");
        testUser.setRole("USER");
        testUser = accountRepository.save(testUser);

        // Get auth token
        String loginJson = "{\"username\":\"favuser\",\"password\":\"Password123!\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        authToken = jsonResponse.get("token").asText();
    }

    /**
     * Test 1: Add HTS Code to Favourites
     * 
     * What it tests:
     * - POST /api/favourites adds an HTS code to user's favourites
     * - Favourite is associated with authenticated user
     * - Returns success response
     * 
     * Why it matters:
     * - Core bookmarking functionality
     * - Allows users to save frequently searched items
     * - Improves user workflow efficiency
     */
    @Test
    public void testAddFavourite_Success() throws Exception {
        String favouriteJson = "{\"htsCode\":\"0407.11.00.00\"}";

        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(favouriteJson)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify favourite was saved
        Favourites savedFav = favouritesRepository.findByHtsCode("0407.11.00.00").orElse(null);
        assertNotNull(savedFav, "Favourite should be saved");
    }

    /**
     * Test 2: Get User's Favourites List
     * 
     * What it tests:
     * - GET /api/favourites returns all user's favourites
     * - Only returns favourites for authenticated user
     * - Returns empty array if no favourites
     * 
     * Why it matters:
     * - Users can view their bookmarked items
     * - Privacy: isolated favourites per user
     * - Quick access to frequently used HTS codes
     */
    @Test
    public void testGetFavourites_ReturnsUserFavourites_Success() throws Exception {
        // Add favourites directly to database
        Favourites fav1 = new Favourites();
        fav1.setHtsCode("0407.11.00.00");
        favouritesRepository.save(fav1);

        Favourites fav2 = new Favourites();
        fav2.setHtsCode("1905.90.10");
        favouritesRepository.save(fav2);

        // Link to user
        testUser.getFavourites().add(fav1);
        testUser.getFavourites().add(fav2);
        accountRepository.save(testUser);

        // Get favourites
        MvcResult result = mockMvc.perform(get("/api/favourites")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode favourites = objectMapper.readTree(responseBody);

        assertEquals(2, favourites.size(), "Should return 2 favourites");
    }

    /**
     * Test 3: Remove Favourite
     * 
     * What it tests:
     * - DELETE /api/favourites/{id} removes favourite from user's list
     * - Favourite is actually deleted from database
     * - Returns success status
     * 
     * Why it matters:
     * - Users can manage their favourites list
     * - Prevents clutter from outdated bookmarks
     * - Gives users control over their data
     */
    @Test
    public void testRemoveFavourite_Success() throws Exception {
        // Create favourite
        Favourites favourite = new Favourites();
        favourite.setHtsCode("0407.11.00.00");
        favourite = favouritesRepository.save(favourite);

        // Link to user
        testUser.getFavourites().add(favourite);
        accountRepository.save(testUser);

        // Remove favourite
        mockMvc.perform(delete("/api/favourites/" + favourite.getFavouritesID())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify removed
        assertFalse(favouritesRepository.existsById(favourite.getFavouritesID()),
                    "Favourite should be deleted");
    }

    /**
     * Test 4: Prevent Duplicate Favourites
     * 
     * What it tests:
     * - Cannot add same HTS code to favourites twice
     * - Returns appropriate error/success response
     * 
     * Why it matters:
     * - Data integrity: no duplicate bookmarks
     * - Better user experience: clean favourites list
     */
    @Test
    public void testAddDuplicateFavourite_HandledGracefully() throws Exception {
        // Add favourite first time
        String favouriteJson = "{\"htsCode\":\"0407.11.00.00\"}";
        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(favouriteJson)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Try to add same favourite again
        mockMvc.perform(post("/api/favourites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(favouriteJson)
                .header("Authorization", "Bearer " + authToken));
        // Should either succeed (idempotent) or return conflict status

        // Verify only one favourite exists
        long count = favouritesRepository.findAll().stream()
                .filter(f -> f.getHtsCode().equals("0407.11.00.00"))
                .count();
        assertEquals(1, count, "Should have exactly one favourite for this HTS code");
    }

    /**
     * Test 5: Favourites Isolation Between Users
     * 
     * What it tests:
     * - User A cannot see User B's favourites
     * - Each user has their own favourites list
     * 
     * Why it matters:
     * - Privacy and data isolation
     * - Multi-tenant security
     * - Prevents data leakage between users
     */
    @Test
    public void testFavouritesIsolation_BetweenUsers() throws Exception {
        // Create second user
        Account user2 = new Account();
        user2.setEmail("favtest2@test.com");
        user2.setPassword(passwordEncoder.encode("Password123!"));
        user2.setUsername("favuser2");
        user2.setRole("USER");
        user2 = accountRepository.save(user2);

        // Add favourite to user2
        Favourites fav2 = new Favourites();
        fav2.setHtsCode("9999.99.99.99");
        fav2 = favouritesRepository.save(fav2);
        user2.getFavourites().add(fav2);
        accountRepository.save(user2);

        // Add favourite to testUser
        Favourites fav1 = new Favourites();
        fav1.setHtsCode("0407.11.00.00");
        fav1 = favouritesRepository.save(fav1);
        testUser.getFavourites().add(fav1);
        accountRepository.save(testUser);

        // Get testUser's favourites
        MvcResult result = mockMvc.perform(get("/api/favourites")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode favourites = objectMapper.readTree(responseBody);

        // Should only see testUser's favourite, not user2's
        assertEquals(1, favourites.size(), "Should only see own favourites");
        assertEquals("0407.11.00.00", favourites.get(0).get("htsCode").asText());
    }

    /**
     * Test 6: Unauthenticated Favourites Access
     * 
     * What it tests:
     * - Favourites endpoints require authentication
     * - Returns 401 without valid token
     * 
     * Why it matters:
     * - Security: prevents anonymous access
     * - Favourites are user-specific features
     */
    @Test
    public void testGetFavourites_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(get("/api/favourites"))
                .andExpect(status().isUnauthorized());
    }
}
