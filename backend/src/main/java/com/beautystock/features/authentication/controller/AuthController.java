package com.beautystock.features.authentication.controller;

import com.beautystock.features.authentication.dto.AuthResponseDTO;
import com.beautystock.features.authentication.dto.GoogleAuthRequestDTO;
import com.beautystock.features.authentication.dto.LoginDTO;
import com.beautystock.features.authentication.dto.RegisterDTO;
import com.beautystock.features.authentication.dto.UserProfileDTO;
import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.entity.UserRole;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.features.authentication.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> google(@RequestBody GoogleAuthRequestDTO request) {
        return ResponseEntity.ok(authService.authenticateWithGoogle(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            authService.logout(authentication.getName());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        Optional<User> u = userRepository.findByEmailIgnoreCase(email);
        if (u.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = u.get();
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .profileImageUrl(user.getProfileImageUrl())
                .googleId(user.getGoogleId())
                .createdAt(user.getCreatedAt())
                .city(user.getCity())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> updateRole(Authentication authentication, @RequestBody Map<String, String> request) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        String roleValue = request.get("role");
        if (roleValue == null || roleValue.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UserRole role;
        try {
            role = UserRole.valueOf(roleValue);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> u = userRepository.findByEmailIgnoreCase(authentication.getName());
        if (u.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = u.get();
        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .profileImageUrl(user.getProfileImageUrl())
                .googleId(user.getGoogleId())
                .createdAt(user.getCreatedAt())
                .city(user.getCity())
                .build());
    }
}
