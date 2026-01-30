package com.crcs.bookingservice;

import com.crcs.common.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"com.crcs.bookingservice", "com.crcs.kafka"})
public class BookingServiceApplication {

    public static void main(String[] args) {
        // Load .env file if it exists
        DotenvLoader.loadDotenv();
        
        SpringApplication.run(BookingServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }
}
