package com.beautystock.features.recommendations.service;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.features.recommendations.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class RecommendationService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${app.weather.api-key}")
    private String weatherApiKey;

    @Value("${app.weather.api-url}")
    private String weatherApiUrl;

    public RecommendationService(RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    public WeatherResponse getWeatherAdvice(String role) {
        try {
            String currentEmail = getCurrentEmail();
            User user = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String city = user.getCity();
            if (city == null || city.isBlank()) {
                return new WeatherResponse(null, null, null, null, "Please set your city to get weather-based skincare tips");
            }

            // Normalize the city input (trim whitespace, collapse spaces)
            String normalizedCity = city.trim().replaceAll("\\s+", " ");

            // Prioritize Philippine cities by querying OpenWeatherMap with the PH
            // country code first (e.g. "Cebu,PH"). If the user already supplied a
            // country code in their city string (e.g. "Paris,FR"), respect it and
            // skip the PH-biased lookup.
            boolean userSuppliedCountry = normalizedCity.contains(",");

            Map<String, Object> weatherData = null;
            String resolvedQuery = null;

            try {
                if (!userSuppliedCountry) {
                    // First attempt: append ",PH" so Philippine cities resolve correctly.
                    String phQuery = normalizedCity + ",PH";
                    try {
                        weatherData = fetchWeather(phQuery);
                        resolvedQuery = phQuery;
                    } catch (HttpClientErrorException.NotFound phNotFound) {
                        // City not found in PH — fall back to a plain lookup so
                        // international cities (Tokyo, New York, etc.) still work.
                        System.out.println("City '" + normalizedCity + "' not found in PH; falling back to global lookup.");
                    }
                }

                if (weatherData == null) {
                    try {
                        weatherData = fetchWeather(normalizedCity);
                        resolvedQuery = normalizedCity;
                    } catch (HttpClientErrorException.NotFound globalNotFound) {
                        String errorMsg = "City '" + city + "' was not found. Please check the spelling or try a nearby major city.";
                        System.err.println(errorMsg);
                        return new WeatherResponse(city, null, null, null, errorMsg);
                    }
                }

                if (weatherData == null) {
                    String errorMsg = "Unable to fetch weather data for " + city;
                    System.err.println(errorMsg);
                    return new WeatherResponse(city, null, null, null, errorMsg);
                }

                System.out.println("Weather resolved via query: " + resolvedQuery);

                Double temperature = null;
                Integer humidity = null;
                String condition = null;
                
                // Extract main weather info
                Map<String, Object> main = (Map<String, Object>) weatherData.get("main");
                if (main != null) {
                    Object tempObj = main.get("temp");
                    Object humidityObj = main.get("humidity");
                    if (tempObj != null) {
                        temperature = ((Number) tempObj).doubleValue();
                    }
                    if (humidityObj != null) {
                        humidity = ((Number) humidityObj).intValue();
                    }
                }

                // Extract weather condition
                java.util.List<Map<String, Object>> weather = (java.util.List<Map<String, Object>>) weatherData.get("weather");
                if (weather != null && !weather.isEmpty()) {
                    condition = (String) weather.get(0).get("main");
                }

                System.out.println("Weather data - Temp: " + temperature + ", Humidity: " + humidity + ", Condition: " + condition);

                // Generate skincare advice based on role, temperature, and humidity
                String advice = generateAdvice(role, temperature, humidity, condition);

                return new WeatherResponse(city, temperature, humidity, condition, advice);
            } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
                String errorMsg = "Weather API Error: Invalid API key. Please check your OpenWeatherMap API key configuration.";
                System.err.println(errorMsg);
                e.printStackTrace();
                return new WeatherResponse(city, null, null, null, errorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error fetching weather data: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            return new WeatherResponse(null, null, null, null, errorMsg);
        }
    }

    private String generateAdvice(String role, Double temperature, Integer humidity, String condition) {
        StringBuilder advice = new StringBuilder();

        if ("ROLE_YOUTH".equals(role)) {
            advice.append("👧 Youth Skincare Tip: ");
        } else {
            advice.append("👩 Adult Skincare Tip: ");
        }

        if (temperature != null && temperature > 30) {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("It's hot! Use lightweight, oil-free moisturizers. ");
            } else {
                advice.append("Hot weather—use hydrating serums under lightweight moisturizers. ");
            }
        } else if (temperature != null && temperature < 10) {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("It's cold! Protect with a richer moisturizer and SPF. ");
            } else {
                advice.append("Cold weather—strengthen your barrier with nourishing oils. ");
            }
        } else if (temperature != null) {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("Mild temperatures are perfect for consistent skincare routine. ");
            } else {
                advice.append("Mild temperatures—maintain your regular skincare routine. ");
            }
        }

        if (humidity != null && humidity > 70) {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("High humidity detected—skip heavy creams, use gel-based products. ");
            } else {
                advice.append("High humidity—use matte products and reduce heavy oils. ");
            }
        } else if (humidity != null && humidity < 30) {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("Low humidity—hydrate with serums and lightweight moisturizers. ");
            } else {
                advice.append("Dry conditions—boost hydration with hyaluronic acid and humectants. ");
            }
        } else if (humidity != null) {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("Moderate humidity—balanced hydration levels today. ");
            } else {
                advice.append("Moderate humidity—maintain balanced hydration. ");
            }
        }

        if (condition != null) {
            if (condition.contains("Rain") || condition.contains("Rainy")) {
                if ("ROLE_YOUTH".equals(role)) {
                    advice.append("Rainy weather—ensure good skin barrier with SPF.");
                } else {
                    advice.append("Rainy weather—maintain SPF and keep skin hydrated.");
                }
            } else if (condition.contains("Clear") || condition.contains("Sunny")) {
                if ("ROLE_YOUTH".equals(role)) {
                    advice.append("Sunny weather—don't forget broad-spectrum SPF 30+!");
                } else {
                    advice.append("Sunny weather—reapply SPF 50+ every 2 hours for UV protection!");
                }
            } else if (condition.contains("Cloud") || condition.contains("Overcast")) {
                if ("ROLE_YOUTH".equals(role)) {
                    advice.append("Cloudy weather—still use SPF as UV rays penetrate clouds.");
                } else {
                    advice.append("Cloudy weather—UV protection is still needed; apply SPF.");
                }
            } else if (condition.contains("Snow") || condition.contains("Sleet")) {
                if ("ROLE_YOUTH".equals(role)) {
                    advice.append("Snowy weather—protect skin from dry winds with richer moisturizers.");
                } else {
                    advice.append("Snowy weather—boost barrier protection with ceramides and SPF.");
                }
            } else {
                if ("ROLE_YOUTH".equals(role)) {
                    advice.append("Weather: " + condition + "—follow your regular skincare routine.");
                } else {
                    advice.append("Weather: " + condition + "—adjust routine as needed for conditions.");
                }
            }
        } else {
            if ("ROLE_YOUTH".equals(role)) {
                advice.append("Set your city to get weather-specific recommendations.");
            } else {
                advice.append("Set your city for personalized weather-based skincare advice.");
            }
        }

        return advice.toString().trim();
    }

    private Map<String, Object> fetchWeather(String query) {
        String url = weatherApiUrl + "/weather?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&appid=" + weatherApiKey + "&units=metric";
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("cod")) {
                String cod = response.get("cod").toString();
                if ("404".equals(cod) || "400".equals(cod)) {
                    throw new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND);
                }
            }
            return response;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("Weather API Error for query '" + query + "': " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error fetching weather for query '" + query + "': " + e.getMessage());
            throw new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("User not authenticated");
    }
}
