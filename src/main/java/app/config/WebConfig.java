package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Use /api/** to cover all API endpoints
                .allowedOrigins("http://localhost:5173") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Add OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true);

        // registry.addMapping("/home/**")

    }
}