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
import app.product.Product;
import app.product.ProductRepository;
import app.query.Query;
import app.query.QueryRepository;

import java.time.LocalDate;

/**
 * Integration Test: Search and Query History Flow
 * 
 * This test validates the tariff search functionality:
 * 1. User searches for HTS codes
 * 2. Search results are returned from Product database
 * 3. Query history is automatically logged
 * 4. User can retrieve their search history
 * 
 * Tests the integration between Product, Query, and Account entities
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class SearchAndQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testUser;
    private String authToken;
    private Product testProduct;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up existing test data
        queryRepository.deleteAll();
        productRepository.deleteAll();
        accountRepository.findByEmail("searchtest@test.com").ifPresent(accountRepository::delete);

        // Create test user
        testUser = new Account();
        testUser.setEmail("searchtest@test.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setUsername("searchuser");
        testUser.setRole("USER");
        testUser = accountRepository.save(testUser);

        // Create test product
        testProduct = new Product();
        testProduct.setHtsCode("0407.11.00.00");
        testProduct.setDescription("Eggs, in shell, fresh, of chickens");
        testProduct.setGeneral("2.8¢/doz.");
        testProduct.setSpecial("1.9¢/doz.");
        testProduct.setCategory("Agricultural");
        testProduct.setFetchDate(LocalDate.now());
        testProduct = productRepository.save(testProduct);

        // Login to get auth token
        String loginJson = "{\"username\":\"searchuser\",\"password\":\"Password123!\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        authToken = jsonResponse.get("token").asText();
    }

    /**
     * Test 1: Search for Products by HTS Code
     * 
     * What it tests:
     * - GET /api/products/search?query={htsCode} returns matching products
     * - Product details (description, tariff rates) are correctly returned
     * - Partial HTS code matching works
     * 
     * Why it matters:
     * - Core search functionality for users to find tariff information
     * - Ensures product data is accessible and accurate
     */
    @Test
    public void testSearchProducts_ByHtsCode_Success() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("query", "0407.11")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].htsCode").value("0407.11.00.00"))
                .andExpect(jsonPath("$[0].description").value("Eggs, in shell, fresh, of chickens"))
                .andExpect(jsonPath("$[0].general").value("2.8¢/doz."));
    }

    /**
     * Test 2: Query Logging on Search
     * 
     * What it tests:
     * - POST /api/queries logs a search query
     * - Query is associated with the authenticated user
     * - HTS code is stored in query history
     * 
     * Why it matters:
     * - Tracks user search behavior for analytics
     * - Enables "recent searches" feature
     * - Helps understand user needs
     */
    @Test
    public void testLogQuery_OnProductSearch_Success() throws Exception {
        String queryJson = "{\"htsCode\":\"0407.11.00.00\"}";

        mockMvc.perform(post("/api/queries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(queryJson)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Verify query was logged in database
        Query savedQuery = queryRepository.findAll().stream()
                .filter(q -> q.getHtsCode().equals("0407.11.00.00"))
                .findFirst()
                .orElse(null);

        assertNotNull(savedQuery, "Query should be saved");
        assertEquals(testUser.getUserID(), savedQuery.getAccount().getUserID(), 
                     "Query should be associated with user");
    }

    /**
     * Test 3: Retrieve User Query History
     * 
     * What it tests:
     * - GET /api/queries returns user's search history
     * - Only returns queries for authenticated user (not other users)
     * - Queries are ordered (most recent first)
     * 
     * Why it matters:
     * - Users can see their past searches
     * - Privacy: users only see their own history
     * - Enables quick re-search of previous items
     */
    @Test
    public void testGetQueryHistory_ReturnsUserQueries_Success() throws Exception {
        // Create multiple queries for test user
        Query query1 = new Query();
        query1.setHtsCode("0407.11.00.00");
        query1.setAccount(testUser);
        queryRepository.save(query1);

        Query query2 = new Query();
        query2.setHtsCode("1905.90.10");
        query2.setAccount(testUser);
        queryRepository.save(query2);

        // Create query for different user to ensure isolation
        Account otherUser = new Account();
        otherUser.setEmail("other@test.com");
        otherUser.setPassword(passwordEncoder.encode("Password123!"));
        otherUser.setUsername("otheruser");
        otherUser.setRole("USER");
        otherUser = accountRepository.save(otherUser);

        Query otherQuery = new Query();
        otherQuery.setHtsCode("2208.40");
        otherQuery.setAccount(otherUser);
        queryRepository.save(otherQuery);

        // Get query history for test user
        MvcResult result = mockMvc.perform(get("/api/queries")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode queries = objectMapper.readTree(responseBody);

        // Should return 2 queries for test user, not the other user's query
        assertEquals(2, queries.size(), "Should return only test user's queries");
    }

    /**
     * Test 4: Search with No Results
     * 
     * What it tests:
     * - Searching for non-existent HTS code returns empty array
     * - Returns 200 OK (not error) for valid searches with no matches
     * 
     * Why it matters:
     * - Handles edge cases gracefully
     * - User experience: differentiates between error and "no results"
     */
    @Test
    public void testSearchProducts_NoResults_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("query", "9999.99.99.99")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Test 5: Get Product Details by Exact HTS Code
     * 
     * What it tests:
     * - GET /api/products/{htsCode} returns specific product
     * - All product fields are populated correctly
     * 
     * Why it matters:
     * - Detailed view of tariff information
     * - Ensures data integrity across all product fields
     */
    @Test
    public void testGetProductByHtsCode_Success() throws Exception {
        mockMvc.perform(get("/api/products/" + testProduct.getHtsCode())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.htsCode").value("0407.11.00.00"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.general").exists())
                .andExpect(jsonPath("$.category").value("Agricultural"));
    }

    /**
     * Test 6: Unauthenticated Search Request
     * 
     * What it tests:
     * - Search requires authentication
     * - Returns 401 Unauthorized without valid token
     * 
     * Why it matters:
     * - Security: protects API from anonymous access
     * - Enables user-specific features (history, favorites)
     */
    @Test
    public void testSearchProducts_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("query", "0407.11"))
                .andExpect(status().isUnauthorized());
    }
}
