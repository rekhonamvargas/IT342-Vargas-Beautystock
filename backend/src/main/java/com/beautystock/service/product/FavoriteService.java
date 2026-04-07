package com.beautystock.service.product;

import com.beautystock.entity.Favorite;
import com.beautystock.entity.Product;
import com.beautystock.exception.ProductNotFoundException;
import com.beautystock.repository.FavoriteRepository;
import com.beautystock.repository.ProductRepository;
import com.beautystock.service.auth.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing product favorites.
 * Single Responsibility: Handle all favorite-related operations.
 * Extracted from ProductService to improve maintainability.
 */
@Service
public class FavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public FavoriteService(FavoriteRepository favoriteRepository,
                          ProductRepository productRepository,
                          CurrentUserProvider currentUserProvider) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public void addFavorite(Long productId) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Prevent duplicates
        if (favoriteRepository.existsByOwnerEmailAndProduct(ownerEmail, product)) {
            logger.debug("Product {} already favorited by {}", productId, ownerEmail);
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setOwnerEmail(ownerEmail);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
        
        logger.info("Product {} added to favorites by {}", productId, ownerEmail);
    }

    @Transactional
    public void removeFavorite(Long productId) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        favoriteRepository.deleteByOwnerEmailAndProduct(ownerEmail, product);
        
        logger.info("Product {} removed from favorites by {}", productId, ownerEmail);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long productId) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return favoriteRepository.existsByOwnerEmailAndProduct(ownerEmail, product);
    }

    @Transactional(readOnly = true)
    public long getFavoriteCount(String ownerEmail) {
        return favoriteRepository.countByOwnerEmail(ownerEmail);
    }
}
