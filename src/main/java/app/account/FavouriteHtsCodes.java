package app.account;

import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(mappedBy = "favouriteHtsCodes")
    private Set<Account> accounts = new HashSet<>();

}