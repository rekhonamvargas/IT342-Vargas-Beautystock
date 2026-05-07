package com.beautystock.features.authentication.service;

import com.beautystock.features.authentication.dto.AuthResponseDTO;
import com.beautystock.features.authentication.dto.GoogleAuthRequestDTO;
import com.beautystock.features.authentication.dto.LoginDTO;
import com.beautystock.features.authentication.dto.RegisterDTO;
import com.beautystock.features.authentication.dto.UserProfileDTO;
import com.beautystock.features.authentication.entity.RefreshToken;
import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.entity.UserRole;
import com.beautystock.features.authentication.repository.RefreshTokenRepository;
import com.beautystock.features.authentication.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.beautystock.infrastructure.service.EmailService;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponseDTO register(RegisterDTO dto) {
        // VALID-001: passwords must match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "{\"code\":\"VALID-001\",\"message\":\"Passwords do not match\"}");
        }

        // DB-002: duplicate email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "{\"code\":\"DB-002\",\"message\":\"Email already registered\"}");
        }

        UserRole role = "YOUTH".equalsIgnoreCase(dto.getAgeRange())
                ? UserRole.ROLE_YOUTH
                : UserRole.ROLE_ADULT;

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setFullName(dto.getFullName());
        user.setRole(role);
        user.setEmailVerified(true);

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());

        // Send welcome email
        emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName());

        String jwt = generateJWT(saved);
        String refresh = createRefreshToken(saved);

        return AuthResponseDTO.builder()
                .token(jwt)
                .refreshToken(refresh)
                .user(mapToProfile(saved))
                .isNewUser(true)
                .build();
    }

    @Transactional
    public AuthResponseDTO login(LoginDTO dto) {
        // AUTH-001: invalid credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "{\"code\":\"AUTH-001\",\"message\":\"Invalid email or password\"}");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "{\"code\":\"AUTH-001\",\"message\":\"Invalid email or password\"}"));

        // Update last login timestamp
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String jwt = generateJWT(user);
        String refresh = createRefreshToken(user);

        return AuthResponseDTO.builder()
                .token(jwt)
                .refreshToken(refresh)
                .user(mapToProfile(user))
                .build();
    }

    @Transactional
    public AuthResponseDTO authenticateWithGoogle(GoogleAuthRequestDTO dto) {
        // This endpoint accepts a Google ID token from the client-side flow
        // Verify the token and authenticate the user
        try {
            // For production, use GoogleIdTokenVerifier to validate the token
            // For now, extract basic info from the token (simplified)
            String idToken = dto.getIdToken();
            
            // Decode JWT to extract payload (without verification for demo)
            // In production, always verify with Google's API
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "{\"code\":\"AUTH-002\",\"message\":\"Invalid Google ID token\"}");
            }
            
            // Decode base64 payload
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            java.util.Map<String, Object> payload = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payloadJson, java.util.Map.class);
            
            String googleId = (String) payload.get("sub");
            String email = (String) payload.get("email");
            String fullName = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            Boolean emailVerified = (Boolean) payload.get("email_verified");
            
            if (email == null || googleId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "{\"code\":\"AUTH-002\",\"message\":\"Invalid Google ID token\"}");
            }
            
            // Find existing user by email OR googleId
            Optional<User> existingUser = userRepository.findByEmail(email);
            boolean isNewUser = false;
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                // Update googleId if not set
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                }
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            } else {
                Optional<User> byGoogleId = userRepository.findByGoogleId(googleId);
                if (byGoogleId.isPresent()) {
                    user = byGoogleId.get();
                    user.setLastLoginAt(LocalDateTime.now());
                    userRepository.save(user);
                } else {
                    // Create new user
                    UserRole role = "YOUTH".equalsIgnoreCase(dto.getAgeRange())
                            ? UserRole.ROLE_YOUTH
                            : UserRole.ROLE_ADULT;

                    user = new User();
                    user.setEmail(email);
                    user.setFullName(fullName != null ? fullName : email.split("@")[0]);
                    user.setProfileImageUrl(pictureUrl);
                    user.setRole(role);
                    user.setEmailVerified(emailVerified != null && emailVerified);
                    user.setGoogleId(googleId);
                    // Set a placeholder password hash since OAuth users don't use password
                    String randomPassword = UUID.randomUUID().toString();
                    user.setPasswordHash(passwordEncoder.encode(randomPassword));

                    user = userRepository.save(user);
                    isNewUser = true;
                    log.info("User created via Google OAuth: {}", user.getEmail());
                }
            }

            String jwt = generateJWT(user);
            String refresh = createRefreshToken(user);

            return AuthResponseDTO.builder()
                    .token(jwt)
                    .refreshToken(refresh)
                    .user(mapToProfile(user))
                    .isNewUser(isNewUser)
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google OAuth authentication failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"code\":\"AUTH-003\",\"message\":\"Google authentication failed\"}");
        }
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            refreshTokenRepository.deleteByUser(user);
            log.info("Revoked all refresh tokens for: {}", email);
        });
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        rt.setRevoked(false);
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }

    private String generateJWT(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private UserProfileDTO mapToProfile(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
