package com.beautystock.service.product;

import com.beautystock.repository.FavoriteRepository;
import com.beautystock.repository.ProductRepository;
import com.beautystock.service.auth.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for aggregating dashboard statistics.
 * Single Responsibility: Calculate and provide dashboard metrics.
 * Extracted from ProductService to improve maintainability.
 */
@Service
public class DashboardService {
    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserProvider currentUserProvider;

    public DashboardService(ProductRepository productRepository,
                           FavoriteRepository favoriteRepository,
                           CurrentUserProvider currentUserProvider) {
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        String ownerEmail = currentUserProvider.getCurrentUserEmail();
        LocalDate today = LocalDate.now();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProducts", getTotalProductCount(ownerEmail));
        stats.put("expiringCount", getExpiringCount(ownerEmail, today));
        stats.put("runningOutCount", getRunningLowCount(ownerEmail));
        stats.put("expiredCount", getExpiredCount(ownerEmail, today));
        stats.put("favoritesCount", getFavoriteCount(ownerEmail));
        stats.put("totalSpent", getTotalSpent(ownerEmail));

        return stats;
    }

    private long getTotalProductCount(String ownerEmail) {
        return productRepository.countByOwnerEmail(ownerEmail);
    }

    private long getExpiringCount(String ownerEmail, LocalDate today) {
        LocalDate future = today.plusDays(15);
        return productRepository.countByOwnerEmailAndExpirationDateBetween(ownerEmail, today, future);
    }

    private long getRunningLowCount(String ownerEmail) {
        return productRepository.countRunningLowByOwnerEmail(ownerEmail);
    }

    private long getExpiredCount(String ownerEmail, LocalDate today) {
        return productRepository.countByOwnerEmailAndExpirationDateBefore(ownerEmail, today);
    }

    private long getFavoriteCount(String ownerEmail) {
        return favoriteRepository.countByOwnerEmail(ownerEmail);
    }

    private Double getTotalSpent(String ownerEmail) {
        return productRepository.sumPriceByOwner(ownerEmail);
    }
}
