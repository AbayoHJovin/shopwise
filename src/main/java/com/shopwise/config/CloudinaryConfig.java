package com.shopwise.config;

import com.cloudinary.Cloudinary;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    
    @Bean
    public Cloudinary cloudinary() {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.configure().load();
        
        // Configure Cloudinary with credentials from .env
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", dotenv.get("CLOUDINARY_CLOUD_NAME"));
        config.put("api_key", dotenv.get("CLOUDINARY_API_KEY"));
        config.put("api_secret", dotenv.get("CLOUDINARY_API_SECRET"));
        config.put("secure", "true");
        
        return new Cloudinary(config);
    }
}
