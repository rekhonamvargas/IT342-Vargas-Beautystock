package com.beautystock.features.products.dto;

import java.time.LocalDate;

public class ProductDTO {
    private Long id;
    private String name;
    private String brand;
    private String category;
    private String description;
    private Double price;
    private String purchaseLocation;
    private String imageUrl;
    private LocalDate expirationDate;
    private LocalDate openedDate;
    private Boolean isExpired;
    private Boolean isFavorite;
    private Boolean isExpiringWithin15Days;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getPurchaseLocation() { return purchaseLocation; }
    public void setPurchaseLocation(String purchaseLocation) { this.purchaseLocation = purchaseLocation; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public LocalDate getOpenedDate() { return openedDate; }
    public void setOpenedDate(LocalDate openedDate) { this.openedDate = openedDate; }
    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean expired) { isExpired = expired; }
    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean favorite) { isFavorite = favorite; }
    public Boolean getIsExpiringWithin15Days() { return isExpiringWithin15Days; }
    public void setIsExpiringWithin15Days(Boolean expiringWithin15Days) { isExpiringWithin15Days = expiringWithin15Days; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
