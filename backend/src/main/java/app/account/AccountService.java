package app.account;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private Integer userID;

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
     * Retrieves all accounts stored in the DB.
     * 
     * @return List containing instances of accounts.
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
    public Account createAccount(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    /**
     * Retrieves the account given the user ID.
     * 
     * @param id Target user ID.
     * @return Corresponding Account instance.
     */
    public Account getAccountById(Long id) {
        return accountRepository.findByUserID(userID);
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
     * Retrieves the account given the email.
     * 
     * @param username Target username.
     * @return Corresponding Account instance.
     */
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    /**
     * Updates an account.
     * 
     * @param account Target Account instance.
     * @return Updated Account.
     */
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Deletes an Account by User ID.
     * 
     * @param userId Target User ID.
     */
    public void deleteAccount(Integer userId) {
        accountRepository.deleteById(userId);
    }

    /**
     * Deleted an Account by username.
     * 
     * @param username Target username.
     */
    public void deleteAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account != null) {
            accountRepository.deleteById(account.getUserID());
        }
    }

    /**
     * Updates the password of an Account.
     * 
     * @param userId           Target user ID.
     * @param previousPassword Original password.
     * @param newPassword      New password to update to.
     */
    public void changePassword(Integer userId, String previousPassword, String newPassword) {
        Account account = accountRepository.findByUserID(userId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        String password = account.getPassword();
        if (!password.equals(previousPassword)) {
            throw new IllegalArgumentException("Previous password is incorrect.");
        } else if (password.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from the previous password.");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    /*
     * Saves the favourite hts code of user into their account
     */
    public void addFavouriteHtsCode(Integer userId, String htsCode) {
        Account account = accountRepository.findById(userId).orElseThrow();
        account.getFavouriteHtsCodes().add(new FavouriteHtsCodes());
        accountRepository.save(account);
    }

    /*
     * Removes the favourite hts code of user from their account
     */
    public void removeFavouriteHtsCode(Integer userId, String htsCode) {
        Account account = accountRepository.findById(userId).orElseThrow();
        account.getFavouriteHtsCodes().remove(new FavouriteHtsCodes());
        accountRepository.save(account);
    }

    /*
     * Retrieves the favourite hts codes of a user from their account
     */
    public Set<String> getFavouriteHtsCodes(Integer userId) {
        Account account = accountRepository.findById(userId).orElseThrow();
        return account.getFavouriteHtsCodes()
                      .stream()
                      .map(FavouriteHtsCodes::getHtsCode)
                      .collect(Collectors.toSet());
    }
}
