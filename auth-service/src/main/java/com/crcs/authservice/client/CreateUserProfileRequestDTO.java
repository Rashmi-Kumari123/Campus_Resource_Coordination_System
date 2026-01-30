package com.crcs.authservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a user profile in user-service (user_profile table).
 * Matches the contract expected by user-service POST /users.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserProfileRequestDTO {
    private String userId;
    private String email;
    private String name;
    private String role;
}
