package app.fta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@DataJpaTest
public class FTARepositoryTest {

    @Autowired
    private FTARepository ftaRepository;

    // @Test
    void findByCountry_ShouldReturnMatchingFTAs() {
        FTA fta = new FTA();
        fta.setCountry("Singapore");
        ftaRepository.save(fta);

        Optional<List<FTA>> result = ftaRepository.findByCountry("Singapore");

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Singapore", result.get().get(0).getCountry());
    }
}