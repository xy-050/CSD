# Integration Testing Guide

## Overview

Integration tests validate that different components of the application work correctly together. Unlike unit tests (which test individual methods in isolation), integration tests verify:
- Database interactions
- API endpoints (controllers)
- Authentication and authorization
- Business logic flow across multiple layers
- Data persistence and retrieval

## Test Structure

We've created 4 comprehensive integration test suites:

### 1. AuthenticationIntegrationTest
**What it tests:** User authentication and security flow

**Scenarios covered:**
- ✅ User signup (registration)
- ✅ User login with valid credentials
- ✅ JWT token generation
- ✅ Protected endpoint access with JWT
- ✅ Login failure with invalid credentials
- ✅ Duplicate email prevention
- ✅ Unauthorized access prevention

**Why it matters:**
- Ensures security infrastructure works end-to-end
- Validates JWT authentication chain
- Confirms user registration process
- Tests authorization controls

### 2. SearchAndQueryIntegrationTest
**What it tests:** Product search and query history tracking

**Scenarios covered:**
- ✅ Search products by HTS code
- ✅ Automatic query logging when users search
- ✅ Retrieve user's search history
- ✅ Query isolation (users only see their own history)
- ✅ Handle searches with no results
- ✅ Get detailed product information
- ✅ Authentication requirement enforcement

**Why it matters:**
- Core functionality: finding tariff information
- Analytics: understanding user search behavior
- User experience: search history feature
- Privacy: data isolation between users

### 3. FavouritesIntegrationTest
**What it tests:** Bookmark/favourites management

**Scenarios covered:**
- ✅ Add HTS codes to favourites
- ✅ Retrieve user's favourites list
- ✅ Remove items from favourites
- ✅ Prevent duplicate favourites
- ✅ Favourites isolation between users
- ✅ Authentication requirement

**Why it matters:**
- Productivity: quick access to frequently used items
- User workflow optimization
- Data integrity: proper ManyToMany relationships
- Security: favourites privacy

### 4. AccountManagementIntegrationTest
**What it tests:** User profile and account management

**Scenarios covered:**
- ✅ View current user profile
- ✅ Update username
- ✅ Update email address
- ✅ Change password (with verification)
- ✅ Wrong password rejection
- ✅ Admin view all accounts
- ✅ Role-based access control (RBAC)
- ✅ Admin change user roles
- ✅ Prevent unauthorized account modifications

**Why it matters:**
- User self-service: profile management
- Security: password changes, access controls
- Administration: user management
- Privacy: users can only modify their own data

## Running the Tests

### Option 1: Run All Integration Tests
```bash
cd backend
mvn test -Dtest="**/*IntegrationTest"
```

### Option 2: Run Specific Test Suite
```bash
# Authentication tests only
mvn test -Dtest=AuthenticationIntegrationTest

# Search tests only
mvn test -Dtest=SearchAndQueryIntegrationTest

# Favourites tests only
mvn test -Dtest=FavouritesIntegrationTest

# Account management tests only
mvn test -Dtest=AccountManagementIntegrationTest
```

### Option 3: Run Specific Test Method
```bash
mvn test -Dtest=AuthenticationIntegrationTest#testUserLogin_Success
```

### Option 4: Run in IDE
- Right-click on test file → Run
- Right-click on test method → Run
- Use test runner panel

## Test Configuration

Integration tests use a separate configuration:
- **File:** `src/test/resources/application-test.properties`
- **Database:** H2 in-memory (isolated from production)
- **Schema:** Auto-created and dropped for each test
- **Data:** Clean state for every test run

### Key Configuration Settings:

```properties
# In-memory database (fast, isolated)
spring.datasource.url=jdbc:h2:mem:testdb

# Auto-create/drop schema (clean slate)
spring.jpa.hibernate.ddl-auto=create-drop

# Test JWT secret (different from production)
jwt.secret=testSecretKeyForIntegrationTestsOnly123456789
```

## Understanding Test Annotations

### @SpringBootTest
- Loads full Spring application context
- All beans are available (repositories, services, etc.)
- Mimics real application environment

### @AutoConfigureMockMvc
- Automatically configures MockMvc
- MockMvc = simulates HTTP requests without starting server
- Fast execution, no need to deploy

### @TestPropertySource
- Loads test-specific configuration
- Overrides application.properties with application-test.properties

### @Transactional
- Each test runs in a transaction
- Auto-rollback after test completes
- Ensures clean database state between tests

### @BeforeEach
- Runs before each test method
- Sets up test data (users, tokens, etc.)
- Ensures consistent starting state

## Test Data Strategy

### Clean Slate Approach
Each test:
1. Deletes existing test data (BeforeEach)
2. Creates fresh test data
3. Runs assertions
4. Auto-rollback (via @Transactional)

