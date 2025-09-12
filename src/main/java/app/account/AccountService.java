package app.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    // @Autowired
    private AccountRepository accountRepository;
    private Integer userid;
    
    //get all accounts
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    //create account
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    //get account by userId
    public Account getAccountById(Long id) {
        return accountRepository.findByUserID(userid);
    }
    
    //get account by username
    public Account getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    //update account
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    //delete account by userId
    public void deleteAccount(Integer userId) {
        accountRepository.deleteById(userId);
    }
    //delete account by username
    public void deleteAccountByUsername(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account != null) {
            accountRepository.deleteById(account.getUserid());
        }
    }

    // Change password method
    public void changePassword(Integer userId, String previousPassword, String newPassword) {
        Account account = accountRepository.findByUserID(userId);
        if (account != null && account.getPassword().equals(previousPassword)) {
            account.setPassword(newPassword);
            accountRepository.save(account);
        } else {
            // Handle error: account not found or previous password incorrect
            throw new IllegalArgumentException("Invalid user or password");
        }
    }

}

