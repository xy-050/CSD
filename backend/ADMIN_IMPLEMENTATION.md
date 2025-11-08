# Admin Account Implementation Guide

## 🎯 Overview

Your application now has **role-based access control** with two user types:
- **ADMIN**: Full system access including user management
- **USER**: Standard application access

## 🔑 Admin Credentials

**Default admin account (seeded automatically):**
```
Email:    admin@tarrific.com
Username: admin
Password: AdminTarrific2024!
Role:     ADMIN
```

⚠️ **IMPORTANT**: Change this password in production! Edit `DataSeeder.java` line 42 before deploying.

## 📋 What Changed

### 1. Account Entity (`Account.java`)
**Added:**
```java
private String role;  // USER or ADMIN
```

This field stores the user's role and is used by Spring Security for authorization.

### 2. Database Seeder (`DataSeeder.java`)
**Changed:**
- Added admin account seeding (runs first)
- Updated all regular users to have role "USER"
- Modified `seedUser()` to accept and set the role

**Seeded Accounts:**
| Email | Username | Role | Password |
|-------|----------|------|----------|
| admin@tarrific.com | admin | ADMIN | AdminTarrific2024! |
| rachel@tarrific.com | rachel | USER | PasswordRachel12345! |
| elodie@tarrific.com | elodie | USER | PasswordElodie12345! |
| xinyu@tarrific.com | xinyu | USER | PasswordXinyu12345! |
| amos@tarrific.com | amos | USER | PasswordAmos12345! |
| roopa@tarrific.com | roopa | USER | PasswordRoopa12345! |
| jordan@tarrific.com | jordan | USER | PasswordJordan12345! |

### 3. User Details Service (`UserDetailsServiceImpl.java`)
**Changed:**
- Now reads `role` from Account entity
- Uses "USER" as default if role is null (backwards compatibility)
- Passes role to Spring Security

**Code:**
```java
String role = account.getRole() != null ? account.getRole() : "USER";
return User.builder()
    .username(account.getUsername())
    .password(account.getPassword())
    .roles(role)  // Now uses database role instead of hardcoded "USER"
    .build();
```

### 4. Security Configuration (`SecurityConfig.java`)
**Added:**
- `@EnableMethodSecurity` annotation (enables `@PreAuthorize`)
- Admin-only route protection: `.requestMatchers("/api/admin/**").hasRole("ADMIN")`

This ensures only users with ADMIN role can access `/api/admin/*` endpoints.

### 5. Admin Controller (`AdminController.java`) **[NEW FILE]**
Created admin-only REST API endpoints for user management.

**Endpoints:**

#### GET `/api/admin/users`
Lists all user accounts in the system.

**Response:**
```json
[
  {
    "userID": 1,
    "username": "admin",
    "email": "admin@tarrific.com",
    "role": "ADMIN"
  },
  {
    "userID": 2,
    "username": "rachel",
    "email": "rachel@tarrific.com",
    "role": "USER"
  }
]
```

#### DELETE `/api/admin/users/{userId}`
Deletes a user account.

**Example:**
```bash
DELETE /api/admin/users/5
```

#### GET `/api/admin/stats`
Returns system statistics.

**Response:**
```json
{
  "totalUsers": 7,
  "totalAdmins": 1
}
```

#### PUT `/api/admin/users/{userId}/role`
Changes a user's role (promote to admin or demote to user).

**Request Body:**
```json
{
  "role": "ADMIN"
}
```

**Example:**
```bash
PUT /api/admin/users/2/role
Body: { "role": "ADMIN" }
```

## 🔒 How Authorization Works

### Spring Security Role Mapping

When a user logs in:
1. `UserDetailsServiceImpl.loadUserByUsername()` is called
2. It fetches the Account from the database
3. It reads the `role` field ("USER" or "ADMIN")
4. Spring Security converts it to `ROLE_USER` or `ROLE_ADMIN` authority
5. JWT token contains the role in the `scope` claim

### Access Control

**Method 1: URL-based (in `SecurityConfig.java`)**
```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```
- Blocks entire `/api/admin/*` path for non-admins

**Method 2: Method-level (in controllers)**
```java
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    // All methods require ADMIN role
}
```

## 🧪 Testing the Implementation

### Step 1: Restart the Application

The database needs to be recreated to add the `role` column:

```bash
# Stop containers
docker-compose down -v

# Start again (will recreate database with new schema)
docker-compose up --build
```

### Step 2: Login as Admin

