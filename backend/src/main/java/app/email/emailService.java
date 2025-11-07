package app.email;

import security.JwtUtils; 
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class emailService {
    private String htscode;
    private final JavaMailSender mailSender;
    private String fromAddress = "no-reply@tariffics.org";
    private String PasswordResetSubject = "Password Reset Request";
    private String PasswordResetBody = "Click the link below to reset your password:\n\n" +
            "http://your-frontend-url/reset-password?email=%s";
    private String NotificationSubject = "Notification from Tariffics";
    private String NotificationBody = "Hello! The item" + htscode + "This is a notification email from Tariffics.\n\n%s";

    /**
    * Constructor-based injection.
    * 
    * @param mailSender JavaMailSender dependency.
    */
    public emailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    /*helper methods*/
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Email sent via SES SMTP!");
    }

    /*send Password reset emails*/
    public void sendPasswordResetEmail(String userEmail) {
        String to = userEmail;
        emailService.sendEmail(to, PasswordResetSubject, PasswordResetBody.formatted(userEmail));
    }

    /*send Password reset emails*/
    public void sendNotificationEmail(String to, String subject, String body) {
        emailService.sendEmail(to, subject, body);
    }

}


