package com.beautystock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Strategy for handling Spring Security exceptions.
 */
public class SecurityExceptionStrategy implements ExceptionHandlingStrategy {
    @Override
    public ResponseEntity<?> handle(Exception ex, WebRequest request) {
        if (!(ex instanceof org.springframework.security.access.AccessDeniedException)) {
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("code", "SECURITY-403");
        body.put("message", "You do not have permission to access this resource");

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}
