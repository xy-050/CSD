package app.query;

import app.account.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// Class implementation goes here
// user id(fk, int), queryID (pk, long)
// hts code(string) - product code --> 7-10 digits
// originCountry(string)
// modeOfTranspot(string)
// quantity(int)

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Query {

    // Class implementation goes here
    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment to allocate next avail ID
    private Long queryID; // primary key

    @ManyToOne
    @JoinColumn(name = "userID")
    private Account userID; // foreign key from Account entity

    private String htsCode; // product code
    private String originCountry;
    private String modeOfTransport;
    private int quantity;

   

}
