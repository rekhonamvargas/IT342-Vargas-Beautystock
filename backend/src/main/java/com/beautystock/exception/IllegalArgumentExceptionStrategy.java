package com.beautystock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Strategy for handling illegal argument exceptions.
 */
public class IllegalArgumentExceptionStrategy implements ExceptionHandlingStrategy {
    @Override
    public ResponseEntity<?> handle(Exception ex, WebRequest request) {
        if (!(ex instanceof IllegalArgumentException)) {
            return null;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("code", "INVALID-ARG-400");
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
