# Database Seeding Guide

## Overview
This guide explains how to pre-fill your MySQL database with user accounts, historical queries, and tariff data when using Docker.

## Files Involved

### 1. `SeedDataGenerator.java`
**Location**: `src/main/java/app/utility/SeedDataGenerator.java`

**Purpose**: 
- Java utility that generates SQL seed data with BCrypt-hashed passwords
- Uses Spring Security's BCryptPasswordEncoder (same as your app uses for login)
- Creates idempotent SQL statements (safe to re-run without creating duplicates)

**How it works**:
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
String hashedPassword = encoder.encode("PasswordRachel12345!");
// Generates: $2a$10$kGn2wEaUOraL9Wc6e8xA6ec31RlTMLinCJvEU0L0NodsVenOkGJkK
```

**Why BCrypt**:
- One-way hash function designed specifically for passwords
- Includes automatic salting (random data added to each password before hashing)
- The "10" parameter is the cost factor (higher = more secure but slower)
- Our Spring Security configuration uses BCrypt, so seed data must match

**How to run it**:
```powershell
# From backend directory
mvn exec:java '-Dexec.mainClass=app.utility.SeedDataGenerator'
```

**Output**: Generates `seed-data.sql` in the backend directory

---

### 2. `seed-data.sql`
**Location**: `backend/seed-data.sql`

**Purpose**:
- Auto-generated SQL file containing all seed data with BCrypt-hashed passwords
- Used by MySQL during container initialization

**What it contains**:
1. **User Accounts** (6 team members + 1 admin):
   - rachel@tarrific.com
   - elodie@tarrific.com
   - xinyu@tarrific.com
   - amos@tarrific.com
   - roopa@tarrific.com
   - jordan@tarrific.com
   - admin@ tarrific.com
   
   All passwords are BCrypt-hashed (plaintext versions are in SeedDataGenerator.java for reference)

2. **Historical Queries** (1 sample query for each hts code):
   - Past tariff lookups by users for analytics

**SQL Pattern (Idempotent)**:
```sql
INSERT INTO account (email, password, username)
SELECT 'rachel@tarrific.com', '$2a$10$...hashed...', 'rachel' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM account WHERE email = 'rachel@tarrific.com');
```

This pattern ensures:
- Only inserts if the email doesn't already exist
- Safe to run multiple times (won't create duplicates)
- Uses `FROM DUAL` (MySQL syntax for single-row selects)

---

### 3. `init.sql`
**Location**: `backend/init.sql`

**Purpose**:
- Creates MySQL users and sets permissions
- Runs BEFORE seed-data.sql

**What it does**:
```sql
CREATE USER IF NOT EXISTS 'sa'@'%' IDENTIFIED BY '';
CREATE USER IF NOT EXISTS 'sa'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON `TarrificDB`.* TO 'sa'@'%';
GRANT ALL PRIVILEGES ON `TarrificDB`.* TO 'sa'@'localhost';
FLUSH PRIVILEGES;
```

**Why it exists**:
- Docker MySQL containers need explicit user creation
- `sa` user with empty password matches your application.properties configuration
- Runs first (alphabetically: 01-init.sql before 02-seed-data.sql)

---

### 4. `docker-compose.yml`
**Location**: `backend/docker-compose.yml`

**Relevant section**:
```yaml
mysql:
  volumes:
    - mysql-data:/var/lib/mysql
    - ./init.sql:/docker-entrypoint-initdb.d/01-init.sql
    - ./seed-data.sql:/docker-entrypoint-initdb.d/02-seed-data.sql
```

**How Docker MySQL init works**:
1. MySQL container starts for the FIRST TIME (fresh volume)
2. Looks in `/docker-entrypoint-initdb.d/` directory
3. Executes all `.sql` files in **alphabetical order**:
   - `01-init.sql` → Creates users and permissions
   - `02-seed-data.sql` → Inserts seed data
4. On subsequent starts, these scripts are **SKIPPED** (data already exists)

**Important**: Init scripts only run when the MySQL data volume is empty!

---

## How to Use This System

### Step 1: Modify Seed Data (Optional)
If you want to change the seed data:

1. Edit `SeedDataGenerator.java`
2. Update the arrays:
   ```java
   String[][] users = {
       {"email", "plaintext_password", "username"},
       // Add more users here
   };
   
   String[][] queries = {
       {"hts_code", "transport_mode", "origin", "quantity", "userId"},
       // Add more historical queries here
   };
   ```

3. Re-run the generator:
   ```powershell
   mvn exec:java '-Dexec.mainClass=app.utility.SeedDataGenerator'
   ```

### Step 2: Reset Database (To Apply Changes)
Since init scripts only run on first startup, you must reset the database to re-run them:

```powershell
# Stop containers and remove volumes
docker compose down -v

