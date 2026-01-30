package com.crcs.authservice.service;

import com.crcs.authservice.dto.SignupRequestDTO;
import com.crcs.authservice.model.User;
import com.crcs.authservice.repository.UserRepository;
import com.crcs.common.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User insertNewUser(SignupRequestDTO request) {
        User user = new User();
        // Validate and set role
        UserRole userRole = request.getUserRole();
        
        user.setId(UUID.randomUUID().toString());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole.getValue());
        return userRepository.save(user);
    }
}
