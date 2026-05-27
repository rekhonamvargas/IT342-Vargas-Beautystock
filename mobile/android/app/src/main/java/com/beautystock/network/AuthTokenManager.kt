package com.beautystock.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_store")

object AuthTokenManager {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_ROLE_KEY = stringPreferencesKey("user_role")

    // In-memory cache to avoid blocking DataStore reads on every API call
    @Volatile var cachedToken: String? = null
        private set

    fun getToken(context: Context): Flow<String?> =
        context.authDataStore.data.map { it[TOKEN_KEY] }

    fun getRefreshToken(context: Context): Flow<String?> =
        context.authDataStore.data.map { it[REFRESH_TOKEN_KEY] }

    fun getUserId(context: Context): Flow<String?> =
        context.authDataStore.data.map { it[USER_ID_KEY] }

    fun getUserEmail(context: Context): Flow<String?> =
        context.authDataStore.data.map { it[USER_EMAIL_KEY] }

    fun getUserRole(context: Context): Flow<String?> =
        context.authDataStore.data.map { it[USER_ROLE_KEY] }

    suspend fun saveToken(context: Context, token: String) {
        cachedToken = token
        context.authDataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveRefreshToken(context: Context, refreshToken: String) {
        context.authDataStore.edit { it[REFRESH_TOKEN_KEY] = refreshToken }
    }

    suspend fun saveUserInfo(context: Context, userId: String, email: String, role: String) {
        context.authDataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun clearAllTokens(context: Context) {
        cachedToken = null
        context.authDataStore.edit { it.clear() }
    }

    suspend fun isTokenAvailable(context: Context): Boolean {
        if (cachedToken != null) return true
        val token = context.authDataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
        cachedToken = token
        return !token.isNullOrBlank()
    }

    /** Call once on startup to warm the in-memory token cache. */
    suspend fun initialize(context: Context) {
        if (cachedToken == null) {
            cachedToken = context.authDataStore.data.map { it[TOKEN_KEY] }.firstOrNull()
        }
    }
}
