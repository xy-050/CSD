package app.fta;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FTA {
    @Id 
    private String country;

    // { htsCode -> { fetchDate, price } }
    private Map<String, Map<LocalDate, Double>> prices;

}
