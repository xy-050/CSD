package app.email;


import app.account.AccountRepository;

//import required classes 
import java.io.File;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final EmailRepository emailRepository;
    /*
     * Contructor-based dependency injection for EmailRepository
     * @paramater :  emailRepository the repository to be injected
    */
    @Autowired
    public EmailService(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    // Annotation
    @Autowired private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}") private String sender;

    /*
     * Sends a simple email message.
    */
    public String sendSimpleMail(EmailDetails details){

        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage
                = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            return "Error while Sending Mail";
        }
    }
    
    /*method to search for user via userID*/
    public void getUserByID(Integer userID) {
        emailRepository.findById(userID).orElse(null);
    }

    //method to search for user via email
    public Email getUserByEmail(String email) {
        return emailRepository.findByEmail(email);
    }


    //Method to delete entry by id 
    public void deleteByID(Integer TempID) {
        emailRepository.deleteById(TempID);
    }

    /**If time expires, delete the entry made in database */
    public void deleteIfExpired(Integer tempID) {
    Email email = emailRepository.findbyTempID(tempID);
    if (email != null && System.currentTimeMillis() > email.getExpirationdate()) {
        emailRepository.deleteById(email.getTempID());
    }
    }

    //update token method
    public void updateToken(String email, String newToken){
        if (email != null) {
            Email existingEmail = emailRepository.findByEmail(email);
            if (existingEmail != null) {
                existingEmail.setToken(newToken);
                existingEmail.setExpirationdate(System.currentTimeMillis() + 15 * 60 * 1000); // reset expiration date
                emailRepository.save(existingEmail);
            }
        }
    }

    //update expiration date method
    public void updateExpirationDate(String email, Long newExpirationDate){
        if (email != null) {
            Email existingEmail = emailRepository.findByEmail(email);
            if (existingEmail != null) {
                existingEmail.setExpirationdate(newExpirationDate);
                emailRepository.save(existingEmail);
            }
        }
    }

}