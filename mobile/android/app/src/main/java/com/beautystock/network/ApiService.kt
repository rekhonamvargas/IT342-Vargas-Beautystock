package com.beautystock.network

import com.beautystock.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ================ Authentication Endpoints ================
    
    @POST("v1/auth/register")
    suspend fun register(@Body registerDTO: RegisterDTO): Response<AuthResponseDTO>
    
    @POST("v1/auth/login")
    suspend fun login(@Body loginDTO: LoginDTO): Response<AuthResponseDTO>
    
    @POST("v1/auth/google")
    suspend fun googleAuth(@Body googleAuthRequest: GoogleAuthRequestDTO): Response<AuthResponseDTO>
    
    @POST("v1/auth/logout")
    suspend fun logout(): Response<Unit>
    
    @GET("v1/auth/me")
    suspend fun getMe(): Response<UserProfileDTO>
    
    @PATCH("v1/auth/me/role")
    suspend fun updateRole(@Body roleUpdate: RoleUpdateDTO): Response<UserProfileDTO>
    
    // ================ Product Endpoints ================
    
    @GET("products")
    suspend fun getProducts(): Response<List<ProductDTO>>
    
    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductDTO>
    
    @POST("products")
    suspend fun createProduct(@Body createProductDTO: CreateProductDTO): Response<ProductDTO>
    
    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body createProductDTO: CreateProductDTO): Response<ProductDTO>
    
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Unit>
    
    @Multipart
    @POST("products/{id}/upload-image")
    suspend fun uploadProductImage(
        @Path("id") productId: Long,
        @Part file: MultipartBody.Part
    ): Response<ProductDTO>
    
    @GET("products/dashboard")
    suspend fun getDashboard(): Response<DashboardDTO>
    
    @GET("products/search")
    suspend fun searchProducts(@Query("query") query: String): Response<List<ProductDTO>>
    
    @GET("products/expiring")
    suspend fun getExpiringProducts(): Response<List<ProductDTO>>
    
    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<ProductDTO>>
    
    // ================ Favorite Endpoints ================

    @GET("favorites")
    suspend fun getFavorites(): Response<List<ProductDTO>>

    @POST("favorites/{productId}")
    suspend fun addFavorite(@Path("productId") productId: Long): Response<Unit>

    @DELETE("favorites/{productId}")
    suspend fun removeFavorite(@Path("productId") productId: Long): Response<Unit>

    @GET("favorites/{productId}/is-favorite")
    suspend fun isFavorite(@Path("productId") productId: Long): Response<FavoriteResponse>
    
    // ================ Profile/User Endpoints ================
    
    @GET("profile")
    suspend fun getProfile(): Response<UserProfileDTO>
    
    @PUT("profile")
    suspend fun updateProfile(@Body profileUpdate: ProfileUpdateDTO): Response<UserProfileDTO>
    
    @GET("users/me")
    suspend fun getUserMe(): Response<UserProfileDTO>
    
    @PUT("users/me/profile")
    suspend fun updateUserProfile(@Body profileUpdate: ProfileUpdateDTO): Response<UserProfileDTO>
    
    @PUT("users/me/location")
    suspend fun updateLocation(@Body locationUpdate: LocationUpdateDTO): Response<MessageResponse>
    
    @Multipart
    @POST("users/me/profile-image")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): Response<UserProfileDTO>
    
    @GET("users/me/notifications")
    suspend fun getNotificationSettings(): Response<NotificationSettingsDTO>
    
    @PUT("users/me/notifications")
    suspend fun updateNotifications(@Body notificationUpdate: NotificationUpdateDTO): Response<NotificationSettingsDTO>
    
    @POST("users/me/test-notification")
    suspend fun sendTestNotification(): Response<MessageResponse>
    
    // ================ File Upload Endpoints ================
    
    @GET("uploads/profiles/{filename}")
    suspend fun getProfileImage(@Path("filename") filename: String): Response<Unit>
    
    // ================ Recommendation Endpoints ================

    @GET("recommendations/youth/weather")
    suspend fun getYouthWeatherAdvice(): Response<WeatherResponse>

    @GET("recommendations/adult/weather")
    suspend fun getAdultWeatherAdvice(): Response<WeatherResponse>

    @GET("recommendations/weather")
    suspend fun getWeatherByCity(
        @Query("city") city: String,
        @Query("role") role: String = "ROLE_USER"
    ): Response<WeatherResponse>
}
