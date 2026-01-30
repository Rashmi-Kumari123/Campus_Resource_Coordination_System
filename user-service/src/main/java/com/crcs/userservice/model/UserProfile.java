package com.crcs.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(length = 50)
    private String role;

    @Column(name = "profile_picture", length = 500)
    private String profilePicture;

    @Column(length = 1000)
    private String bio;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified")
    private Boolean isPhoneVerified = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
