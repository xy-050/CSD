package app.email; 

@RestController
public class emailController {
    private emailService emailService;

    public emailController(emailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/forget-password")
    public void PasswordReset() {
        return ResponseEntity.ok();
    }

    @GetMapping("/"){
    
    }
}