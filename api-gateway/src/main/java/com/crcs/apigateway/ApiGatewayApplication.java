package com.crcs.apigateway;

import com.crcs.common.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {

  public static void main(String[] args) {
    // Load .env file if it exists
    DotenvLoader.loadDotenv();
    
    SpringApplication.run(ApiGatewayApplication.class, args);
  }

}
