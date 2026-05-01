package com.beautystock.features.favorites.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.beautystock.features.products.entity.Product;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_email", "product_id"})
        }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
}
