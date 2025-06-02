package com.shopwise.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to load environment variables from .env file
 * and set them as system properties for Spring to use
 */
@Configuration
@Slf4j
public class EnvConfig {

    @PostConstruct
    public void init() {
        try {
            log.info("Loading environment variables from .env file");
            Dotenv dotenv = Dotenv.load();
            
            // Database configuration
            setEnvIfPresent(dotenv, "DB_URL");
            setEnvIfPresent(dotenv, "DB_USERNAME");
            setEnvIfPresent(dotenv, "DB_PASSWORD");
            
            // Mail configuration
            setEnvIfPresent(dotenv, "MAIL_USERNAME");
            setEnvIfPresent(dotenv, "MAIL_PASSWORD");
            
            // Cloudinary configuration
            setEnvIfPresent(dotenv, "CLOUDINARY_CLOUD_NAME");
            setEnvIfPresent(dotenv, "CLOUDINARY_API_KEY");
            setEnvIfPresent(dotenv, "CLOUDINARY_API_SECRET");

            //GEMINI configuration
            setEnvIfPresent(dotenv, "GEMINI_API_KEY");
            setEnvIfPresent(dotenv, "GEMINI_MODEL_NAME");
            setEnvIfPresent(dotenv, "GEMINI_PROJECT_ID");
            log.info("Environment variables loaded successfully");
        } catch (Exception e) {
            log.error("Error loading environment variables: {}", e.getMessage(), e);
        }
    }
    
    private void setEnvIfPresent(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
            log.info("Loaded environment variable: {}", key);
        } else {
            log.warn("Environment variable not found: {}", key);
        }
    }
}
