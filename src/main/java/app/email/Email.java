package app.email;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Email {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment to allocate next avail ID
    private Integer TempID; 
    private String email;
    private String token;
    private Long expirationdate = System.currentTimeMillis() + 15 * 60 * 1000; // 15 minutes from now

}