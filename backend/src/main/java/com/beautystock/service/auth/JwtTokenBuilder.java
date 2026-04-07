package com.beautystock.service.auth;

import com.beautystock.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Builder Pattern implementation for JWT token creation.
 * Provides fluent API for configurable token generation.
 * Isolates token creation logic from business logic.
 */
@Component
public class JwtTokenBuilder {
    private final String jwtSecret;
    private final long jwtExpiration;

    public JwtTokenBuilder(@Value("${jwt.secret}") String jwtSecret, 
                          @Value("${jwt.expiration}") long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Build JWT token for user.
     * 
     * @param user User entity
     * @return Signed JWT token string
     */
    public String buildToken(User user) {
        return this.forUser(user.getEmail())
                .withClaim("userId", user.getId())
                .withClaim("role", user.getRole().name())
                .build();
    }

    /**
     * Fluent API entry point.
     */
    public JwtTokenBuilder forUser(String email) {
        this.subject = email;
        return this;
    }

    // Builder fields
    private String subject;
    private Date issuedAt;
    private Date expiration;

    /**
     * Add custom claims to token (fluent API).
     */
    public JwtTokenBuilder withClaim(String key, Object value) {
        if (value instanceof String) {
            this.claims.put(key, (String) value);
        } else if (value instanceof Long) {
            this.claims.put(key, value.toString());
        } else {
            this.claims.put(key, value.toString());
        }
        return this;
    }

    private java.util.Map<String, String> claims = new java.util.HashMap<>();

    /**
     * Set token expiration (default: jwtExpiration milliseconds from now).
     */
    public JwtTokenBuilder expiresIn(long expirationMs) {
        this.expiration = new Date(System.currentTimeMillis() + expirationMs);
        return this;
    }

    /**
     * Build the final JWT token.
     */
    public String build() {
        Date now = new Date();
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        if (this.expiration == null) {
            this.expiration = new Date(now.getTime() + jwtExpiration);
        }

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(this.expiration)
                .claims(claims)
                .signWith(key)
                .compact();
    }
}
