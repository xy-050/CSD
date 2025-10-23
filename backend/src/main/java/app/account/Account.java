package app.account;

import java.util.ArrayList;
import java.util.List;

import app.query.Query;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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


}
