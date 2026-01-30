package com.crcs.authservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for user-service. Used to create user profile in user_profile table on signup.
 */
@Component
public class UserServiceClient {
    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${user.service.url:http://localhost:6002}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    /**
     * Create user profile in user-service (user_profile table) with signup data.
     * Does not fail signup if this call fails (e.g. user-service down); only logs the error.
     */
    public void createUserProfile(String userId, String email, String name, String role) {
        if (userId == null || userId.isBlank() || email == null || email.isBlank()) {
            log.warn("Cannot create user profile: userId and email are required");
            return;
        }
        try {
            String url = userServiceUrl + "/users";
            CreateUserProfileRequestDTO request = new CreateUserProfileRequestDTO(userId, email, name, role);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CreateUserProfileRequestDTO> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("User profile created for userId: {}", userId);
            } else {
                log.warn("User service returned non-success creating profile for userId: {} - {}", userId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to create user profile in user-service for userId: {}. Signup succeeded but profile was not created.", userId, e);
        }
    }
}
