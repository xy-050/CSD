package app.fta;

import java.time.LocalDate;

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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ftaId;

    private String country;

    private String htsCode;

    private double price;

    private LocalDate date;

    public FTA(String country, String htsCode, double price, LocalDate date) {
        this.country = country;
        this.htsCode = htsCode;
        this.price = price;
        this.date = date;
    }

}
