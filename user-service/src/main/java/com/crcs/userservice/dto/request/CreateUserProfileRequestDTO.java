package com.crcs.userservice.dto.request;

import com.crcs.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import lombok.Data;

@Data
public class CreateUserProfileRequestDTO {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email address")
    private String email;

    private String name;

    private String role; // Will be validated and converted to UserRole

    /**
     * Get validated UserRole from role string
     */
    public UserRole getUserRole() {
        if (StringUtils.isBlank(role)) {
            return UserRole.USER; // Default role
        }
        try {
            return UserRole.fromString(role);
        } catch (IllegalArgumentException e) {
            return UserRole.USER; // Default to USER if invalid
        }
    }
}
