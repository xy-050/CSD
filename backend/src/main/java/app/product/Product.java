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
@IdClass(ProductId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Product {

    @Id
    private String htsCode;

    @Id
    private LocalDate fetchDate;

    private double price;
    private String description;
    private String category;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFetchDate(LocalDate fetchDate) {
        this.fetchDate = fetchDate;
    }

    public void setHtsCode(String htsCode) {
        this.htsCode = htsCode;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Product p) {
            return this.htsCode.equals(p.getHtsCode()) && this.price == p.getPrice() && this.description.equals(p.getDescription());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(htsCode, price);
    }
}
