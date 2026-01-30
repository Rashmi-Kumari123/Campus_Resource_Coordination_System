package com.crcs.authservice.controller;

import com.crcs.authservice.dto.*;
import com.crcs.authservice.service.AuthService;
import com.crcs.authservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class AuthController {
  private final AuthService authService;
  private final JwtUtil jwtUtil;

  public AuthController(AuthService authService, JwtUtil jwtUtil) {
    this.authService = authService;
    this.jwtUtil = jwtUtil;
  }

  @Operation(summary = "User login", description = "Authenticate user and receive JWT tokens")
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
    Optional<LoginResponseDTO> loginResponseOptional = authService.authenticate(loginRequestDTO);

    if (loginResponseOptional.isEmpty()) {
      ApiResponseDTO errorResponse = new ApiResponseDTO("Invalid email or password");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    return ResponseEntity.ok(loginResponseOptional.get());
  }

  @Operation(summary = "User signup", description = "Register new user and receive JWT tokens")
  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
    if (signupRequestDTO.getPassword() != null && 
        signupRequestDTO.getPassword().equals(signupRequestDTO.getEmail())) {
      ApiResponseDTO errorResponse = new ApiResponseDTO("Password cannot be the same as email address");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    Optional<SignupResponseDTO> signupResponseOptional = authService.registerAndAuthenticate(signupRequestDTO);

    if (signupResponseOptional.isEmpty()) {
      ApiResponseDTO errorResponse = new ApiResponseDTO("User already exists");
      return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(signupResponseOptional.get());
  }

  @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
    Optional<LoginResponseDTO> responseOptional = authService.refreshToken(request);
    
    if (responseOptional.isEmpty()) {
      ApiResponseDTO errorResponse = new ApiResponseDTO("Invalid or expired refresh token");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    return ResponseEntity.ok(responseOptional.get());
  }

  @Operation(summary = "User logout", description = "Logout user and invalidate refresh tokens")
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      ApiResponseDTO errorResponse = new ApiResponseDTO("Missing or invalid authorization header");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    String token = authHeader.substring(7);
    try {
      String userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        ApiResponseDTO errorResponse = new ApiResponseDTO("Invalid token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
      }
      
      boolean success = authService.logout(userId);
      if (success) {
        ApiResponseDTO response = new ApiResponseDTO("Logged out successfully");
        return ResponseEntity.ok(response);
      } else {
        ApiResponseDTO errorResponse = new ApiResponseDTO("Logout failed");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
      }
    } catch (Exception e) {
      ApiResponseDTO errorResponse = new ApiResponseDTO("Invalid token");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
  }

  @Operation(summary = "Validate token", description = "Validate JWT token (for gateway)")
  @GetMapping("/validate")
  public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    return authService.validateToken(authHeader.substring(7))
        ? ResponseEntity.ok().build()
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }
}
