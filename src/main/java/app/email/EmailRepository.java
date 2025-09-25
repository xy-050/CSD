package app.email;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Integer> {
    Email findByEmail(String email);
    Email findbyTempID(Integer TempID);
    Email findByToken(String token);
    void deleteById(Integer TempID);
}
