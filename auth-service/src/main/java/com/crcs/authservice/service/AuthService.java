package com.crcs.authservice.service;

import com.crcs.authservice.client.UserServiceClient;
import com.crcs.authservice.dto.*;
import com.crcs.authservice.model.RefreshToken;
import com.crcs.authservice.model.User;
import com.crcs.authservice.repository.RefreshTokenRepository;
import com.crcs.authservice.repository.UserRepository;
import com.crcs.authservice.util.JwtUtil;
import com.crcs.common.enums.UserRole;
import com.crcs.kafka.producer.NotificationEventPublisher;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationEventPublisher notificationEventPublisher;
    private final UserServiceClient userServiceClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository,
                      NotificationEventPublisher notificationEventPublisher,
                      UserServiceClient userServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.notificationEventPublisher = notificationEventPublisher;
        this.userServiceClient = userServiceClient;
    }

    public Optional<LoginResponseDTO> authenticate(LoginRequestDTO loginRequestDTO) {
        return userRepository.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                .map(u -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("role", u.getRole());
                    claims.put("userId", u.getId());
                    
                    String token = jwtUtil.generateToken(u.getEmail(), u.getRole(), claims);
                    String refreshToken = jwtUtil.generateRefreshToken(u.getEmail(), u.getRole(), claims);
                    
                    saveRefreshToken(u.getId(), refreshToken);
                    
                    UserInfoDTO userInfo = new UserInfoDTO(u.getId(), u.getEmail(), null, u.getRole());
                    
                    return new LoginResponseDTO(
                            token, 
                            refreshToken, 
                            jwtUtil.getAccessTokenExpiration(), 
                            claims,
                            userInfo
                    );
                });
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Transactional
    public Optional<SignupResponseDTO> registerAndAuthenticate(SignupRequestDTO request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            log.info("Email is already taken");
            return Optional.empty();
        }

        // Validate and set role
        UserRole userRole = request.getUserRole();
        
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(userRole.getValue());
        
        newUser = userRepository.save(newUser);
        
        // Create user profile in user-service (user_profile table) with signup data
        try {
            String name = StringUtils.isNotBlank(request.getName()) ? request.getName() : null;
            userServiceClient.createUserProfile(newUser.getId(), newUser.getEmail(), name, newUser.getRole());
        } catch (Exception e) {
            log.error("Failed to create user profile for userId: {}. Signup succeeded.", newUser.getId(), e);
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", newUser.getRole());
        claims.put("userId", newUser.getId());
        
        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getRole(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(newUser.getEmail(), newUser.getRole(), claims);
        
        saveRefreshToken(newUser.getId(), refreshToken);
        
        // Send welcome email via generic notification publisher (does not fail signup on error)
        try {
            String contactName = StringUtils.isNotBlank(request.getName()) ? request.getName() : "Valued User";
            notificationEventPublisher.publishWelcome(newUser.getEmail(), contactName, newUser.getId());
        } catch (Exception e) {
            log.error("Failed to send welcome email for user: {}", newUser.getEmail(), e);
        }
        
        UserInfoDTO userInfo = new UserInfoDTO(newUser.getId(), newUser.getEmail(), null, newUser.getRole());
        
        return Optional.of(new SignupResponseDTO(
                token,
                refreshToken,
                jwtUtil.getAccessTokenExpiration(),
                claims,
                userInfo
        ));
    }

    public Optional<LoginResponseDTO> refreshToken(RefreshTokenRequestDTO request) {
        try {
            jwtUtil.validateToken(request.getRefreshToken());
            Claims claims = jwtUtil.extractClaims(request.getRefreshToken());
            
            if (!"refresh".equals(claims.get("type"))) {
                return Optional.empty();
            }
            
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            String userId = claims.get("userId", String.class);
            
            Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
            if (refreshTokenOpt.isEmpty() || refreshTokenOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
                return Optional.empty();
            }
            
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("role", role);
            newClaims.put("userId", userId);
            
            String newToken = jwtUtil.generateToken(email, role, newClaims);
            String newRefreshToken = jwtUtil.generateRefreshToken(email, role, newClaims);
            
            refreshTokenRepository.delete(refreshTokenOpt.get());
            saveRefreshToken(userId, newRefreshToken);
            
            UserInfoDTO userInfo = new UserInfoDTO(userId, email, null, role);
            
            return Optional.of(new LoginResponseDTO(
                    newToken,
                    newRefreshToken,
                    jwtUtil.getAccessTokenExpiration(),
                    newClaims,
                    userInfo
            ));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
        return true;
    }

    private void saveRefreshToken(String userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        refreshTokenRepository.save(refreshToken);
    }
}
