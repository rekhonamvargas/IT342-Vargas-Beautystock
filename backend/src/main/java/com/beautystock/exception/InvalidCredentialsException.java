package com.beautystock.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when user authentication fails.
 */
public class InvalidCredentialsException extends BeautyStockException {
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "AUTH-001");
    }
}
