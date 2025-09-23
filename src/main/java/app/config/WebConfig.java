package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/app/query/**") // Adjust the path to match your API routes
                .allowedOrigins("http://localhost:5173"); // Allow frontend's origin
                // .allowedMethods("GET", "POST", "PUT", "DELETE"); // Allowing HTTP methods
    }
}
