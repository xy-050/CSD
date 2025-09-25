package app.email;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Integer> {
    Email findByEmail(String email);
    Email findbyTempID(Integer TempID);
    Email findByToken(String token);
    Email save(Email email);
    void deleteById(Integer TempID);
}
