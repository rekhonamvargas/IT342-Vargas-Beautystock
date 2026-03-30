package com.beautystock.data.api

import com.beautystock.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("v1/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("v1/auth/me")
    suspend fun getMe(): Response<UserDTO>

    // Products
    @GET("products")
    suspend fun getProducts(): Response<List<ProductDTO>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Long): Response<ProductDTO>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ProductDTO>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Unit>

    @Multipart
    @POST("products/{id}/upload-image")
    suspend fun uploadImage(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Response<ProductDTO>

    @GET("products/dashboard")
    suspend fun getDashboard(): Response<DashboardDTO>

    @GET("products/search")
    suspend fun searchProducts(@Query("query") query: String): Response<List<ProductDTO>>

    @GET("products/category/{category}")
    suspend fun filterByCategory(@Path("category") category: String): Response<List<ProductDTO>>

    // Favorites
    @POST("favorites/{productId}")
    suspend fun addFavorite(@Path("productId") productId: Long): Response<Unit>

    @DELETE("favorites/{productId}")
    suspend fun removeFavorite(@Path("productId") productId: Long): Response<Unit>

    // Weather / Recommendations
    @GET("recommendations/youth/weather")
    suspend fun getYouthWeather(): Response<WeatherResponse>

    @GET("recommendations/adult/weather")
    suspend fun getAdultWeather(): Response<WeatherResponse>

    // User
    @PUT("users/me/location")
    suspend fun updateLocation(@Body request: LocationRequest): Response<Unit>
}
