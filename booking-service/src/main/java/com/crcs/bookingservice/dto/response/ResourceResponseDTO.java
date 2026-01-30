package com.crcs.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponseDTO {
    private String id;
    private String name;
    private ResourceType type;
    private String description;
    private ResourceStatus status;
    private String location;
    private Integer capacity;
    private String ownerId;
    private String responsiblePerson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ResourceType {
        ROOM,
        LAB,
        HALL,
        EQUIPMENT,
        CAFETERIA,
        LIBRARY,
        PARKING,
        SPORTS
    }

    public enum ResourceStatus {
        AVAILABLE,
        BOOKED,
        MAINTENANCE,
        UNAVAILABLE
    }
}
