package com.beautystock.network

import android.content.Context
import com.beautystock.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var apiService: ApiService? = null
    private var context: Context? = null
    private var currentBaseUrl: String? = null

    fun initialize(appContext: Context) {
        context = appContext.applicationContext
    }

    fun getApiService(): ApiService {
        if (apiService == null) {
            apiService = createRetrofit().create(ApiService::class.java)
        }
        return apiService!!
    }

    fun updateBaseUrl(baseUrl: String) {
        val normalized = normalize(baseUrl)
        if (normalized != currentBaseUrl) {
            currentBaseUrl = normalized
            apiService = null
        }
    }

    private fun normalize(url: String): String {
        val t = url.trim()
        return if (t.endsWith("/")) t else "$t/"
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(currentBaseUrl ?: BuildConfig.BASE_URL.takeIf { it.isNotBlank() } ?: BackendUrlProvider.defaultBaseUrl())
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    private fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        val auth = Interceptor { chain ->
            val token = AuthTokenManager.cachedToken
            val request = if (!token.isNullOrBlank()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(auth)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createGson(): Gson = GsonBuilder().setLenient().create()
}
