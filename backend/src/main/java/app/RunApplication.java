package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
		info = @Info(
				title = "CSD",
				version = "1.0",
				description = ""
		)
)
@SpringBootApplication
@EntityScan(basePackages = {"app.account", "app.query"})
public class RunApplication {
    public static void main(String[] args) {
        // configure and start the Spring Boot application
        SpringApplication.run(RunApplication.class, args);
    }
}