package com.beautystock.data.model

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val ageRange: String
)

data class AuthResponse(
    val token: String,
    val user: UserDTO
)

data class UserDTO(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String,
    val profileImageUrl: String? = null,
    val createdAt: String? = null,
    val city: String? = null
)

data class ProductDTO(
    val id: Long,
    val name: String,
    val brand: String,
    val category: String,
    val description: String?,
    val price: Double,
    val purchaseLocation: String?,
    val imageUrl: String?,
    val expirationDate: String?,
    val openedDate: String?,
    val isExpired: Boolean,
    val isFavorite: Boolean,
    val isExpiringWithin15Days: Boolean
)

data class CreateProductRequest(
    val name: String,
    val brand: String,
    val category: String,
    val description: String?,
    val price: Double,
    val purchaseLocation: String?,
    val expirationDate: String?,
    val openedDate: String?
)

data class DashboardDTO(
    val totalProducts: Int,
    val expiringCount: Int,
    val expiredCount: Int,
    val favoritesCount: Int,
    val totalSpent: Double
)

data class WeatherResponse(
    val city: String?,
    val temperature: Double?,
    val humidity: Int?,
    val advice: String?
)

data class LocationRequest(val city: String)
