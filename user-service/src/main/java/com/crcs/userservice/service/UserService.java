package com.crcs.userservice.service;

import com.crcs.common.enums.UserRole;
import com.crcs.userservice.dto.request.CreateUserProfileRequestDTO;
import com.crcs.userservice.dto.request.UpdateUserProfileRequestDTO;
import com.crcs.userservice.dto.response.PageResponseDTO;
import com.crcs.userservice.dto.response.UserProfileResponseDTO;
import com.crcs.userservice.exception.UserNotFoundException;
import com.crcs.userservice.model.UserProfile;
import com.crcs.userservice.repository.UserProfileRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserProfileRepository userProfileRepository;

    public UserService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileResponseDTO createUserProfile(CreateUserProfileRequestDTO request) {
        // Check if profile already exists
        if (userProfileRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("User profile already exists for userId: " + request.getUserId());
        }

        // Validate and set role using DTO method
        UserRole userRole = request.getUserRole();
        
        UserProfile profile = UserProfile.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .name(request.getName())
                .role(userRole.getValue())
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .isActive(true)
                .build();

        profile = userProfileRepository.save(profile);
        logger.info("User profile created for userId: {}", request.getUserId());
        return mapToResponseDTO(profile);
    }

    public UserProfileResponseDTO getProfile(String userId) {
        return userProfileRepository.findByUserId(userId)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    @Transactional
    public UserProfileResponseDTO updateProfile(String userId, UpdateUserProfileRequestDTO request, String userRoleFromHeader) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> createProfileFromUpdate(userId, request, userRoleFromHeader));

        if (StringUtils.isNotBlank(request.getName())) profile.setName(request.getName());
        if (StringUtils.isNotBlank(request.getEmail())) profile.setEmail(request.getEmail());
        if (StringUtils.isNotBlank(request.getBio())) profile.setBio(request.getBio());
        if (StringUtils.isNotBlank(request.getProfilePicture())) profile.setProfilePicture(request.getProfilePicture());
        if (StringUtils.isNotBlank(request.getPhoneNumber())) profile.setPhoneNumber(request.getPhoneNumber());

        profile = userProfileRepository.save(profile);
        logger.info("User profile updated for userId: {}", userId);
        return mapToResponseDTO(profile);
    }

    /**
     * Create a new UserProfile when updating a user who signed up via auth-service
     * but has no profile in user-service yet (e.g. first time completing profile).
     */
    private UserProfile createProfileFromUpdate(String userId, UpdateUserProfileRequestDTO request, String userRoleFromHeader) {
        logger.info("Creating user profile for userId: {} (profile did not exist, upsert from update)", userId);
        if (StringUtils.isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email is required when creating profile. Include 'email' in the request body.");
        }
        String role = StringUtils.isNotBlank(userRoleFromHeader) ? userRoleFromHeader : "USER";
        return UserProfile.builder()
                .userId(userId)
                .email(request.getEmail())
                .name(request.getName())
                .role(role)
                .bio(request.getBio())
                .profilePicture(request.getProfilePicture())
                .phoneNumber(request.getPhoneNumber())
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .isActive(true)
                .build();
    }

    public PageResponseDTO<UserProfileResponseDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfile> userPage = userProfileRepository.findByIsActiveTrue(pageable);
        
        return new PageResponseDTO<>(
                userPage.getContent().stream()
                        .map(this::mapToResponseDTO)
                        .collect(Collectors.toList()),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast(),
                userPage.isFirst()
        );
    }

    @Transactional
    public void deactivateUser(String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        profile.setIsActive(false);
        userProfileRepository.save(profile);
        logger.info("User deactivated: {}", userId);
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        userProfileRepository.deleteById(userId);
        logger.info("User deleted: {}", userId);
    }

    private UserProfileResponseDTO mapToResponseDTO(UserProfile profile) {
        return UserProfileResponseDTO.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .name(profile.getName())
                .role(profile.getRole())
                .profilePicture(profile.getProfilePicture())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .isEmailVerified(profile.getIsEmailVerified())
                .isPhoneVerified(profile.getIsPhoneVerified())
                .isActive(profile.getIsActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
