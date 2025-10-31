package app.product;

import java.io.Serializable;
import java.time.LocalDate;

public class ProductId implements Serializable {
    private String htsCode;
    private LocalDate fetchDate;

    public ProductId(String htsCode, LocalDate fetchDate) {
        this.htsCode = htsCode;
        this.fetchDate = fetchDate;
    }
}