**Request:**
```bash
POST http://localhost:8080/login
Content-Type: application/json

{
  "username": "admin",
  "password": "AdminTarrific2024!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

Save this token!

### Step 3: Test Admin Endpoints

**List all users (admin only):**
```bash
GET http://localhost:8080/api/admin/users
Authorization: Bearer <your_admin_token>
```

**Try as regular user (should fail):**
```bash
# Login as rachel
POST http://localhost:8080/login
Body: { "username": "rachel", "password": "PasswordRachel12345!" }

# Try to access admin endpoint
GET http://localhost:8080/api/admin/users
Authorization: Bearer <rachel_token>

# Expected: 403 Forbidden
```

### Step 4: Verify Database

```bash
docker exec -it TarrificDB-mysql mysql -u sa -ppassword TarrificDB

# Check roles
SELECT userID, username, email, role FROM account;
```

**Expected Output:**
```
+--------+----------+------------------------+-------+
| userID | username | email                  | role  |
+--------+----------+------------------------+-------+
|      1 | admin    | admin@tarrific.com     | ADMIN |
|      2 | rachel   | rachel@tarrific.com    | USER  |
|      3 | elodie   | elodie@tarrific.com    | USER  |
|      4 | xinyu    | xinyu@tarrific.com     | USER  |
|      5 | amos     | amos@tarrific.com      | USER  |
|      6 | roopa    | roopa@tarrific.com     | USER  |
|      7 | jordan   | jordan@tarrific.com    | USER  |
+--------+----------+------------------------+-------+
```

## 🎨 Frontend Integration

### Storing Admin Status

After login, decode the JWT token to check the user's role:

```javascript
// Example: React/JavaScript
function decodeToken(token) {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload;
}

const tokenData = decodeToken(jwtToken);
const isAdmin = tokenData.scope.includes('ROLE_ADMIN');

// Store in state
setUser({
    username: tokenData.sub,
    isAdmin: isAdmin
});
```

### Conditional UI Rendering

```jsx
{user.isAdmin && (
    <Link to="/admin/dashboard">
        Admin Panel
    </Link>
)}
```

### Protected Routes

```jsx
// Example: React Router
<Route 
    path="/admin/*" 
    element={
        user.isAdmin ? <AdminPanel /> : <Navigate to="/" />
    } 
/>
```

## 🔐 Security Best Practices

### 1. Change Default Admin Password
**Before production**, edit `DataSeeder.java`:

```java
// Change this line (line 42)
seedUser(accountRepo, "admin@tarrific.com", "YourStrongPassword123!", "admin", "ADMIN", encoder);
```

### 2. Add Password Strength Requirements
Consider enforcing strong passwords:
- Minimum 12 characters
- Mix of uppercase, lowercase, numbers, symbols
- No common words

### 3. Audit Admin Actions
Log admin operations:

```java
@PostMapping("/users/{userId}")
public ResponseEntity<?> deleteUser(@PathVariable Integer userId, Principal principal) {
    log.info("ADMIN ACTION: User {} deleted user {}", principal.getName(), userId);
    accountService.deleteAccount(userId);
    return ResponseEntity.ok("User deleted");
}
```

### 4. Prevent Self-Deletion
Add check to prevent admin from deleting themselves:

```java
@DeleteMapping("/users/{userId}")
public ResponseEntity<String> deleteUser(@PathVariable Integer userId, Principal principal) {
    Account currentUser = accountService.getAccountByUsername(principal.getName());
    
    if (currentUser.getUserID().equals(userId)) {
        return ResponseEntity.badRequest().body("Cannot delete your own account");
    }
    
    accountService.deleteAccount(userId);
    return ResponseEntity.ok("User deleted successfully");
}
```

## 🚀 Next Steps

### Optional Enhancements

1. **Admin Dashboard Frontend**
   - Create React components for user management
   - Add data visualization for system stats

2. **More Granular Permissions**
   - Add additional roles (MODERATOR, ANALYST, etc.)
   - Implement permission-based access (READ, WRITE, DELETE)

3. **Activity Logging**
   - Track all admin actions
   - Create audit trail table

4. **Multi-Factor Authentication (MFA)**
   - Require 2FA for admin accounts
   - Use TOTP (Google Authenticator)

5. **Account Recovery**
   - Password reset via email
   - Admin can unlock accounts

## 📝 Summary

✅ **Completed:**
- Added `role` field to Account entity
- Seeded admin account with ADMIN role
- All existing users have USER role
- Updated authentication to use database roles
- Protected `/api/admin/*` routes
- Created AdminController with user management endpoints
- Enabled method-level security with `@PreAuthorize`

🎉 **Your application now has full role-based access control!**

Login as **admin@tarrific.com** with password **AdminTarrific2024!** to test admin features.
