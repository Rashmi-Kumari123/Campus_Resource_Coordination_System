package com.crcs.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal user profile for notification recipient (matches user-service response fields we need).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private String userId;
    private String email;
    private String name;
}
