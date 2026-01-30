package com.crcs.bookingservice.service;

import com.crcs.bookingservice.dto.response.ResourceResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ResourceServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceClient.class);
    private final RestTemplate restTemplate;
    private final String resourceServiceUrl;

    public ResourceServiceClient(RestTemplate restTemplate,
                                @Value("${resource.service.url:http://localhost:6003}") String resourceServiceUrl) {
        this.restTemplate = restTemplate;
        this.resourceServiceUrl = resourceServiceUrl;
    }

    public ResourceResponseDTO getResourceById(String resourceId) {
        try {
            String url = resourceServiceUrl + "/resources/" + resourceId;
            ResponseEntity<ResourceResponseDTO> response = restTemplate.getForEntity(url, ResourceResponseDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            logger.warn("Resource not found: {}", resourceId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching resource: {}", resourceId, e);
            return null;
        }
    }

    public boolean updateResourceStatus(String resourceId, String status) {
        try {
            String url = resourceServiceUrl + "/resources/" + resourceId + "/status?status=" + status;
            restTemplate.patchForObject(url, null, Void.class);
            return true;
        } catch (Exception e) {
            logger.error("Error updating resource status: {}", resourceId, e);
            return false;
        }
    }
}
