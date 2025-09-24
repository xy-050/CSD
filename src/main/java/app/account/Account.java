package app.account;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.query.Query;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;
    private String username;
    private String password;
    private String email;

    @OneToMany(mappedBy = "userID", cascade = CascadeType.ALL)
    private List<Query> queries = new ArrayList<>();

    // User favourited hts codes
    @ManyToMany
    @JoinTable(
        name = "account_favourite_hts",
        joinColumns = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "hts_code")
    )
    private Set<FavouriteHtsCodes> favouriteHtsCodes = new HashSet<>();


}
