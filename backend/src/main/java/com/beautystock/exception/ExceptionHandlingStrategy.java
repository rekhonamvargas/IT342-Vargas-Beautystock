package com.beautystock.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

/**
 * Strategy Pattern implementation for exception handling.
 * Defines contract for handling different exception types.
 */
public interface ExceptionHandlingStrategy {
    ResponseEntity<?> handle(Exception ex, WebRequest request);
}
