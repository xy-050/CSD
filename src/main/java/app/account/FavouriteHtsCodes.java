package app.account;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode 
@ToString

public class FavouriteHtsCodes {
    @Id
    private String htsCode;
}
