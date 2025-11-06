package app.product;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@IdClass(ProductId.class)
public class Product implements Comparable<Product> {
    @Id
    private String htsCode;

    @Id
    private LocalDate fetchDate;

    @Override
    public int compareTo(Product other) {
        int htsCodeComparison = htsCode.compareTo(other.htsCode);
        if (htsCodeComparison != 0) return htsCodeComparison;
        return fetchDate.compareTo(other.fetchDate);
    }

    @Lob
    private String description;
    
    @Column(nullable = true)
    private String general;

    @Column(nullable = true)
    private String special;

    private String category;
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Product p) {
            boolean htsCodeEqual = htsCode.equals(p.getHtsCode());
            boolean generalEqual = general == null ? p.getGeneral() == null : general.equals(p.getGeneral());
            boolean specialEqual = special == null ? p.getSpecial() == null : special.equals(p.getSpecial());
            return htsCodeEqual && generalEqual && specialEqual;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(htsCode, description, general, special, category);
    }
}
