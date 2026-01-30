package com.crcs.userservice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserProfileRequestDTO {
    private String name;
    
    @Email(message = "Email should be a valid email address")
    private String email;
    
    private String bio;
    
    private String profilePicture;
    
    private String phoneNumber;
}
