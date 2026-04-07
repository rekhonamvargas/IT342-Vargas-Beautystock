package com.beautystock.service;

import com.beautystock.entity.UserRole;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Weather Service - Provides weather-based skincare recommendations.
 * This is a placeholder implementation for weather-based recommendations.
 */
@Service
public class WeatherService {

    public Map<String, Object> getSkincareRecommendation(String city, UserRole role) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("city", city);
        recommendation.put("role", role);
        recommendation.put("recommendation", "Stay hydrated and apply sunscreen");
        recommendation.put("temperature", "22°C");
        recommendation.put("humidity", "60%");
        return recommendation;
    }
}
