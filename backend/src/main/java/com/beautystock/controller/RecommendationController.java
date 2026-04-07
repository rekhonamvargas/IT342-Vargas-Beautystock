package com.beautystock.controller;

import com.beautystock.entity.UserRole;
import com.beautystock.repository.UserRepository;
import com.beautystock.service.WeatherService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Recommendation Controller - Provides weather-based skincare advice (SDD Section 2.4)
 * API Specification: Section 5.2 - Endpoint Specifications
 * Response Format: Section 5.1 - API Standards
 */
@RestController
@RequestMapping("/v1/recommendations")
public class RecommendationController {

    private final WeatherService weatherService;
    private final UserRepository userRepository;

    public RecommendationController(WeatherService weatherService, UserRepository userRepository) {
        this.weatherService = weatherService;
        this.userRepository = userRepository;
    }

    /**
     * GET /api/v1/recommendations/youth/weather
     * 
     * SDD Reference: Feature: Weather-Based Skincare Recommendation (RBAC)
     * - Provides age-appropriate skincare advice based on weather conditions
     * - ROLE_YOUTH only access
     * - Requires authenticated user with city set
     * 
     * Response Format (SDD 5.1):
     * {
     *   "success": boolean,
     *   "data": { recommendation object },
     *   "error": null,
     *   "timestamp": ISO-8601
     * }
     */
    @GetMapping("/youth/weather")
    public ResponseEntity<?> getYouthWeatherAdvice() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            
            var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // SDD AC-3: Age-Based Weather Recommendation (RBAC)
            if (user.getRole() != UserRole.ROLE_YOUTH) {
                return ResponseEntity.status(403).body(buildErrorResponse(
                    "AUTH-003",
                    "Insufficient permissions (RBAC restriction)",
                    "Only ROLE_YOUTH can access this endpoint"
                ));
            }

            // Validation: User must have city set
            if (user.getCity() == null || user.getCity().isEmpty()) {
                return ResponseEntity.ok(buildErrorDataResponse(
                    "Please set your city in your profile to get weather-based skincare advice.",
                    "Youth Skincare Advice"
                ));
            }

            // Fetch weather-based recommendation
            Map<String, Object> recommendation = weatherService.getSkincareRecommendation(
                user.getCity(), 
                UserRole.ROLE_YOUTH
            );
            
            return ResponseEntity.ok(buildSuccessResponse(recommendation));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(buildErrorResponse(
                "SYSTEM-001",
                "Internal server error",
                e.getMessage()
            ));
        }
    }

    /**
     * GET /api/v1/recommendations/adult/weather
     * 
     * SDD Reference: Feature: Weather-Based Skincare Recommendation (RBAC)
     * - Provides age-appropriate skincare advice based on weather conditions
     * - ROLE_ADULT only access
     * - Requires authenticated user with city set
     * 
     * Response Format (SDD 5.1):
     * {
     *   "success": boolean,
     *   "data": { recommendation object },
     *   "error": null,
     *   "timestamp": ISO-8601
     * }
     */
    @GetMapping("/adult/weather")
    public ResponseEntity<?> getAdultWeatherAdvice() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            
            var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // SDD AC-3: Age-Based Weather Recommendation (RBAC)
            if (user.getRole() != UserRole.ROLE_ADULT) {
                return ResponseEntity.status(403).body(buildErrorResponse(
                    "AUTH-003",
                    "Insufficient permissions (RBAC restriction)",
                    "Only ROLE_ADULT can access this endpoint"
                ));
            }

            // Validation: User must have city set
            if (user.getCity() == null || user.getCity().isEmpty()) {
                return ResponseEntity.ok(buildErrorDataResponse(
                    "Please set your city in your profile to get weather-based skincare advice.",
                    "Adult Skincare Advice"
                ));
            }

            // Fetch weather-based recommendation
            Map<String, Object> recommendation = weatherService.getSkincareRecommendation(
                user.getCity(), 
                UserRole.ROLE_ADULT
            );
            
            return ResponseEntity.ok(buildSuccessResponse(recommendation));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(buildErrorResponse(
                "SYSTEM-001",
                "Internal server error",
                e.getMessage()
            ));
        }
    }

    // ==================== Response Builders (SDD Section 5.1) ====================

    /**
     * Builds success response wrapper (SDD 5.1 - Response Structure)
     */
    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("error", null);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    /**
     * Builds error response wrapper (SDD 5.1 - Error Handling)
     * Error Code Examples from SDD Section 5.3
     */
    private Map<String, Object> buildErrorResponse(String code, String message, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("data", null);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("details", details);
        response.put("error", error);
        
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    /**
     * Builds error data response when user doesn't have city set
     * Returns partial data (title/advice) without weather metrics
     */
    private Map<String, Object> buildErrorDataResponse(String advice, String title) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("advice", advice);
        data.put("temperature", null);
        data.put("humidity", null);
        data.put("emoji", title.contains("Youth") ? "🌟" : "✨");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("error", null);
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}
