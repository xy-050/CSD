package repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{
    Account findByUsername(String username);
    Account findByUserID(Integer userid);
    Account save(Account account);
    List<Account> findAll();
    void deleteById(Integer userId);
    
    
} 
