package com.beautystock.features.profile.controller;

import com.beautystock.features.authentication.dto.UserProfileDTO;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.features.profile.dto.ProfileUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> {
                    UserProfileDTO profile = new UserProfileDTO();
                    profile.setId(user.getId());
                    profile.setEmail(user.getEmail());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setFullName(user.getFullName());
                    profile.setRole(user.getRole().name());
                    profile.setProfileImageUrl(user.getProfileImageUrl());
                    profile.setCreatedAt(user.getCreatedAt());
                    profile.setCity(user.getCity());
                    return ResponseEntity.ok((Object) profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> {
                    if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
                        user.setFirstName(dto.getFirstName().trim());
                    }
                    if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
                        user.setLastName(dto.getLastName().trim());
                    }
                    if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
                        user.setFullName(dto.getFullName().trim());
                    }
                    if (dto.getCity() != null) {
                        user.setCity(dto.getCity().isBlank() ? null : dto.getCity().trim());
                    }
                    if (dto.getProfileImageUrl() != null) {
                        user.setProfileImageUrl(dto.getProfileImageUrl());
                    }
                    userRepository.save(user);

                    UserProfileDTO profile = new UserProfileDTO();
                    profile.setId(user.getId());
                    profile.setEmail(user.getEmail());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setFullName(user.getFullName());
                    profile.setRole(user.getRole().name());
                    profile.setProfileImageUrl(user.getProfileImageUrl());
                    profile.setCreatedAt(user.getCreatedAt());
                    profile.setCity(user.getCity());
                    return ResponseEntity.ok((Object) profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
