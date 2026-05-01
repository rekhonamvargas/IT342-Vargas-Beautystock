package com.beautystock.features.favorites.repository;

import com.beautystock.features.favorites.entity.Favorite;
import com.beautystock.features.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByOwnerEmailAndProduct(String ownerEmail, Product product);
    void deleteByOwnerEmailAndProduct(String ownerEmail, Product product);
    long countByOwnerEmail(String ownerEmail);
    Optional<Favorite> findByOwnerEmailAndProduct(String ownerEmail, Product product);
}
