package com.beautystock.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when input validation fails.
 */
public class ValidationException extends BeautyStockException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION-400");
    }
}
