package com.beautystock.features.products.service;

import com.beautystock.features.products.dto.CreateProductDTO;
import com.beautystock.features.products.dto.ProductDTO;
import com.beautystock.features.products.entity.Product;
import com.beautystock.features.products.repository.ProductRepository;
import com.beautystock.features.favorites.entity.Favorite;
import com.beautystock.features.favorites.repository.FavoriteRepository;
import com.beautystock.features.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository,
                          FavoriteRepository favoriteRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAll() {
        String ownerEmail = getCurrentEmail();
        return productRepository.findByOwnerEmailOrderByCreatedAtDesc(ownerEmail)
                .stream()
            .map(p -> toDto(p, ownerEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        String ownerEmail = getCurrentEmail();
        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDto(product, ownerEmail);
    }

    @Transactional
    public ProductDTO create(CreateProductDTO request) {
        String ownerEmail = getCurrentEmail();
        validateRequest(request);

        Product product = new Product();
        applyRequest(product, request);
        product.setOwnerEmail(ownerEmail);
        product.setUserId(legacyUserId(ownerEmail));

        Product saved = productRepository.save(product);
        return toDto(saved, ownerEmail);
    }

    @Transactional
    public ProductDTO update(Long id, CreateProductDTO request) {
        String ownerEmail = getCurrentEmail();
        validateRequest(request);

        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        applyRequest(product, request);
        Product saved = productRepository.save(product);
        return toDto(saved, ownerEmail);
    }

    @Transactional
    public void delete(Long id) {
        String ownerEmail = getCurrentEmail();
        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        favoriteRepository.deleteByOwnerEmailAndProduct(ownerEmail, product);
        productRepository.delete(product);
    }

    @Transactional
    public ProductDTO uploadImage(Long id, MultipartFile file) {
        String ownerEmail = getCurrentEmail();
        logger.info("Uploading image for product {}: {}", id, file.getOriginalFilename());
        
        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        try {
            byte[] fileBytes = file.getBytes();
            logger.info("Image size: {} bytes", fileBytes.length);
            
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String base64 = Base64.getEncoder().encodeToString(fileBytes);
            String imageUrl = "data:" + contentType + ";base64," + base64;
            
            logger.info("Base64 image URL length: {} chars", imageUrl.length());
            product.setImageUrl(imageUrl);
        } catch (IOException e) {
            logger.error("Failed to process image", e);
            throw new RuntimeException("Failed to process image");
        }

        Product saved = productRepository.save(product);
        logger.info("Image uploaded successfully for product {}", id);
        return toDto(saved, ownerEmail);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getExpiring() {
        String ownerEmail = getCurrentEmail();
        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(15);
        return productRepository.findByOwnerEmailAndExpirationDateBetweenOrderByExpirationDateAsc(ownerEmail, today, future)
                .stream()
            .map(p -> toDto(p, ownerEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard() {
        String ownerEmail = getCurrentEmail();
        LocalDate today = LocalDate.now();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalProducts", productRepository.countByOwnerEmail(ownerEmail));
        out.put("expiringCount", getExpiring().size());
        out.put("runningOutCount", productRepository.countRunningLowByOwnerEmail(ownerEmail));
        out.put("expiredCount", productRepository.countByOwnerEmailAndExpirationDateBefore(ownerEmail, today));
        out.put("favoritesCount", favoriteRepository.countByOwnerEmail(ownerEmail));
        out.put("totalSpent", productRepository.sumPriceByOwner(ownerEmail));
        return out;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> search(String query) {
        String ownerEmail = getCurrentEmail();
        return productRepository.search(ownerEmail, query == null ? "" : query.trim())
                .stream()
            .map(p -> toDto(p, ownerEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> filterByCategory(String category) {
        String ownerEmail = getCurrentEmail();
        return productRepository.findByOwnerEmailAndCategoryIgnoreCaseOrderByCreatedAtDesc(ownerEmail, category)
                .stream()
            .map(p -> toDto(p, ownerEmail))
                .toList();
    }

    @Transactional
    public void addFavorite(Long productId) {
        String ownerEmail = getCurrentEmail();
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!favoriteRepository.existsByOwnerEmailAndProduct(ownerEmail, product)) {
            var user = userRepository.findByEmail(ownerEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Favorite favorite = new Favorite();
            favorite.setOwnerEmail(ownerEmail);
            favorite.setProduct(product);
            favorite.setUserId(user.getId());
            favoriteRepository.save(favorite);
        }
    }

    @Transactional
    public void removeFavorite(Long productId) {
        String ownerEmail = getCurrentEmail();
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        favoriteRepository.deleteByOwnerEmailAndProduct(ownerEmail, product);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long productId) {
        String ownerEmail = getCurrentEmail();
        Product product = productRepository.findByIdAndOwnerEmail(productId, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return favoriteRepository.existsByOwnerEmailAndProduct(ownerEmail, product);
    }

    private void validateRequest(CreateProductDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getBrand() == null || request.getBrand().isBlank()) {
            throw new IllegalArgumentException("Brand is required");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("Price must be 0 or greater");
        }
    }

    private void applyRequest(Product product, CreateProductDTO request) {
        product.setName(request.getName().trim());
        product.setBrand(request.getBrand().trim());
        product.setCategory(request.getCategory().trim());
        product.setDescription(blankToNull(request.getDescription()));
        product.setPrice(request.getPrice());
        product.setPurchaseLocation(blankToNull(request.getPurchaseLocation()));
        product.setExpirationDate(request.getExpirationDate());
        product.setOpenedDate(request.getOpenedDate());
        product.setStatus(blankToNull(request.getStatus()));
    }

    private ProductDTO toDto(Product product, String ownerEmail) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setPurchaseLocation(product.getPurchaseLocation());
        dto.setImageUrl(product.getImageUrl());
        dto.setExpirationDate(product.getExpirationDate());
        dto.setOpenedDate(product.getOpenedDate());
        dto.setStatus(product.getStatus());

        LocalDate today = LocalDate.now();
        boolean expired = product.getExpirationDate() != null && product.getExpirationDate().isBefore(today);
        boolean expiringSoon = product.getExpirationDate() != null
                && !expired
                && !product.getExpirationDate().isAfter(today.plusDays(15));

        dto.setIsExpired(expired);
        dto.setIsExpiringWithin15Days(expiringSoon);
        dto.setIsFavorite(favoriteRepository.existsByOwnerEmailAndProduct(ownerEmail, product));
        return dto;
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private long legacyUserId(String ownerEmail) {
        return Math.abs((long) ownerEmail.hashCode());
    }
}
