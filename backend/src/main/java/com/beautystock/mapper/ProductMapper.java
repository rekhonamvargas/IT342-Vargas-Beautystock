package com.beautystock.mapper;

import com.beautystock.dto.ProductDTO;
import com.beautystock.entity.Product;
import com.beautystock.repository.FavoriteRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Concrete implementation of EntityMapper for Product → ProductDTO transformations.
 * Centralizes all product mapping logic.
 * Can be injected and reused across services.
 */
@Component
public class ProductMapper implements EntityMapper<Product, ProductDTO> {
    private final FavoriteRepository favoriteRepository;

    public ProductMapper(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * Convert Product entity to ProductDTO.
     * Includes favorite status based on owner email.
     */
    public ProductDTO toDto(Product product, String ownerEmail) {
        LocalDate today = LocalDate.now();
        boolean isExpired = product.getExpirationDate() != null && product.getExpirationDate().isBefore(today);
        boolean isExpiringWithin15Days = product.getExpirationDate() != null 
                && !isExpired 
                && product.getExpirationDate().isBefore(today.plusDays(15));
        
        boolean isFavorite = favoriteRepository.existsByOwnerEmailAndProduct(ownerEmail, product);

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setExpirationDate(product.getExpirationDate());
        dto.setIsExpired(isExpired);
        dto.setIsFavorite(isFavorite);
        dto.setIsExpiringWithin15Days(isExpiringWithin15Days);

        return dto;
    }

    @Override
    public ProductDTO toDto(Product entity) {
        if (entity == null) return null;
        
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBrand(entity.getBrand());
        dto.setCategory(entity.getCategory());
        dto.setPrice(entity.getPrice());
        dto.setImageUrl(entity.getImageUrl());
        dto.setExpirationDate(entity.getExpirationDate());
        
        return dto;
    }

    @Override
    public Product toEntity(ProductDTO dto) {
        if (dto == null) return null;
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());
        product.setExpirationDate(dto.getExpirationDate());
        
        return product;
    }
}
