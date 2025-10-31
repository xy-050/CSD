package app.favourites;

import java.util.HashSet;
import java.util.Set;

import app.account.Account;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(exclude = {"accounts"})
@ToString(exclude =  {"accounts"})
public class Favourites {
    @Id
    private String htsCode;

    @ManyToMany(mappedBy = "favourites")
    private Set<Account> accounts = new HashSet<>();

}