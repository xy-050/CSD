package app.query;

import app.account.Account;
import jakarta.persistence.*;
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
@EqualsAndHashCode(exclude = {"account"})
@ToString(exclude = {"account"})
public class Query {

    // Class implementation goes here
    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment to allocate next avail ID
    private Long queryID; // primary key

    @ManyToOne
    @JoinColumn(name = "userID")
    private Account account; // foreign key from Account entity

    private String htsCode; // product code

    // Explicit getters/setters added to ensure availability during CI/Docker builds
    // (works around cases where Lombok annotation processing is not run)
    public Long getQueryID() {
        return this.queryID;
    }

    public void setQueryID(Long queryID) {
        this.queryID = queryID;
    }
}
