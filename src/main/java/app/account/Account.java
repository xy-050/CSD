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

    // // Getters and Setters
    // public Integer getId() {
    //     return userid;
    // }
    // public void setId(Integer userid) {
    //     this.userid = userid;
    // }
    // public String getUsername() {
    //     return username;
    // }

    // public void setUsername(String username) {
    //     this.username = username;
    // }
    // public String getPassword() {
    //     return password;
    // }

    // public void setPassword(String password) {
    //     this.password = password;
    // }


}
