package com.beautystock.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Global exception handler using Strategy Pattern.
 * Delegates exception handling to appropriate strategy implementations.
 * Simplifies handler logic and makes it easy to extend with new strategies.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final List<ExceptionHandlingStrategy> strategies;

    public GlobalExceptionHandler() {
        // Order matters: more specific exceptions first
        this.strategies = Arrays.asList(
                new BeautyStockExceptionStrategy(),
                new SecurityExceptionStrategy(),
                new IllegalArgumentExceptionStrategy()
                // GenericExceptionStrategy is fallback (not in list)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex, WebRequest request) {
        log.debug("Handling exception: {}", ex.getClass().getName());

        // Try each strategy in order
        for (ExceptionHandlingStrategy strategy : strategies) {
            ResponseEntity<?> response = strategy.handle(ex, request);
            if (response != null) {
                log.debug("Exception handled by strategy: {}", strategy.getClass().getName());
                return response;
            }
        }

        // Fallback to generic handler
        log.error("Unhandled exception:", ex);
        return new GenericExceptionStrategy().handle(ex, request);
    }
}
