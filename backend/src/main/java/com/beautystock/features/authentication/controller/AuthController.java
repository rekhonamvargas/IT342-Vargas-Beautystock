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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /** POST /api/v1/auth/register */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        AuthResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** POST /api/v1/auth/login */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    /** POST /api/v1/auth/google — Google OAuth2 ID token authentication */
    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleAuth(@Valid @RequestBody GoogleAuthRequestDTO dto) {
        AuthResponseDTO response = authService.authenticateWithGoogle(dto);
        return ResponseEntity.ok(response);
    }

    /** POST /api/v1/auth/logout — revokes all refresh tokens */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(email);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/v1/auth/me */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(user -> {
                    UserProfileDTO profile = new UserProfileDTO();
                    profile.setId(user.getId());
                    profile.setEmail(user.getEmail());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setFullName(user.getFullName());
                    profile.setRole(user.getRole().name());
                    profile.setProfileImageUrl(user.getProfileImageUrl());
                    profile.setGoogleId(user.getGoogleId());
                    profile.setNotificationEmail(user.getNotificationEmail());
                    profile.setNotificationsEnabled(user.isNotificationsEnabled());
                    profile.setCreatedAt(user.getCreatedAt());
                    profile.setCity(user.getCity());
                    return ResponseEntity.ok((Object) profile);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found"));
    }

    /** PATCH /api/v1/auth/me/role - Update user role */
    @PatchMapping("/me/role")
    public ResponseEntity<?> updateUserRole(@RequestBody Map<String, String> roleUpdate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String roleStr = roleUpdate.get("role");

        if (roleStr == null) {
            return ResponseEntity.badRequest().body("Role is required");
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    try {
                        user.setRole(UserRole.valueOf(roleStr));
                        userRepository.save(user);
                        
                        UserProfileDTO profile = new UserProfileDTO();
                        profile.setId(user.getId());
                        profile.setEmail(user.getEmail());
                        profile.setFirstName(user.getFirstName());
                        profile.setLastName(user.getLastName());
                        profile.setFullName(user.getFullName());
                        profile.setRole(user.getRole().name());
                        profile.setProfileImageUrl(user.getProfileImageUrl());
                        profile.setGoogleId(user.getGoogleId());
                        profile.setNotificationEmail(user.getNotificationEmail());
                        profile.setNotificationsEnabled(user.isNotificationsEnabled());
                        profile.setCreatedAt(user.getCreatedAt());
                        profile.setCity(user.getCity());
                        return ResponseEntity.ok((Object) profile);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Invalid role: " + roleStr);
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found"));
    }
}
