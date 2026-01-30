package com.crcs.notification;

import com.crcs.common.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.crcs.notification", "com.crcs.kafka"})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        // Load .env file if it exists
        DotenvLoader.loadDotenv();
        
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
