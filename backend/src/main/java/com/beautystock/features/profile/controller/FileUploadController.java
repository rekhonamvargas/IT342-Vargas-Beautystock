package com.beautystock.features.profile.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private static final String UPLOAD_DIR = "uploads";

    /** GET /uploads/profiles/{filename} - Download profile image (public endpoint) */
    @GetMapping("/profiles/{filename}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> getProfileImage(@PathVariable String filename) {
        try {
            // Security check: prevent directory traversal
            if (filename.contains("..") || filename.contains("/")) {
                return ResponseEntity.badRequest().build();
            }
            
            Path filepath = Paths.get(UPLOAD_DIR, "profiles", filename);
            
            if (!Files.exists(filepath)) {
                logger.warn("File not found: " + filepath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filepath);
            
            // Determine media type
            String contentType = Files.probeContentType(filepath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to retrieve file: " + filename, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
