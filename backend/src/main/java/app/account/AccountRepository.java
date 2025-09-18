package app.account;

import java.util.List;
import org.springframework.stereotype.Repository;
import app.JpaRepository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);

    Account findByUserID(Integer userid);

    Account save(Account account);

    List<Account> findAll();

    void deleteById(Integer userId);
}
