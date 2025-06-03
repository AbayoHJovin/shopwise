//package com.shopwise.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@Slf4j
//public class EnvConfig {
//
//    @PostConstruct
//    public void init() {
//        try {
//            log.info("Attempting to load environment variables from .env file in directory: {}", System.getProperty("user.dir"));
//            Dotenv dotenv = Dotenv.load();
//
//            // Database configuration
//            setEnvIfPresent(dotenv, "DB_URL");
//            setEnvIfPresent(dotenv, "DB_USER");
//            setEnvIfPresent(dotenv, "DB_PASSWORD");
//
//            // Mail configuration
//            setEnvIfPresent(dotenv, "MAIL_USERNAME");
//            setEnvIfPresent(dotenv, "MAIL_PASSWORD");
//
//            // Cloudinary configuration
//            setEnvIfPresent(dotenv, "CLOUDINARY_CLOUD_NAME");
//            setEnvIfPresent(dotenv, "CLOUDINARY_API_KEY");
//            setEnvIfPresent(dotenv, "CLOUDINARY_API_SECRET");
//
//            // GEMINI configuration
//            setEnvIfPresent(dotenv, "GEMINI_API_KEY");
//            setEnvIfPresent(dotenv, "GEMINI_MODEL_NAME");
//            setEnvIfPresent(dotenv, "GEMINI_PROJECT_ID");
//            log.info("Environment variables loaded successfully");
//        } catch (Exception e) {
//            log.error("Error loading environment variables: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to load .env file", e); // Rethrow for visibility during build
//        }
//    }
//
//    private void setEnvIfPresent(Dotenv dotenv, String key) {
//        String value = dotenv.get(key);
//        if (value != null && !value.isEmpty()) {
//            System.setProperty(key, value);
//            log.info("Loaded environment variable: {}", key);
//        } else {
//            log.warn("Environment variable not found: {}", key);
//        }
//    }
//}


package com.shopwise.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EnvConfig {

    @PostConstruct
    public void init() {
        try {
            log.info("Attempting to load environment variables from .env file in directory: {}", System.getProperty("user.dir"));
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            // Database configuration
            setEnvIfPresent(dotenv, "DB_URL");
            setEnvIfPresent(dotenv, "DB_USER");
            setEnvIfPresent(dotenv, "DB_PASSWORD");

            // Mail configuration
            setEnvIfPresent(dotenv, "MAIL_USERNAME");
            setEnvIfPresent(dotenv, "MAIL_PASSWORD");

            // Cloudinary configuration
            setEnvIfPresent(dotenv, "CLOUDINARY_CLOUD_NAME");
            setEnvIfPresent(dotenv, "CLOUDINARY_API_KEY");
            setEnvIfPresent(dotenv, "CLOUDINARY_API_SECRET");

            // GEMINI configuration
            setEnvIfPresent(dotenv, "GEMINI_API_KEY");
            setEnvIfPresent(dotenv, "GEMINI_MODEL_NAME");
            setEnvIfPresent(dotenv, "GEMINI_PROJECT_ID");
            log.info("Environment variables loaded successfully");
        } catch (Exception e) {
            log.error("Error loading .env file, falling back to system environment variables", e);
            // Fallback to system environment variables
            loadSystemEnvVariables();
        }
    }

    private void setEnvIfPresent(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
            log.info("Loaded environment variable from .env: {}", key);
        } else {
            // Fallback to system environment
            String systemValue = System.getenv(key);
            if (systemValue != null && !systemValue.isEmpty()) {
                System.setProperty(key, systemValue);
                log.info("Loaded environment variable from system: {}", key);
            } else {
                log.warn("Environment variable not found: {}", key);
            }
        }
    }

    private void loadSystemEnvVariables() {
        String[] keys = {
                "DB_URL", "DB_USER", "DB_PASSWORD",
                "MAIL_USERNAME", "MAIL_PASSWORD",
                "CLOUDINARY_CLOUD_NAME", "CLOUDINARY_API_KEY", "CLOUDINARY_API_SECRET",
                "GEMINI_API_KEY", "GEMINI_MODEL_NAME", "GEMINI_PROJECT_ID"
        };
        for (String key : keys) {
            String value = System.getenv(key);
            if (value != null && !value.isEmpty()) {
                System.setProperty(key, value);
                log.info("Loaded environment variable from system: {}", key);
            } else {
                log.warn("System environment variable not found: {}", key);
            }
        }
    }
}