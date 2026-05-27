package com.beautystock.repository

import android.content.Context
import com.beautystock.model.*
import com.beautystock.network.ApiService
import com.beautystock.network.AuthTokenManager
import com.beautystock.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class BeautyStockRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    private fun service(): ApiService = RetrofitClient.getApiService()

    // ================ Authentication ================

    suspend fun login(loginDTO: LoginDTO): Result<AuthResponseDTO> = try {
        val response = service().login(loginDTO)
        if (response.isSuccessful) {
            val auth = response.body()!!
            AuthTokenManager.saveToken(context, auth.token)
            AuthTokenManager.saveRefreshToken(context, auth.refreshToken)
            AuthTokenManager.saveUserInfo(context, auth.user.id.toString(), auth.user.email, auth.user.role)
            Result.success(auth)
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception(extractErrorMessage(errorBody, "Login failed: ${response.code()}")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun register(registerDTO: RegisterDTO): Result<AuthResponseDTO> = try {
        val response = service().register(registerDTO)
        if (response.isSuccessful) {
            val auth = response.body()!!
            AuthTokenManager.saveToken(context, auth.token)
            AuthTokenManager.saveRefreshToken(context, auth.refreshToken)
            AuthTokenManager.saveUserInfo(context, auth.user.id.toString(), auth.user.email, auth.user.role)
            Result.success(auth)
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception(extractErrorMessage(errorBody, "Registration failed: ${response.code()}")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun googleAuth(idToken: String, ageRange: String): Result<AuthResponseDTO> = try {
        val response = service().googleAuth(GoogleAuthRequestDTO(idToken, ageRange))
        if (response.isSuccessful) {
            val auth = response.body()!!
            AuthTokenManager.saveToken(context, auth.token)
            AuthTokenManager.saveRefreshToken(context, auth.refreshToken)
            AuthTokenManager.saveUserInfo(context, auth.user.id.toString(), auth.user.email, auth.user.role)
            Result.success(auth)
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception(extractErrorMessage(errorBody, "Google sign-in failed: ${response.code()}")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun logout(): Result<Unit> = try {
        service().logout()
        AuthTokenManager.clearAllTokens(context)
        Result.success(Unit)
    } catch (e: Exception) {
        AuthTokenManager.clearAllTokens(context)
        Result.success(Unit)
    }

    suspend fun getMe(): Result<UserProfileDTO> = try {
        val response = service().getMe()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get profile failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateRole(role: String): Result<UserProfileDTO> = try {
        val response = service().updateRole(RoleUpdateDTO(role))
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), "Update role failed")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ================ Products ================

    suspend fun getProducts(): Result<List<ProductDTO>> = try {
        val response = service().getProducts()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get products failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getProductById(id: Long): Result<ProductDTO> = try {
        val response = service().getProductById(id)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get product failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createProduct(createProductDTO: CreateProductDTO): Result<ProductDTO> = try {
        val response = service().createProduct(createProductDTO)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), "Create product failed: ${response.code()}")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProduct(id: Long, createProductDTO: CreateProductDTO): Result<ProductDTO> = try {
        val response = service().updateProduct(id, createProductDTO)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), "Update product failed: ${response.code()}")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteProduct(id: Long): Result<Unit> = try {
        val response = service().deleteProduct(id)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Delete product failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadProductImage(productId: Long, imageFile: File): Result<ProductDTO> = try {
        val requestBody = imageFile.asRequestBody("image/*".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
        val response = service().uploadProductImage(productId, multipartBody)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Upload image failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDashboard(): Result<DashboardDTO> = try {
        val response = service().getDashboard()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get dashboard failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchProducts(query: String): Result<List<ProductDTO>> = try {
        val response = service().searchProducts(query)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Search failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getExpiringProducts(): Result<List<ProductDTO>> = try {
        val response = service().getExpiringProducts()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get expiring products failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getProductsByCategory(category: String): Result<List<ProductDTO>> = try {
        val response = service().getProductsByCategory(category)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get products by category failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ================ Favorites ================

    suspend fun getFavorites(): Result<List<ProductDTO>> = try {
        val response = service().getFavorites()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get favorites failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addFavorite(productId: Long): Result<Unit> = try {
        val response = service().addFavorite(productId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Add favorite failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun removeFavorite(productId: Long): Result<Unit> = try {
        val response = service().removeFavorite(productId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Remove favorite failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun isFavorite(productId: Long): Result<Boolean> = try {
        val response = service().isFavorite(productId)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it.isFavorite) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Check favorite failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ================ Profile ================

    suspend fun getProfile(): Result<UserProfileDTO> = try {
        val response = service().getProfile()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception(extractErrorMessage(response.errorBody()?.string(), "Get profile failed")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProfile(profileUpdate: ProfileUpdateDTO): Result<UserProfileDTO> = try {
        val response = service().updateProfile(profileUpdate)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Update profile failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateLocation(city: String): Result<MessageResponse> = try {
        val response = service().updateLocation(LocationUpdateDTO(city))
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Update location failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadProfileImage(imageFile: File): Result<UserProfileDTO> = try {
        val requestBody = imageFile.asRequestBody("image/*".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)
        val response = service().uploadProfileImage(multipartBody)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Upload profile image failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getNotificationSettings(): Result<NotificationSettingsDTO> = try {
        val response = service().getNotificationSettings()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get notification settings failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateNotifications(enabled: Boolean): Result<NotificationSettingsDTO> = try {
        val response = service().updateNotifications(NotificationUpdateDTO(enabled))
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Update notifications failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ================ Recommendations ================

    suspend fun getYouthWeatherAdvice(): Result<WeatherResponse> = try {
        val response = service().getYouthWeatherAdvice()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get weather advice failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAdultWeatherAdvice(): Result<WeatherResponse> = try {
        val response = service().getAdultWeatherAdvice()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get weather advice failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getWeatherByCity(city: String, role: String): Result<WeatherResponse> = try {
        val response = service().getWeatherByCity(city, role)
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) }
                ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("Get weather failed: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun extractErrorMessage(errorBody: String?, fallback: String): String {
        if (errorBody.isNullOrBlank()) return fallback
        return runCatching {
            val json = org.json.JSONObject(errorBody)
            json.optString("message", fallback).takeIf { it.isNotBlank() } ?: fallback
        }.getOrElse {
            errorBody.takeIf { it.isNotBlank() } ?: fallback
        }
    }
}
