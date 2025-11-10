package app.config;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import app.account.Account;
import app.account.AccountRepository;
import app.fta.FTA;
import app.fta.FTARepository;
import app.product.Product;
import app.product.ProductRepository;
import app.product.ProductService;
import app.query.Query;
import app.query.QueryRepository;

/**
 * Database seeder that runs after Spring Boot starts and Hibernate creates tables.
 * Populates database with initial data on first startup.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            AccountRepository accountRepo, 
            QueryRepository queryRepo,
            ProductService productService,
            ProductRepository productRepo,
            FTARepository ftaRepo) {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            
            System.out.println("=== Starting Database Seeding ===");
            
            // ========== SEED PRODUCTS ==========
            System.out.println("Seeding products...");
            seedProducts(productService, productRepo);
            
            // ========== SEED FTAs ==========
            System.out.println("Seeding FTA data...");
            seedFTAs(ftaRepo);
            
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
            
            // ========== SEED HISTORICAL QUERIES ==========
            System.out.println("Seeding historical queries...");
            if (rachel != null) {
                seedQuery(queryRepo, "0404.90.10.00", rachel);
                seedQuery(queryRepo, "0404.90.10.00", rachel);
                seedQuery(queryRepo, "0407.11.00.00", rachel);
                seedQuery(queryRepo, "1905.90.10", rachel);
            }
            
            if (elodie != null) {
                seedQuery(queryRepo, "1905.90.10", elodie);
                seedQuery(queryRepo, "1006.10.00.00", elodie);
                seedQuery(queryRepo, "0404.90.10.00", elodie);
                seedQuery(queryRepo, "2303.20.00", elodie);
            }
            
            System.out.println("=== Database Seeding Complete ===");
        };
    }
    
    /**
     * Seeds products with historical data.
     */
    private void seedProducts(ProductService productService, ProductRepository productRepo) {
        // Check if products already seeded
        if (productRepo.count() > 0) {
            System.out.println("  ✓ Products already seeded (count: " + productRepo.count() + ")");
            return;
        }
        
        // Fetch products from external API
        productService.fetchExternal();
        List<Product> products = productRepo.findAll();
        
        System.out.println("  + Fetched " + products.size() + " products from external API");
        
        // Create historical data for 2022 and 2023
        for (Product product : products) {
            // 2022 version
            product.setFetchDate(LocalDate.of(2022, 1, 1));
            productRepo.save(product);
            
            // 2023 version with special handling
            product.setFetchDate(LocalDate.of(2023, 1, 1));
            if (product.getHtsCode().equals("0407.11.00.00")) {
                product.setGeneral("2.6¢/doz.");
            }
            productRepo.save(product);
        }
        
        System.out.println("  + Created historical product data for 2022 and 2023");
    }
    
    /**
     * Seeds FTA (Free Trade Agreement) data.
     */
    private void seedFTAs(FTARepository ftaRepo) {
        // Check if FTAs already seeded
        if (ftaRepo.count() > 0) {
            System.out.println("  ✓ FTAs already seeded (count: " + ftaRepo.count() + ")");
            return;
        }
        
        List<FTA> ftas = List.of(
            new FTA("AU", "0407.11.00.00", "Free", LocalDate.of(2028, 1, 1)),
            new FTA("NZ", "0407.11.00.00", "2¢/doz", LocalDate.of(2026, 1, 1)),
            new FTA("NZ", "0407.11.00.00", "1.4¢/doz", LocalDate.of(2028, 1, 1)),
            new FTA("NZ", "0407.11.00.00", "1.2¢/doz", LocalDate.of(2030, 1, 1)),
            new FTA("NZ", "0407.11.00.00", "1.1¢/doz", LocalDate.of(2031, 1, 1)),
            new FTA("NZ", "0407.11.00.00", "0.7¢/doz", LocalDate.of(2033, 1, 1)),
            new FTA("NZ", "0407.11.00.00", "Free", LocalDate.of(2035, 1, 1))
        );
        
        for (FTA fta : ftas) {
            ftaRepo.save(fta);
        }
        
        System.out.println("  + Created " + ftas.size() + " FTA records");
    }
    
    /**
     * Seeds a user account if it doesn't already exist.
     */
    private Account seedUser(AccountRepository repo, String email, String rawPassword, 
                            String username, String role, BCryptPasswordEncoder encoder) {
        Optional<Account> existing = repo.findByEmail(email);
        
        if (existing.isPresent()) {
            Account acc = existing.get();
            System.out.println("  ✓ User already exists: " + email + " (Role: " + acc.getRole() + ")");
            return acc;
        }
        
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(encoder.encode(rawPassword));
        account.setUsername(username);
        account.setRole(role);
        
        Account saved = repo.save(account);
        System.out.println("  + Created " + role + ": " + email + " (ID: " + saved.getUserID() + ")");
        return saved;
    }
    
    /**
     * Seeds a historical query.
     */
    private void seedQuery(QueryRepository repo, String htsCode, Account user) {
        Query query = new Query();
        query.setHtsCode(htsCode);
        query.setAccount(user);
        
        Query saved = repo.save(query);
        System.out.println("  + Created query: " + htsCode + " for user " + user.getUsername());
    }
}