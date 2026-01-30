package com.crcs.userservice.repository;

import com.crcs.userservice.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);
    Optional<UserProfile> findByEmail(String email);
    Page<UserProfile> findByIsActiveTrue(Pageable pageable);
}
