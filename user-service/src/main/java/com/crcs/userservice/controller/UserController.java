package com.crcs.userservice.controller;

import com.crcs.userservice.dto.request.CreateUserProfileRequestDTO;
import com.crcs.userservice.dto.request.UpdateUserProfileRequestDTO;
import com.crcs.userservice.dto.response.ApiResponseDTO;
import com.crcs.userservice.dto.response.PageResponseDTO;
import com.crcs.userservice.dto.response.UserProfileResponseDTO;
import com.crcs.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing user profiles")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create user profile", description = "Create a new user profile")
    @PostMapping
    public ResponseEntity<UserProfileResponseDTO> createUserProfile(
            @Valid @RequestBody CreateUserProfileRequestDTO request) {
        logger.info("Creating user profile for userId: {}", request.getUserId());
        UserProfileResponseDTO profile = userService.createUserProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @Operation(summary = "Get user profile", description = "Get user profile by ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(@PathVariable("userId") String userId) {
        logger.info("Fetching user profile for userId: {}", userId);
        UserProfileResponseDTO profile = userService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Update user profile", description = "Update user profile information. Creates profile if it does not exist (e.g. after signup via auth).")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponseDTO> updateUserProfile(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UpdateUserProfileRequestDTO request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        logger.info("Updating user profile for userId: {}", userId);
        UserProfileResponseDTO updatedProfile = userService.updateProfile(userId, request, userRole);
        ApiResponseDTO response = new ApiResponseDTO("Profile updated successfully", updatedProfile);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users", description = "Get paginated list of all active users")
    @GetMapping
    public ResponseEntity<PageResponseDTO<UserProfileResponseDTO>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        logger.info("Fetching all users - page: {}, size: {}", page, size);
        PageResponseDTO<UserProfileResponseDTO> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Deactivate user", description = "Deactivate user account")
    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponseDTO> deactivateUser(@PathVariable("userId") String userId) {
        logger.info("Deactivating user: {}", userId);
        userService.deactivateUser(userId);
        ApiResponseDTO response = new ApiResponseDTO("Account deactivated successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Permanently delete user account")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDTO> deleteUser(@PathVariable("userId") String userId) {
        logger.info("Deleting user: {}", userId);
        userService.deleteUser(userId);
        ApiResponseDTO response = new ApiResponseDTO("User deleted successfully");
        return ResponseEntity.ok(response);
    }
}
