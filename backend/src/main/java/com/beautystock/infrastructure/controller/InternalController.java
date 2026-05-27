package com.beautystock.infrastructure.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class InternalController {

    @Value("${app.weather.api-key:}")
    private String weatherApiKey;

    @GetMapping("/weather-key")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> weatherKeyStatus() {
        Map<String, Object> resp = new HashMap<>();
        boolean configured = weatherApiKey != null && !weatherApiKey.isBlank();
        resp.put("configured", configured);
        if (configured) {
            String masked = "[REDACTED]";
            try {
                if (weatherApiKey.length() > 4) {
                    masked = "[REDACTED]-" + weatherApiKey.substring(weatherApiKey.length() - 4);
                }
            } catch (Exception ignored) {}
            resp.put("masked", masked);
        } else {
            resp.put("masked", null);
        }
        return ResponseEntity.ok(resp);
    }
}
