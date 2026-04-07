package com.beautystock.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when there's a conflict (e.g., duplicate email).
 */
public class ConflictException extends BeautyStockException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT-409");
    }
}
