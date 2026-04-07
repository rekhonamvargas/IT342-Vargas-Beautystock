package com.beautystock.service.product;

import com.beautystock.dto.ProductDTO;
import com.beautystock.entity.Product;
import com.beautystock.exception.ProductNotFoundException;
import com.beautystock.mapper.ProductMapper;
import com.beautystock.repository.ProductRepository;
import com.beautystock.service.auth.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for handling product image uploads and processing.
 * Single Responsibility: Manage image-related operations.
 * Extracted from ProductService using Strategy Pattern for image processing.
 */
@Service
public class ProductImageService {
    private static final Logger logger = LoggerFactory.getLogger(ProductImageService.class);

    private final ProductRepository productRepository;
    private final ImageProcessingStrategy imageProcessingStrategy;
    private final CurrentUserProvider currentUserProvider;
    private final ProductMapper productMapper;

    public ProductImageService(ProductRepository productRepository,
                              ImageProcessingStrategy imageProcessingStrategy,
                              CurrentUserProvider currentUserProvider,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.imageProcessingStrategy = imageProcessingStrategy;
        this.currentUserProvider = currentUserProvider;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDTO uploadImage(Long productId, MultipartFile file) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        logger.info("Uploading image for product {}: {}", productId, file.getOriginalFilename());
        
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Clean up old image if exists
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            imageProcessingStrategy.deleteImage(product.getImageUrl());
        }

        // Process and store new image
        String imageUrl = imageProcessingStrategy.processImage(file);
        product.setImageUrl(imageUrl);
        
        Product saved = productRepository.save(product);
        logger.info("Image uploaded successfully for product {}", productId);
        
        return productMapper.toDto(saved, ownerEmail);
    }
}
