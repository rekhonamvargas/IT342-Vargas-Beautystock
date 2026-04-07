package com.beautystock.service.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * Base64 image processing strategy.
 * Converts images to Base64 data URLs for embedding in responses.
 * Good for small images; not ideal for large archives.
 */
@Component
public class Base64ImageProcessingStrategy implements ImageProcessingStrategy {
    private static final Logger logger = LoggerFactory.getLogger(Base64ImageProcessingStrategy.class);

    @Override
    public String processImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        try {
            byte[] fileBytes = file.getBytes();
            logger.debug("Processing Base64 image: {} bytes", fileBytes.length);
            
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String base64 = Base64.getEncoder().encodeToString(fileBytes);
            
            return "data:" + contentType + ";base64," + base64;
        } catch (IOException e) {
            logger.error("Failed to process Base64 image", e);
            throw new RuntimeException("Failed to process image: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String imageReference) {
        // Base64 images are embedded; no cleanup needed
        logger.debug("Base64 image cleanup (no-op)");
    }
}
