package app.account;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import app.exception.UserConflictException;
import app.exception.UserNotFoundException;
import app.security.PasswordChecker;

import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

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
     * Creates a new account.
     * 
     * @param account Account object to save.
     * @return Account object that is saved.
     */
    public Account createAccount(Account account) throws UserConflictException {
        Account existing = accountRepository.findByUsername(account.getUsername());
        if (existing != null) {
            throw new UserConflictException("Username " + existing.getUsername() + " already exists!");
        }

        existing = accountRepository.findByEmail(account.getEmail());
        if (existing != null) {
            throw new UserConflictException("Email already associated with a different account!");
        }

        if (!PasswordChecker.isValidPassword(account.getPassword())) {
            throw new IllegalArgumentException("Password does not meet the minimum requirements");
        }

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    /**
     * Retrieves the account given the username.
     * 
     * @param username Target username.
     * @return Corresponding Account instance.
     */
    public Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    /**
     * Retrieves the account given the user ID.
     * 
     * @param username Target user ID.
     * @return Corresponding Account instance.
     */
    public Account getAccountByUserID(int userID) {
        return accountRepository.findByUserID(userID);
    }

    /**
     * Deletes an Account by User ID.
     * 
     * @param userId Target User ID.
     */
    public void deleteAccount(Integer userId) throws UserNotFoundException {
        Account existing = accountRepository.findByUserID(userId);
        if (existing == null) {
            throw new UserNotFoundException("Account not found.");
        }

        accountRepository.deleteById(userId);
    }

    /**
     * Updates the password of an Account.
     * 
     * @param userId           Target user ID.
     * @param previousPassword Original password.
     * @param newPassword      New password to update to.
     */
    public void changePassword(Integer userId, String previousPassword, String newPassword)
            throws UserNotFoundException {
        Account account = accountRepository.findByUserID(userId);
        if (account == null) {
            throw new UserNotFoundException("Account not found.");
        }

        String password = account.getPassword();
        if (!passwordEncoder.matches(previousPassword, password)) {
            throw new IllegalArgumentException("Previous password is incorrect.");
        } else if (passwordEncoder.matches(newPassword, password)) {
            throw new IllegalArgumentException("New password must be different from the previous password.");
        } else if (!PasswordChecker.isValidPassword(newPassword)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters long, containing a mix of uppercase letters, lowercase letters, and special symbols.");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    /**
     * Updates an account's information
     * @param userId Target user ID
     * @param newAccount Account with updated information
     * @throws UserNotFoundException if account not found
     * @throws UserConflictException if username/email conflicts
     */
    public void updateDetails(Integer userId, Account newAccount) throws UserNotFoundException, UserConflictException {
        // retrieve account
        Account account = accountRepository.findByUserID(userId);
        if (account == null) {
            throw new UserNotFoundException("User with user ID " + userId + " does not exist");
        }

        // check if username is being updated
        String newUsername = newAccount.getUsername();
        boolean usernameUpdated = !account.getUsername().equals(newUsername);
        if (usernameUpdated) {
            // check if username already exists
            Account existing = accountRepository.findByUsername(newUsername);
            if (existing != null) {
                throw new UserConflictException("Username " + newUsername + " already taken, please try another one!");
            }

            // update username
            account.setUsername(newUsername);
        }

        // check if email is being updated
        String newEmail = newAccount.getEmail();
        boolean emailUpdated = !account.getEmail().equals(newEmail);
        if (emailUpdated) {
            // check if email is already associated with another account
            Account existing = accountRepository.findByEmail(newEmail);
            if (existing != null) {
                throw new UserConflictException(
                        "Email " + newEmail + " already associated with another account, please use another one!");
            }

            // update email
            account.setEmail(newEmail);
        }

        if (!usernameUpdated && !emailUpdated) {
            throw new IllegalArgumentException("Nothing to update.");
        }

        // save changes
        accountRepository.save(account);
    }

    /**
     * Gets all accounts in the system.
     * @return List of all accounts
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Gets account by ID.
     * @param id The account ID
     * @return The account or null if not found
     */
    public Account getAccountById(Integer id) {
        return accountRepository.findByUserID(id);
    }

    /**
     * Updates an account.
     * @param account The account to update
     * @return The updated account
     */
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }
}