package com.shopwise;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShopWiseApplication {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            if (dotenv != null) {
                dotenv.entries().forEach(entry -> {
                    System.setProperty(entry.getKey(), entry.getValue());
                });
            }
        } catch (Exception e) {
            System.out.println("No .env file found, relying on system environment variables: " + e.getMessage());
        }

        SpringApplication.run(ShopWiseApplication.class, args);
    }
}