# Restart (this triggers init scripts again)
docker compose up --build -d
```

**Warning**: `-v` flag deletes all database data! Only use this in development.

### Step 3: Verify Seeded Data

Check accounts:
```powershell
docker exec -it TarrificDB-mysql mysql -u sa -D TarrificDB -e "SELECT userID, email, username FROM account;"
```

Check queries:
```powershell
docker exec -it TarrificDB-mysql mysql -u sa -D TarrificDB -e "SELECT * FROM query;"
```

Check favourites:
```powershell
docker exec -it TarrificDB-mysql mysql -u sa -D TarrificDB -e "SELECT * FROM favourite_hts_codes;"
```

---

## Entity Structure Mapping

Your JPA entities map to these tables:

### Account Entity → `account` table
```java
@Entity
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;       // Auto-increment primary key
    private String username;
    private String password;      // BCrypt-hashed
    private String email;         // Unique identifier
    
    @OneToMany
    private List<Query> queries;
    
    @ManyToMany
    private Set<FavouriteHtsCodes> favouriteHtsCodes;
}
```

### Query Entity → `query` table
```java
@Entity
public class Query {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long queryID;
    
    @ManyToOne
    @JoinColumn(name = "userID")  // Foreign key column name
    private Account userID;        // Reference to Account
    
    private String htsCode;
    private String originCountry;
    private String modeOfTransport;
    private int quantity;
}
```

**Important Note**: The `@JoinColumn(name = "userID")` means the database column is named `userID` (not `userid`). This is why the generated SQL uses `userID`.

### FavouriteHtsCodes Entity → `favourite_hts_codes` table
```java
@Entity
public class FavouriteHtsCodes {
    @Id
    private String htsCode;  // Primary key (String, not auto-increment)
    
    @ManyToMany(mappedBy = "favouriteHtsCodes")
    private Set<Account> accounts;
}
```

---

## Troubleshooting

### Problem: "Access denied for user 'sa'"
**Solution**: Ensure `init.sql` creates the user properly and runs before seed-data.sql

### Problem: "Table doesn't exist"
**Cause**: Init scripts run BEFORE Spring Boot creates tables via Hibernate

**Solutions**:
1. **Current approach**: Let Hibernate create tables first, then manually run seed SQL later
2. **Better approach**: Use Flyway migrations to control exact order
3. **Alternative**: Define schema in SQL within init scripts (not relying on Hibernate)

### Problem: Duplicate entries on restart
**Why**: If you manually run SQL without idempotent checks

**Solution**: Use the `WHERE NOT EXISTS` pattern (already in seed-data.sql)

### Problem: Password doesn't work after seeding
**Cause**: Password not BCrypt-hashed or wrong hash algorithm

**Solution**: Always use SeedDataGenerator.java to create seed data (ensures correct BCrypt hashing)

---

## Production Considerations

**DO NOT use this approach in production as-is!**

For production:
1. **Use environment variables** for all passwords (never hardcode in SQL)
2. **Use a migration tool** (Flyway/Liquibase) for versioned schema + data changes
3. **Store secrets securely** (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault)
4. **Never commit real passwords** to Git (even hashed)
5. **Use stronger BCrypt cost factors** (12-14 instead of 10)

---

## Quick Reference

| File | Purpose | When it runs |
|------|---------|--------------|
| `SeedDataGenerator.java` | Generates SQL with hashed passwords | Manually via Maven |
| `seed-data.sql` | Contains seed data to insert | On first Docker MySQL startup |
| `init.sql` | Creates MySQL users | On first Docker MySQL startup (before seed-data.sql) |
| `docker-compose.yml` | Mounts SQL files into container | Docker Compose startup |

**Workflow**:
1. Modify `SeedDataGenerator.java` → Run generator → Generates `seed-data.sql`
2. `docker compose down -v` → Deletes old data
3. `docker compose up --build` → Runs init.sql, seed-data.sql, starts app
4. App connects to MySQL with pre-filled data

---

## Summary

This system provides:
✅ Secure password hashing (BCrypt) matching your app's authentication
✅ Idempotent SQL (safe to re-run)
✅ Automatic seeding on fresh database creation
✅ Easy modification via Java code
✅ Historical queries and user relationships pre-populated

You now have a fully automated database seeding system integrated with Docker!
