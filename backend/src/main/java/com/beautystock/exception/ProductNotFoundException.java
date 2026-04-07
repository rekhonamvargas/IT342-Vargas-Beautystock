package com.beautystock.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a product is not found.
 */
public class ProductNotFoundException extends BeautyStockException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId, HttpStatus.NOT_FOUND, "PRODUCT-404");
    }

    public ProductNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "PRODUCT-404");
    }
}
