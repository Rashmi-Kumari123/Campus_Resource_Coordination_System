package com.crcs.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private String userId;
    private String email;
    private String name;
    private String role;
    private String profilePicture;
    private String bio;
    private String phoneNumber;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
