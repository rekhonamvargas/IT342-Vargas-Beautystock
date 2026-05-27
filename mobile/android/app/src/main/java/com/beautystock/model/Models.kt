package com.beautystock.model

import com.google.gson.annotations.SerializedName

// Response Models

data class AuthResponseDTO(
    @SerializedName("token")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("user")
    val user: UserProfileDTO,
    @SerializedName("isNewUser")
    val isNewUser: Boolean = false
)

data class UserProfileDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null,
    @SerializedName("role")
    val role: String,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("ageRange")
    val ageRange: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class ProductDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("brand")
    val brand: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("purchaseLocation")
    val purchaseLocation: String? = null,
    @SerializedName("expirationDate")
    val expirationDate: String? = null,
    @SerializedName("openedDate")
    val openedDate: String? = null,
    @SerializedName("status")
    val status: String? = "AVAILABLE",
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("isFavorite")
    val isFavorite: Boolean? = false,
    @SerializedName("isExpired")
    val isExpired: Boolean? = false,
    @SerializedName("isExpiringWithin15Days")
    val isExpiringWithin15Days: Boolean? = false
)

data class DashboardDTO(
    @SerializedName("totalProducts")
    val totalProducts: Int = 0,
    @SerializedName("expiringCount")
    val expiringProducts: Int = 0,
    @SerializedName("runningOutCount")
    val runningOutProducts: Int = 0,
    @SerializedName("expiredCount")
    val expiredProducts: Int = 0,
    @SerializedName("favoritesCount")
    val favoritesCount: Int = 0,
    @SerializedName("totalSpent")
    val totalSpent: Double? = null
)

data class WeatherResponse(
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("temperature")
    val temperature: Double? = null,
    @SerializedName("humidity")
    val humidity: Int? = null,
    @SerializedName("condition")
    val condition: String? = null,
    @SerializedName("advice")
    val advice: String? = null
)

data class FavoriteResponse(
    @SerializedName("isFavorite")
    val isFavorite: Boolean
)

data class MessageResponse(
    @SerializedName("message")
    val message: String
)

data class NotificationSettingsDTO(
    @SerializedName("notificationEmail")
    val notificationEmail: String,
    @SerializedName("notificationsEnabled")
    val notificationsEnabled: Boolean,
    @SerializedName("isGoogleConnected")
    val isGoogleConnected: Boolean
)

// Request Models

data class RegisterDTO(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("confirmPassword")
    val confirmPassword: String,
    @SerializedName("ageRange")
    val ageRange: String
)

data class LoginDTO(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class GoogleAuthRequestDTO(
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("ageRange")
    val ageRange: String
)

data class CreateProductDTO(
    @SerializedName("name")
    val name: String,
    @SerializedName("brand")
    val brand: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("price")
    val price: Double? = null,
    @SerializedName("purchaseLocation")
    val purchaseLocation: String? = null,
    @SerializedName("expirationDate")
    val expirationDate: String? = null,
    @SerializedName("openedDate")
    val openedDate: String? = null,
    @SerializedName("status")
    val status: String = "AVAILABLE"
)

data class ProfileUpdateDTO(
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null
)

data class LocationUpdateDTO(
    @SerializedName("city")
    val city: String
)

data class RoleUpdateDTO(
    @SerializedName("role")
    val role: String
)

data class NotificationUpdateDTO(
    @SerializedName("notificationsEnabled")
    val notificationsEnabled: Boolean
)

// Enums

enum class UserRole {
    ROLE_YOUTH,
    ROLE_ADMIN,
    ROLE_USER
}

enum class ProductCategory {
    SKINCARE,
    MAKEUP,
    FRAGRANCE,
    HAIRCARE,
    BODYCARE,
    OTHER
}

enum class ProductStatus {
    AVAILABLE,
    EXPIRING_SOON,
    RUNNING_OUT,
    EXPIRED
}

enum class AgeRange {
    YOUTH,
    ADULT
}
