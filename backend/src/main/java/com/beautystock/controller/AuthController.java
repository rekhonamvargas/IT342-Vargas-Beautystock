package com.beautystock.controller;

import com.beautystock.dto.AuthResponseDTO;
import com.beautystock.dto.LoginDTO;
import com.beautystock.dto.RegisterDTO;
import com.beautystock.dto.UserProfileDTO;
import com.beautystock.entity.User;
import com.beautystock.repository.UserRepository;
import com.beautystock.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
                    profile.setFullName(user.getFullName());
                    profile.setRole(user.getRole().name());
                    profile.setProfileImageUrl(user.getProfileImageUrl());
                    profile.setCreatedAt(user.getCreatedAt());
                    return ResponseEntity.ok((Object) profile);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found"));
    }
}
