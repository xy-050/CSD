package app.account;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import app.controller.AdminController.RoleUpdateRequest;
import app.exception.InvalidPasswordException;
import app.exception.UserConflictException;
import app.exception.UserNotFoundException;
import app.security.PasswordChecker;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor-based injection.
     * 
     * @param accountRepository Repository dependency.
     */
    public AccountService(AccountRepository accountRepository, BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves the account given the user ID.
     * 
     * @param username Target user ID.
     * @return Corresponding Account instance.
     */
    public Account getAccountByUserID(int userID) {
        Optional<Account> account = accountRepository.findByUserID(userID);

        if (!account.isPresent()) {
            throw new UserNotFoundException("Account with user ID " + userID + " not found!");
        }

        return account.get();
    }

    /**
     * Retrieves the account given the username.
     * 
     * @param username Target username.
     * @return Corresponding Account instance.
     */
    public Account getAccountByUsername(String username) {
        Optional<Account> account = accountRepository.findByUsername(username);

        if (!account.isPresent()) {
            throw new UserNotFoundException("Account with username " + username + " not found!");
        }

        return account.get();
    }

    /**
     * Retrieves the account given the email.
     * 
     * @param email Target email
     * @return Corresponding Account instance
     */
    public Account getAccountByEmail(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);

        if (!account.isPresent()) {
            throw new UserNotFoundException("Account with email " + email + " not found!");
        }

        return account.get();
    }

    /**
     * Gets all accounts in the system.
     * 
     * @return List of all accounts
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Creates a new account.
     * 
     * @param account Account object to save.
     * @return Account object that is saved.
     */
    public Account createAccount(Account account) throws UserConflictException {
        Account existing = getAccountByUsername(account.getUsername());
        if (existing != null) {
            throw new UserConflictException("Username " + existing.getUsername() + " already exists!");
        }

        existing = getAccountByEmail(account.getEmail());
        if (existing != null) {
            throw new UserConflictException("Email already associated with a different account!");
        }

        if (!PasswordChecker.isValidPassword(account.getPassword())) {
            throw new InvalidPasswordException("Password does not meet the minimum requirements.");
        }

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    /**
     * Deletes an Account by User ID.
     * 
     * @param userId Target User ID.
     */
    public void deleteAccount(Integer userId) {
        Account existing = getAccountByUserID(userId);
        accountRepository.delete(existing);
    }

    /**
     * Updates an account's information
     * 
     * @param userId     Target user ID
     * @param newAccount Account with updated information
     * @throws UserNotFoundException if account not found
     * @throws UserConflictException if username/email conflicts
     */
    public void updateDetails(Integer userId, Account newAccount) {
        // retrieve account
        Account account = getAccountByUserID(userId);

        // check if username is being updated
        String newUsername = newAccount.getUsername();
        boolean usernameUpdated = !account.getUsername().equals(newUsername);
        if (usernameUpdated) {
            try {
                getAccountByUsername(newUsername);
                throw new UserConflictException("Username " + newUsername + " already taken, please try another one!");
            } catch (UserNotFoundException e) {
                account.setUsername(newUsername);
            } 
        }

        // check if email is being updated
        String newEmail = newAccount.getEmail();
        boolean emailUpdated = !account.getEmail().equals(newEmail);
        if (emailUpdated) {
            try {
                getAccountByEmail(newEmail);
                throw new UserConflictException("Email " + newEmail + " already associated with another account, please try another one!");
            } catch (UserNotFoundException e) {
                account.setEmail(newEmail);
            } 
        }

        if (!usernameUpdated && !emailUpdated) {
            throw new IllegalArgumentException("Nothing to update.");
        }

        // save changes
        accountRepository.save(account);
    }

    /**
     * Updates the password of an Account.
     * 
     * @param userId           Target user ID.
     * @param previousPassword Original password.
     * @param newPassword      New password to update to.
     */
    public void updatePassword(Integer userId, String previousPassword, String newPassword) {
        Account account = getAccountByUserID(userId);
        String password = account.getPassword();

        if (!passwordEncoder.matches(previousPassword, password)) {
            throw new InvalidPasswordException("Previous password is incorrect.");
        } else if (passwordEncoder.matches(newPassword, password)) {
            throw new InvalidPasswordException("New password must be different from the previous password.");
        } else if (!PasswordChecker.isValidPassword(newPassword)) {
            throw new InvalidPasswordException();
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    /**
     * Update a user's role (promote or demote to admin)
     * 
     * @param userId  Target user ID
     * @param newRole New role (USER or ADMIN)
     * @return Updated account
     */
    public Account updateRole(Integer userId, RoleUpdateRequest newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null!");
        }

        String role = newRole.getRole();
        Set<String> validRoles = Set.of("USER", "ADMIN");
        if (role == null || !validRoles.contains(role)) {
            throw new IllegalArgumentException("Role " + role + " does not exist!");
        }

        Account account = getAccountByUserID(userId);
        account.setRole(role);
        accountRepository.save(account);
        return account;
    }

    /**
     * Reset an account's password by email. This method will encode the password
     * and save the account. Throws UserNotFoundException when no account exists.
     *
     * @param email the account's email
     * @param newPassword the new plain-text password
     * @throws UserNotFoundException when account not found
     */
    public void resetPassword(String email, String newPassword) throws UserNotFoundException {
        Account account = getAccountByEmail(email);

        if (!PasswordChecker.isValidPassword(newPassword)) {
            throw new InvalidPasswordException("Password does not meet requirements");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}