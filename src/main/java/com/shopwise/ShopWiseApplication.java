package com.shopwise;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShopWiseApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load(); // Load .env from root

        // Load .env entries as System properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });


        SpringApplication.run(ShopWiseApplication.class, args);
    }

}
