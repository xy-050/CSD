package app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") 
                .allowedOrigins(
                    "http://localhost:5173", 
                    "http://tariffics.org:5173",
                     "http://tariffics.org:3307",
                     "https://tariffics.org") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Add OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}