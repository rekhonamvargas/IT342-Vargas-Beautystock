package com.beautystock.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for BeautyStock application.
 * Implements Strategy Pattern foundation for exception handling.
 */
public abstract class BeautyStockException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String errorCode;

    public BeautyStockException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public BeautyStockException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
