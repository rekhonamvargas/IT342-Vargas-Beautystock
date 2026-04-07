package com.beautystock.service.product;

import org.springframework.web.multipart.MultipartFile;

/**
 * Strategy Pattern for image processing.
 * Allows different implementations (Base64, CloudStorage, FileSystem) without coupling ProductService.
 */
public interface ImageProcessingStrategy {
    /**
     * Process uploaded file and return processable URL/reference.
     * 
     * @param file Uploaded file
     * @return Processed image URL or reference
     */
    String processImage(MultipartFile file);

    /**
     * Clean up processed image (e.g., delete from cloud storage).
     * 
     * @param imageReference Image reference to clean
     */
    void deleteImage(String imageReference);
}
