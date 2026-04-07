package com.beautystock.service.product;

import com.beautystock.dto.CreateProductDTO;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Facade Pattern implementation for Product operations.
 * Coordinates between multiple specialized services.
 * Provides simplified, cohesive interface to controllers.
 * 
 * Instead of ProductService handling 15+ methods,
 * we now have focused services delegating through this facade:
 * - ProductRepository: CRUD persistence
 * - FavoriteService: Favorite operations
 * - DashboardService: Statistics aggregation
 * - ProductImageService: Image handling
 */
@Service
public class ProductFacade {
    private static final Logger logger = LoggerFactory.getLogger(ProductFacade.class);

    private final ProductRepository productRepository;
    private final FavoriteService favoriteService;
    private final DashboardService dashboardService;
    private final ProductImageService productImageService;
    private final ProductMapper productMapper;
    private final CurrentUserProvider currentUserProvider;

    public ProductFacade(ProductRepository productRepository,
                        FavoriteService favoriteService,
                        DashboardService dashboardService,
                        ProductImageService productImageService,
                        ProductMapper productMapper,
                        CurrentUserProvider currentUserProvider) {
        this.productRepository = productRepository;
        this.favoriteService = favoriteService;
        this.dashboardService = dashboardService;
        this.productImageService = productImageService;
        this.productMapper = productMapper;
        this.currentUserProvider = currentUserProvider;
    }

    // ─── CRUD Operations ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductDTO> getAll() {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        return productRepository.findByOwnerEmailOrderByCreatedAtDesc(ownerEmail)
                .stream()
                .map(p -> productMapper.toDto(p, ownerEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(Long id) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toDto(product, ownerEmail);
    }

    @Transactional
    public ProductDTO create(CreateProductDTO request) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        validateCreateRequest(request);

        Product product = new Product();
        applyRequest(product, request);
        product.setOwnerEmail(ownerEmail);
        product.setUserId(legacyUserId(ownerEmail));

        Product saved = productRepository.save(product);
        logger.info("Product created: {} for user {}", saved.getId(), ownerEmail);
        
        return productMapper.toDto(saved, ownerEmail);
    }

    @Transactional
    public ProductDTO update(Long id, CreateProductDTO request) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        validateCreateRequest(request);

        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(id));

        applyRequest(product, request);
        Product saved = productRepository.save(product);
        logger.info("Product updated: {}", id);
        
        return productMapper.toDto(saved, ownerEmail);
    }

    @Transactional
    public void delete(Long id) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        Product product = productRepository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        productRepository.delete(product);
        logger.info("Product deleted: {}", id);
    }

    // ─── Image Operations (delegates to service) ───────────────────────

    @Transactional
    public ProductDTO uploadImage(Long id, MultipartFile file) {
        return productImageService.uploadImage(id, file);
    }

    // ─── Favorite Operations (delegates to service) ────────────────────

    @Transactional
    public void addFavorite(Long productId) {
        favoriteService.addFavorite(productId);
    }

    @Transactional
    public void removeFavorite(Long productId) {
        favoriteService.removeFavorite(productId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long productId) {
        return favoriteService.isFavorite(productId);
    }

    // ─── Dashboard Operations (delegates to service) ────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard() {
        return dashboardService.getDashboardStats();
    }

    // ─── Query Operations ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProductDTO> getExpiring() {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(15);
        
        return productRepository.findByOwnerEmailAndExpirationDateBetweenOrderByExpirationDateAsc(
                ownerEmail, today, future)
                .stream()
                .map(p -> productMapper.toDto(p, ownerEmail))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> search(String query) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        // Implement search in ProductRepository if needed
        return List.of(); // TODO: Implement search
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> filterByCategory(String category) {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        // Implement category filter in ProductRepository if needed
        return List.of(); // TODO: Implement filter
    }

    // ─── Helper Methods ────────────────────────────────────────────────

    private void validateCreateRequest(CreateProductDTO request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getBrand() == null || request.getBrand().trim().isEmpty()) {
            throw new IllegalArgumentException("Product brand is required");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Product category is required");
        }
    }

    private void applyRequest(Product product, CreateProductDTO request) {
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setExpirationDate(request.getExpirationDate());
    }

    private Long legacyUserId(String ownerEmail) {
        // Legacy support; can be removed if user management is refactored
        return 0L;
    }
}
