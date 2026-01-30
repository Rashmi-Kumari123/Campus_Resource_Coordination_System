package com.crcs.bookingservice.service;

import com.crcs.bookingservice.dto.response.UserProfileResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${user.service.url:http://localhost:6002}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public UserProfileResponseDTO getUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            String url = userServiceUrl + "/users/" + userId;
            ResponseEntity<UserProfileResponseDTO> response = restTemplate.getForEntity(url, UserProfileResponseDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            logger.warn("User not found: {}", userId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching user: {}", userId, e);
            return null;
        }
    }
}
