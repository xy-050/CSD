package app.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import app.account.AccountService;
import app.account.Account;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountService accountService;

    public UserDetailsServiceImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.getAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("User not found");
        }
        
        // Use the role from the database (USER or ADMIN)
        String role = account.getRole() != null ? account.getRole() : "USER";
        
        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(role)
                .build();
    }
}