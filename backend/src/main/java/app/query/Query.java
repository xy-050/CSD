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
@EqualsAndHashCode(exclude = {"userID"})
@ToString(exclude = {"userID"})
public class Query {

    // Class implementation goes here
    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment to allocate next avail ID
    private Long queryID; // primary key

    @ManyToOne
    @JoinColumn(name = "userID")
    private Account userID; // foreign key from Account entity

    private String htsCode; // product code
}
