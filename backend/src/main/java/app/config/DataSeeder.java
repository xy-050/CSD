package app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import app.account.Account;
import app.account.AccountRepository;
import app.query.Query;
import app.query.QueryRepository;

/**
 * Database seeder that runs after Spring Boot starts and Hibernate creates tables.
 * 
 * PURPOSE:
 * - Automatically populates database with initial data on first startup
 * - Uses BCrypt to hash passwords (same as login system)
 * - Only inserts data if it doesn't already exist (idempotent)
 * 
 * - Runs AFTER Hibernate creates tables (no timing issues)
 * - Uses same PasswordEncoder as your app
 * - Safe to run multiple times
 * 
 * WHEN IT RUNS:
 * - Every time the Spring Boot application starts
 * - Checks if data exists before inserting (won't create duplicates)
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(AccountRepository accountRepo, QueryRepository queryRepo) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            
            System.out.println("=== Starting Database Seeding ===");
            
            // ========== SEED ADMIN ACCOUNT ==========
            System.out.println("Seeding admin account...");
            seedUser(accountRepo, "admin@tarrific.com", "AdminTarrific12345!", "admin", "ADMIN", encoder);
            
            // ========== SEED REGULAR ACCOUNTS ==========
            System.out.println("Seeding user accounts...");
            
            Account rachel = seedUser(accountRepo, "rachel@tarrific.com", "PasswordRachel12345!", "rachel", "USER", encoder);
            Account elodie = seedUser(accountRepo, "elodie@tarrific.com", "PasswordElodie12345!", "elodie", "USER", encoder);
            Account xinyu = seedUser(accountRepo, "xinyu@tarrific.com", "PasswordXinyu12345!", "xinyu", "USER", encoder);
            Account amos = seedUser(accountRepo, "amos@tarrific.com", "PasswordAmos12345!", "amos", "USER", encoder);
            Account roopa = seedUser(accountRepo, "roopa@tarrific.com", "PasswordRoopa12345!", "roopa", "USER", encoder);
            Account jordan = seedUser(accountRepo, "jordan@tarrific.com", "PasswordJordan12345!", "jordan", "USER", encoder);
            
            // Note: Favourites are managed via the Account entity's ManyToMany relationship
            // They will be automatically persisted when accounts are saved
            
            // ========== SEED HISTORICAL QUERIES ==========
            System.out.println("Seeding historical queries...");
            
            if (rachel != null) {
                seedQuery(queryRepo, "8471.30.0100", rachel);
                seedQuery(queryRepo, "8528.72.6400", rachel);
                seedQuery(queryRepo, "8471.30.0100", rachel);
            }
            
            if (elodie != null) {
                seedQuery(queryRepo, "9503.00.0073", elodie);
                seedQuery(queryRepo, "6109.10.0012", elodie);
            }
            
            System.out.println("=== Database Seeding Complete ===");
        };
    }
    
    /**
     * Seeds a user account if it doesn't already exist.
     * 
     * @return The saved Account (or existing one), or null if something went wrong
     */
    private Account seedUser(AccountRepository repo, String email, String rawPassword, String username, String role, BCryptPasswordEncoder encoder) {
        Account existing = repo.findByEmail(email);
        
        if (existing != null) {
            System.out.println("  ✓ User already exists: " + email + " (Role: " + existing.getRole() + ")");
            return existing;
        }
        
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(encoder.encode(rawPassword));  // BCrypt hash
        account.setUsername(username);
        account.setRole(role);  // USER or ADMIN
        
        Account saved = repo.save(account);
        System.out.println("  + Created " + role + ": " + email + " (ID: " + saved.getUserID() + ")");
        return saved;
    }
    
    /**
     * Seeds a historical query.
     * Queries can have duplicates (same user can query same product multiple times)
     * so we don't check for existence.
     */
    private void seedQuery(QueryRepository repo, String htsCode, Account user) {
        Query query = new Query();
        query.setHtsCode(htsCode);
        query.setUserID(user);
        
        Query saved = repo.save(query);
        System.out.println("  + Created query: " + htsCode + " (ID: " + saved.getQueryID() + ")");
    }
}
