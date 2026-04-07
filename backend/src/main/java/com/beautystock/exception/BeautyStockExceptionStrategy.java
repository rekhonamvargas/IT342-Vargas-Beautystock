package com.beautystock.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Strategy for handling BeautyStock custom exceptions.
 */
public class BeautyStockExceptionStrategy implements ExceptionHandlingStrategy {
    @Override
    public ResponseEntity<?> handle(Exception ex, WebRequest request) {
        if (!(ex instanceof BeautyStockException)) {
            return null;
        }

        BeautyStockException bsEx = (BeautyStockException) ex;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", bsEx.getHttpStatus().value());
        body.put("error", bsEx.getHttpStatus().getReasonPhrase());
        body.put("code", bsEx.getErrorCode());
        body.put("message", bsEx.getMessage());

        return new ResponseEntity<>(body, bsEx.getHttpStatus());
    }
}
