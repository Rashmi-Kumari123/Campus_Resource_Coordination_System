package com.crcs.authservice.dto;

import com.crcs.common.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import lombok.Data;

@Data
public class SignupRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    
    @JsonAlias({"name", "fullName"})
    private String name;
    
    private String role; // Will be validated and converted to UserRole

    public SignupRequestDTO() {
    }

    public SignupRequestDTO(String email, String password, String name, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    /**
     * Get validated UserRole from role string
     */
    public UserRole getUserRole() {
        if (StringUtils.isBlank(role)) {
            return UserRole.USER; // Default role
        }
        return UserRole.fromString(role);
    }
}
