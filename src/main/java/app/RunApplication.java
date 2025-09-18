package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"app.account", "app.query"})
public class RunApplication {
    public static void main(String[] args) {
        // configure and start the Spring Boot application
        SpringApplication.run(RunApplication.class, args);
    }
}