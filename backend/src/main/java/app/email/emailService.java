package app.email;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class emailService {

    private final JavaMailSender mailSender;
    private String fromAddress = "no-reply@tariffics.org";
    

    public emailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Email sent via SES SMTP!");
    }

    public void sendPasswordResetEmail(String userEmail) {
        String to = userEmail;
        String subject = "Password Reset Request";
        String body = "Click the link to reset your password: [reset link here]";
        emailService.sendEmail(to, subject, body);
    }
}


