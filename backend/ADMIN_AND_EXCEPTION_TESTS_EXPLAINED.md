# Integration Testing - What Each Test Does (Detailed Breakdown)

## 🎯 Complete Test Inventory

This document explains EVERY test method in detail - what it does, why it matters, and what scenario it represents.

---

## 1️⃣ AdminControllerIntegrationTest (14 tests)

### Purpose
Tests all admin-specific operations and ensures proper role-based access control.

### Test 1: `testAdminGetAllUsers_Success()`
**What it does:**
- Admin logs in and accesses GET /api/admin/users
- Verifies response contains all user accounts
- Checks that user details are properly serialized

**Why it matters:**
- Core admin functionality for viewing all registered users
- Foundation for user management dashboard
- Ensures admin can see the complete user base

**Example:**
```
Admin → GET /api/admin/users
Response: [
  {id: 1, email: "admin@test.com", role: "ADMIN"},
  {id: 2, email: "user1@test.com", role: "USER"},
  {id: 3, email: "user2@test.com", role: "USER"}
]
```

---

### Test 2: `testRegularUserCannotAccessAdminEndpoints_ReturnsForbidden()`
**What it does:**
- Regular user with USER role tries to access admin endpoints
- Expects 403 Forbidden status

**Why it matters:**
- Security: prevents privilege escalation
- Validates @PreAuthorize("hasRole('ADMIN')") annotation works
- Critical for role-based access control

**Example:**
```
Regular User → GET /api/admin/users
Response: 403 Forbidden ❌
```

---

### Test 3: `testUnauthenticatedUserCannotAccessAdminEndpoints_ReturnsUnauthorized()`
**What it does:**
- Request to admin endpoint without JWT token
- Expects 401 Unauthorized status

**Why it matters:**
- Security: admin panel requires authentication
- Prevents anonymous access to sensitive operations
- First line of defense

**Example:**
```
Anonymous User → GET /api/admin/users (no token)
Response: 401 Unauthorized ❌
```

---

### Test 4: `testAdminDeleteUser_Success()`
**What it does:**
- Admin sends DELETE /api/admin/users/{userId}
- Verifies user is deleted from database
- Checks success message is returned

**Why it matters:**
- User management: ability to remove accounts
- Account lifecycle management
- Moderation capabilities

**Example:**
```
Admin → DELETE /api/admin/users/5
Response: "User deleted successfully" ✅
Database: User #5 removed
```

---

### Test 5: `testAdminDeleteNonExistentUser_ReturnsNotFound()`
**What it does:**
- Admin tries to delete user with invalid ID (99999)
- Expects 404 Not Found status

**Why it matters:**
- Error handling: proper response for invalid IDs
- Prevents silent failures
- User experience: clear error feedback

**Example:**
```
Admin → DELETE /api/admin/users/99999
Response: 404 Not Found ❌
```

---

### Test 6: `testRegularUserCannotDeleteUser_ReturnsForbidden()`
**What it does:**
- Regular user tries to delete another user
- Expects 403 Forbidden
- Verifies user was NOT deleted

**Why it matters:**
- Security: prevents users from deleting accounts
- Only admins can perform destructive operations
- Protects user data

**Example:**
```
Regular User → DELETE /api/admin/users/5
Response: 403 Forbidden ❌
Database: User #5 still exists ✅
```

---

### Test 7: `testAdminGetSystemStats_Success()`
**What it does:**
- Admin requests GET /api/admin/stats
- Verifies response contains totalUsers and totalAdmins
- Checks counts are accurate

**Why it matters:**
- Dashboard functionality
- System monitoring and reporting
- Admin insights into user base

**Example:**
```
Admin → GET /api/admin/stats
Response: {
  totalUsers: 50,
  totalAdmins: 3
} ✅
```

---

### Test 8: `testRegularUserCannotViewStats_ReturnsForbidden()`
**What it does:**
- Regular user tries to view system statistics
- Expects 403 Forbidden

**Why it matters:**
- Privacy: stats may reveal sensitive information
- Admin-only feature enforcement
- Protects system metrics

**Example:**
```
Regular User → GET /api/admin/stats
Response: 403 Forbidden ❌
```

---

### Test 9: `testAdminPromoteUserToAdmin_Success()`
**What it does:**
- Admin sends PUT /api/admin/users/{userId}/role with role: "ADMIN"
- Verifies user's role is updated to ADMIN
- Checks database reflects the change

**Why it matters:**
- Permission management: grant admin privileges
- Flexible user role assignment
- Supports administrative workflows

**Example:**
```
Before: {id: 5, role: "USER"}
Admin → PUT /api/admin/users/5/role {role: "ADMIN"}
After: {id: 5, role: "ADMIN"} ✅
```

---

