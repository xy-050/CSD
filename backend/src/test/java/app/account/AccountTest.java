package app.account;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import app.favourites.Favourites;
import app.query.Query;

public class AccountTest {

    private Account account1;
    private Account account2;
    private Query query1;
    private Query query2;
    private Favourites favourite1;
    private Favourites favourite2;

    @BeforeEach
    void setUp() {
        account1 = new Account();
        account1.setUserID(1);
        account1.setUsername("testuser1");
        account1.setEmail("test1@example.com");
        account1.setPassword("password123");
        account1.setRole("USER");

        account2 = new Account();
        account2.setUserID(2);
        account2.setUsername("testuser2");
        account2.setEmail("test2@example.com");
        account2.setPassword("password456");
        account2.setRole("ADMIN");

        query1 = new Query();
        query1.setQueryID(1L);
        query1.setHtsCode("1704.90.35");

        query2 = new Query();
        query2.setQueryID(2L);
        query2.setHtsCode("1704.90.36");

        favourite1 = new Favourites();
        favourite1.setHtsCode("1704.90.35");

        favourite2 = new Favourites();
        favourite2.setHtsCode("1704.90.36");
    }

    // -------------------------------------------------------------------
    // --------------- Entity Relationships Tests ------------------------
    // -------------------------------------------------------------------

    @Test
    void account_WhenQueriesAdded_ShouldMaintainOneToManyRelationship() {
        // Arrange
        List<Query> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);
        account1.setQueries(queries);

        // Act & Assert
        assertNotNull(account1.getQueries());
        assertEquals(2, account1.getQueries().size());
        assertTrue(account1.getQueries().contains(query1));
        assertTrue(account1.getQueries().contains(query2));
    }

    @Test
    void account_WhenFavouritesAdded_ShouldMaintainManyToManyRelationship() {
        // Arrange
        Set<Favourites> favourites = new HashSet<>();
        favourites.add(favourite1);
        favourites.add(favourite2);
        account1.setFavourites(favourites);

        // Act & Assert
        assertNotNull(account1.getFavourites());
        assertEquals(2, account1.getFavourites().size());
        assertTrue(account1.getFavourites().contains(favourite1));
        assertTrue(account1.getFavourites().contains(favourite2));
    }

    @Test
    void account_WhenNoQueries_ShouldReturnEmptyList() {
        // Arrange
        account1.setQueries(new ArrayList<>());

        // Act & Assert
        assertNotNull(account1.getQueries());
        assertTrue(account1.getQueries().isEmpty());
    }

    @Test
    void account_WhenNoFavourites_ShouldReturnEmptySet() {
        // Arrange
        account1.setFavourites(new HashSet<>());

        // Act & Assert
        assertNotNull(account1.getFavourites());
        assertTrue(account1.getFavourites().isEmpty());
    }

    @Test
    void account_WhenQueriesNull_ShouldHandleGracefully() {
        // Arrange
        account1.setQueries(null);

        // Act & Assert
        assertNull(account1.getQueries());
    }

    @Test
    void account_WhenFavouritesNull_ShouldHandleGracefully() {
        // Arrange
        account1.setFavourites(null);

        // Act & Assert
        assertNull(account1.getFavourites());
    }

    @Test
    void account_WhenDefaultConstructor_ShouldInitializeCollections() {
        // Arrange
        Account account = new Account();

        // Act & Assert
        assertNotNull(account.getQueries());
        assertNotNull(account.getFavourites());
        assertTrue(account.getQueries().isEmpty());
        assertTrue(account.getFavourites().isEmpty());
    }

    @Test
    void account_WhenAllArgsConstructor_ShouldSetAllFields() {
        // Arrange
        List<Query> queries = List.of(query1);
        Set<Favourites> favourites = Set.of(favourite1);

        // Act
        Account account = new Account(3, "user3", "pass123", "user3@example.com", "USER", queries, favourites);

        // Assert
        assertEquals(3, account.getUserID());
        assertEquals("user3", account.getUsername());
        assertEquals("pass123", account.getPassword());
        assertEquals("user3@example.com", account.getEmail());
        assertEquals("USER", account.getRole());
        assertEquals(queries, account.getQueries());
        assertEquals(favourites, account.getFavourites());
    }

    // -------------------------------------------------------------------
    // --------------------- Cascade Operations Tests --------------------
    // -------------------------------------------------------------------

    @Test
    void account_WhenQueryAddedToAccount_ShouldMaintainBidirectionalRelationship() {
        // Arrange
        Query query = new Query();
        query.setHtsCode("1704.90.35");
        query.setAccount(account1);
        
        List<Query> queries = new ArrayList<>();
        queries.add(query);
        account1.setQueries(queries);

        // Act & Assert
        assertTrue(account1.getQueries().contains(query));
        assertEquals(account1, query.getAccount());
    }

    @Test
    void account_WhenFavouriteAddedToAccount_ShouldMaintainBidirectionalRelationship() {
        // Arrange
        Favourites favourite = new Favourites();
        favourite.setHtsCode("1704.90.35");
        
        Set<Account> accounts = new HashSet<>();
        accounts.add(account1);
        favourite.setAccounts(accounts);

        Set<Favourites> favourites = new HashSet<>();
        favourites.add(favourite);
        account1.setFavourites(favourites);

        // Act & Assert
        assertTrue(account1.getFavourites().contains(favourite));
        assertTrue(favourite.getAccounts().contains(account1));
    }
}