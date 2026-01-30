package com.crcs.authservice;

import com.crcs.common.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"com.crcs.authservice", "com.crcs.kafka"})
public class AuthServiceApplication {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  public static void main(String[] args) {
    // Load .env file if it exists
    DotenvLoader.loadDotenv();
    
    SpringApplication.run(AuthServiceApplication.class, args);
  }
}
