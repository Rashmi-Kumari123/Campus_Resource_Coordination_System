package com.crcs.bookingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI crcsBookingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CRCS Booking Service API")
                        .description("Booking management service for Campus Resource Coordination System. " +
                                "Handles resource bookings, availability checks, booking status updates, and cancellations. " +
                                "Integrates with resource service for availability validation and sends notifications via Kafka.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("CRCS Team")
                                .email("support@crcs.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:6004").description("Local Development Server"),
                        new Server().url("http://localhost:6000").description("API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from /auth/login or /auth/signup")));
    }
}