### Test 10: `testAdminDemoteAdminToUser_Success()`
**What it does:**
- User is first promoted to ADMIN
- Admin then changes role back to USER
- Verifies role change works bidirectionally

**Why it matters:**
- Flexible permission management
- Ability to revoke admin privileges
- Complete role lifecycle management

**Example:**
```
User: {id: 5, role: "ADMIN"}
Admin → PUT /api/admin/users/5/role {role: "USER"}
Result: {id: 5, role: "USER"} ✅
```

---

### Test 11: `testRegularUserCannotChangeRoles_ReturnsForbidden()`
**What it does:**
- Regular user tries to promote themselves to ADMIN
- Expects 403 Forbidden
- Verifies role was NOT changed

**Why it matters:**
- Security: prevents self-promotion
- Users can't grant themselves admin access
- Critical security control

**Example:**
```
User (id: 5, role: "USER") → PUT /api/admin/users/5/role {role: "ADMIN"}
Response: 403 Forbidden ❌
Database: Still role: "USER" ✅
```

---

### Test 12: `testAdminUpdateRoleNonExistentUser_ReturnsError()`
**What it does:**
- Admin tries to change role for non-existent user (99999)
- Expects 4xx error status

**Why it matters:**
- Data validation
- Error handling for invalid user IDs
- Clear error messages

**Example:**
```
Admin → PUT /api/admin/users/99999/role {role: "ADMIN"}
Response: 4xx Error ❌
```

---

### Test 13: `testSystemStatsUpdateAfterRoleChange_Success()`
**What it does:**
- Gets initial stats (e.g., admins: 3)
- Promotes user to ADMIN
- Gets updated stats
- Verifies admin count increased by 1 (admins: 4)

**Why it matters:**
- Data consistency: stats reflect real-time changes
- Dashboard accuracy
- Validates database triggers/updates work

**Example:**
```
Initial: {totalAdmins: 3}
Admin → Promote user to ADMIN
Updated: {totalAdmins: 4} ✅ (increased by 1)
```

---

### Test 14: `testAdminListShowsUpdatedRoles_Success()`
**What it does:**
- Promotes user to ADMIN
- Gets list of all users
- Verifies promoted user shows ADMIN role in list

**Why it matters:**
- Real-time UI updates
- No caching issues
- Data consistency across operations

**Example:**
```
Admin → Promote user #5 to ADMIN
Admin → GET /api/admin/users
Response includes: {id: 5, role: "ADMIN"} ✅
```

---

## 2️⃣ ExceptionHandlingIntegrationTest (14 tests)

### Purpose
Tests that the GlobalExceptionHandler properly catches and handles all exceptions with correct HTTP status codes and helpful error messages.

### Test 1: `testUserNotFoundException_Returns404()`
**What it does:**
- Tries to access non-existent user (ID: 99999)
- Expects 404 Not Found status

**Why it matters:**
- Proper REST API behavior
- Standard HTTP status codes
- User-friendly error responses

**Example:**
```
GET /api/accounts/99999
Response: 404 Not Found
Message: "User not found with ID: 99999"
```

---

### Test 2: `testProductNotFoundException_Returns404()`
**What it does:**
- Searches for non-existent product ("9999.99.99.99")
- Expects 404 Not Found

**Why it matters:**
- Product search error handling
- Distinguishes "not found" from "no results"
- Clear error feedback

**Example:**
```
GET /api/products/9999.99.99.99
Response: 404 Not Found
Message: "Product not found"
```

---

### Test 3: `testInvalidPasswordException_Returns400()`
**What it does:**
- Tries to signup with weak password ("weak")
- Expects 400 Bad Request
- Verifies error message mentions password requirements

**Why it matters:**
- Security: enforces strong passwords
- User experience: explains requirements
- Prevents weak credentials

**Example:**
```
POST /api/accounts/signup
Body: {password: "weak"}
Response: 400 Bad Request
Message: "Password must be at least 8 characters..."
```

---

### Test 4: `testUserConflictException_Returns409()`
**What it does:**
- Tries to register with already-used email
- Expects 409 Conflict status

**Why it matters:**
- Data integrity: unique emails enforced
- Proper HTTP status for duplicate resources
- Clear error when account exists

**Example:**
```
POST /api/accounts/signup
Body: {email: "existing@test.com"}
Response: 409 Conflict
Message: "Email already in use"
```

---

### Test 5: `testIllegalArgumentException_Returns400()`
**What it does:**
- Sends request with invalid email format ("not-an-email")
- Expects 400 Bad Request

**Why it matters:**
- Input validation enforcement
- Data quality
- User-friendly validation errors

**Example:**
```
PUT /api/accounts/5
Body: {email: "not-an-email"}
Response: 400 Bad Request
Message: "Invalid email format"
```

---

### Test 6: `testFavouritesNotFoundException_Returns404()`
**What it does:**
- Tries to delete non-existent favourite (ID: 99999)
- Expects 404 Not Found

