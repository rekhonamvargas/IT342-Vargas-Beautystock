package com.beautystock.features.recommendations.controller;

import com.beautystock.features.recommendations.dto.WeatherResponse;
import com.beautystock.features.recommendations.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@PreAuthorize("isAuthenticated()")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/youth/weather")
    public ResponseEntity<WeatherResponse> getYouthWeather() {
        WeatherResponse response = recommendationService.getWeatherAdvice("ROLE_YOUTH");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/adult/weather")
    public ResponseEntity<WeatherResponse> getAdultWeather() {
        WeatherResponse response = recommendationService.getWeatherAdvice("ROLE_ADULT");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weather")
    public ResponseEntity<WeatherResponse> getWeatherByCity(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "ROLE_USER") String role) {
        WeatherResponse response = recommendationService.getWeatherAdviceForCity(role, city);
        return ResponseEntity.ok(response);
    }
}