### Example Setup:
```java
@BeforeEach
void setUp() throws Exception {
    // Clean up
    accountRepository.findByEmail("test@test.com")
        .ifPresent(accountRepository::delete);
    
    // Create fresh test user
    Account testUser = new Account();
    testUser.setEmail("test@test.com");
    testUser.setPassword(passwordEncoder.encode("Password123!"));
    testUser = accountRepository.save(testUser);
    
    // Get auth token
    String loginJson = "{\"username\":\"testuser\",\"password\":\"Password123!\"}";
    MvcResult result = mockMvc.perform(post("/api/auth/login")...)
    authToken = extractToken(result);
}
```

## Reading Test Results

### Successful Test Output:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running app.integration.AuthenticationIntegrationTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

### Failed Test Output:
```
[ERROR] Tests run: 6, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failures:
[ERROR]   testUserLogin_Success:95 
      Expected status 200 but was 401
```

## Common Test Patterns

### 1. Create → Action → Verify
```java
@Test
public void testAddFavourite_Success() {
    // CREATE: prepare data
    String favouriteJson = "{\"htsCode\":\"0407.11.00.00\"}";
    
    // ACTION: perform operation
    mockMvc.perform(post("/api/favourites")
            .content(favouriteJson)
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    
    // VERIFY: check result
    Favourites saved = repository.findByHtsCode("0407.11.00.00");
    assertNotNull(saved);
}
```

### 2. Setup → Execute → Assert
```java
@Test
public void testQueryIsolation() {
    // SETUP: create two users with different queries
    createUserWithQuery(user1, "0407.11");
    createUserWithQuery(user2, "1905.90");
    
    // EXECUTE: get user1's queries
    MvcResult result = mockMvc.perform(
        get("/api/queries").header("Authorization", user1Token))
        .andReturn();
    
    // ASSERT: user1 sees only their queries
    JsonNode queries = parse(result);
    assertEquals(1, queries.size());
}
```

### 3. Negative Testing (Error Cases)
```java
@Test
public void testLogin_WrongPassword_Fails() {
    // Expect failure
    mockMvc.perform(post("/api/auth/login")
            .content("{\"username\":\"user\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
}
```

## Debugging Failed Tests

### 1. Enable SQL Logging
In `application-test.properties`:
```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### 2. Print Response Bodies
```java
MvcResult result = mockMvc.perform(...)
    .andReturn();
System.out.println("Response: " + result.getResponse().getContentAsString());
```

### 3. Use Debugger
- Set breakpoints in test methods
- Inspect database state
- Step through test execution

### 4. Check Test Isolation
- Verify @BeforeEach cleans up properly
- Check @Transactional rollback
- Ensure no shared state between tests

## Best Practices

### ✅ DO:
- Test happy path AND error cases
- Use descriptive test method names
- Add comments explaining WHY tests matter
- Clean up test data in @BeforeEach
- Use realistic test data
- Test security boundaries
- Verify database state after operations

### ❌ DON'T:
- Depend on test execution order
- Use production database for tests
- Skip cleanup (causes flaky tests)
- Test implementation details (test behavior)
- Use Thread.sleep() (use proper mocking)
- Hardcode test data (use variables)
- Ignore test failures

## Integration vs Unit Tests

| Aspect | Unit Tests | Integration Tests |
|--------|-----------|-------------------|
| **Scope** | Single method/class | Multiple components |
| **Speed** | Very fast (milliseconds) | Slower (seconds) |
| **Database** | Mocked | Real (H2 in-memory) |
| **Dependencies** | Mocked | Real Spring beans |
| **Isolation** | Complete | Test transactions |
| **Purpose** | Test logic | Test interactions |
| **Example** | `AccountService.updateEmail()` | Login → Get Profile → Update |

## Continuous Integration (CI)

Tests run automatically on:
- Git push
- Pull request creation
- Pre-deployment checks

Prevents broken code from reaching production.

## Coverage Report

To generate test coverage:
```bash
mvn clean test jacoco:report
```

View report: `target/site/jacoco/index.html`

## Troubleshooting

### Issue: Tests fail but app works
**Solution:** Check test database configuration differs from dev/prod

### Issue: Flaky tests (sometimes pass, sometimes fail)
**Solution:** Ensure @Transactional rollback, check test isolation

### Issue: "Table not found" errors
**Solution:** Verify `spring.jpa.hibernate.ddl-auto=create-drop` in test config

### Issue: Authentication fails in tests
**Solution:** Check JWT secret in application-test.properties

### Issue: Slow test execution
**Solution:** Use H2 instead of MySQL for tests, reduce data volume

## Next Steps

1. **Run the tests:**
   ```bash
   cd backend
   mvn test -Dtest="**/*IntegrationTest"
   ```

2. **Review test output:** Check all tests pass

3. **Add more tests:** Cover edge cases specific to your use case

4. **Set up CI/CD:** Automate test execution on every commit

5. **Monitor coverage:** Aim for 80%+ coverage of critical paths

## Questions?

- **What is MockMvc?** A Spring testing tool that simulates HTTP requests without starting a server
- **Why H2 database?** Fast in-memory database, perfect for testing, automatically cleans up
- **What is @Transactional?** Ensures each test runs in isolated transaction that rolls back after test
- **How do I test my own endpoints?** Follow the patterns in existing tests, add to appropriate test suite
