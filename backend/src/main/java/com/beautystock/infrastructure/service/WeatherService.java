package com.beautystock.infrastructure.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${app.weather.api-key}")
    private String apiKey;

    @Value("${app.weather.api-url}")
    private String apiUrl;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Fetches current weather from OpenWeatherMap for the given query string.
     * Throws HttpClientErrorException.NotFound if the city is not found,
     * HttpClientErrorException.Unauthorized if the API key is invalid,
     * or HttpClientErrorException (SERVICE_UNAVAILABLE) for network errors.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchWeatherData(String query) {
        URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .path("/weather")
                .queryParam("q", query)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .build().encode().toUri();

        String masked = uri.toString().replaceAll("(appid=)[^&]+", "$1[REDACTED]");
        System.out.println("Fetching weather URL: " + masked);

        try {
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response != null && response.containsKey("cod")) {
                String cod = response.get("cod").toString();
                if ("404".equals(cod) || "400".equals(cod)) {
                    throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "API returned cod=" + cod);
                }
            }
            return response;
        } catch (HttpClientErrorException e) {
            String body = "";
            try { body = e.getResponseBodyAsString(); } catch (Exception ignore) {}
            System.err.println("Weather API error for '" + query + "': " + e.getStatusCode() + " body=" + body);
            throw e;
        } catch (Exception e) {
            System.err.println("Weather fetch error for '" + query + "': " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }
}
