package com.beautystock.service;

import com.beautystock.dto.AuthResponseDTO;
import com.beautystock.dto.LoginDTO;
import com.beautystock.dto.RegisterDTO;
import com.beautystock.dto.UserProfileDTO;
import com.beautystock.entity.RefreshToken;
import com.beautystock.entity.User;
import com.beautystock.entity.UserRole;
import com.beautystock.repository.RefreshTokenRepository;
import com.beautystock.repository.UserRepository;
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


import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
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
