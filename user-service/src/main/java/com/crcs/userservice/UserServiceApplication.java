package com.crcs.userservice;

import com.crcs.common.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        // Load .env file if it exists
        DotenvLoader.loadDotenv();
        
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
