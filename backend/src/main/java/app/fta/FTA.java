package app.fta;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Country cannot be null")
    @NotBlank(message = "Country cannot be blank")
    private String country;

    @NotNull(message = "HTS Code cannot be null")
    @NotBlank(message = "HTS Code cannot be blank")
    private String htsCode;

    private String price;

    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    public FTA(String country, String htsCode, String price, LocalDate date) {
        this.country = country;
        this.htsCode = htsCode;
        this.price = price;
        this.date = date;
    }

}
