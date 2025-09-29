package app.account;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);

    Account findByUserID(Integer userid);

    Account save(Account account);

    List<Account> findAll();

    void deleteById(Integer userId);
}