**Why it matters:**
- Favourites error handling
- Proper status codes
- Clear error messages

**Example:**
```
DELETE /api/favourites/99999
Response: 404 Not Found
Message: "Favourite not found"
```

---

### Test 7: `testMultipleValidationErrors_Returns400()`
**What it does:**
- Sends request with multiple invalid fields (empty username AND invalid email)
- Expects 400 Bad Request

**Why it matters:**
- Comprehensive validation
- All errors returned at once (not one-by-one)
- Better user experience

**Example:**
```
PUT /api/accounts/5
Body: {username: "", email: "invalid"}
Response: 400 Bad Request
Message: "Username required; Invalid email"
```

---

### Test 8: `testPasswordChangeWrongOldPassword_Returns400()`
**What it does:**
- Tries to change password with wrong old password
- Expects 400 Bad Request

**Why it matters:**
- Security: old password verification
- Prevents unauthorized password changes
- Even with JWT, old password required

**Example:**
```
POST /api/accounts/5/password
Body: {oldPassword: "wrong", newPassword: "new"}
Response: 400 Bad Request
Message: "Current password is incorrect"
```

---

### Test 9: `testErrorResponseContainsMessage_Success()`
**What it does:**
- Triggers an error (duplicate email)
- Verifies response includes helpful error message
- Checks message is not empty

**Why it matters:**
- User experience: helpful error messages
- Users understand what went wrong
- Professional API design

**Example:**
```
POST /api/accounts/signup (duplicate email)
Response: {message: "Email already registered"}
✅ Message is helpful and specific
```

---

### Test 10: `testExceptionHandlingNoSensitiveInfo_Success()`
**What it does:**
- Triggers error and captures response
- Verifies no stack traces in response
- Checks no database details leaked
- Ensures no class names exposed

**Why it matters:**
- Security: prevents information disclosure
- Doesn't reveal system architecture
- Professional error handling

**Example:**
```
GET /api/accounts/99999
Response: "User not found"
❌ NO stack trace
❌ NO "at app.account.AccountService.findById"
❌ NO "SQLException: Table..."
✅ Clean, safe error message
```

---

### Test 11: `testConcurrentExceptionHandling_Success()`
**What it does:**
- Makes multiple failing requests simultaneously
- Verifies all return proper errors independently
- Tests thread safety

**Why it matters:**
- Real-world: multiple users causing errors at once
- Thread safety of GlobalExceptionHandler
- Scalability validation

**Example:**
```
User A → GET /api/accounts/99991 → 404 ✅
User B → GET /api/accounts/99992 → 404 ✅
User C → GET /api/accounts/99993 → 404 ✅
All handled independently
```

---

### Test 12: `testExceptionHandlingAfterSuccess_Success()`
**What it does:**
- Makes successful request
- Makes failing request
- Makes another successful request
- Verifies no state pollution

**Why it matters:**
- Stateless error handling
- No carryover effects between requests
- Reliability

**Example:**
```
GET /api/accounts/current → 200 ✅
GET /api/accounts/99999 → 404 ✅
GET /api/accounts/current → 200 ✅
Each request independent
```

---

### Test 13: `testEmptyRequestBody_Returns400()`
**What it does:**
- Sends empty JSON {} to signup endpoint
- Expects 400 Bad Request

**Why it matters:**
- Input validation
- Prevents null pointer exceptions
- Required fields enforced

**Example:**
```
POST /api/accounts/signup
Body: {}
Response: 400 Bad Request
Message: "Email, password, username required"
```

---

### Test 14: `testInvalidJsonFormat_Returns400()`
**What it does:**
- Sends malformed JSON ("{invalid json")
- Expects 400 Bad Request

**Why it matters:**
- Robust error handling
- Graceful JSON parsing errors
- Protects against malformed input

**Example:**
```
POST /api/accounts/signup
Body: {invalid json
Response: 400 Bad Request
Message: "Invalid JSON format"
```

---

## 🎯 Summary

### AdminControllerIntegrationTest validates:
- ✅ Admin can view all users
- ✅ Admin can delete users
- ✅ Admin can view statistics
- ✅ Admin can change user roles
- ✅ Regular users completely blocked
- ✅ Unauthenticated users blocked
- ✅ Real-time data updates
- ✅ Error handling for invalid IDs

### ExceptionHandlingIntegrationTest validates:
- ✅ Correct HTTP status codes (404, 400, 409)
- ✅ Helpful error messages
- ✅ No sensitive information leaked
- ✅ Thread-safe error handling
- ✅ Stateless error handling
- ✅ Input validation enforced
- ✅ JSON parsing errors caught
- ✅ Professional API responses

**Total: 28 new tests covering admin operations and complete error handling! 🎉**
