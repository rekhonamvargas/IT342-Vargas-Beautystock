// web/src/services/ApiClientFactory.ts
// Factory Pattern: Creates and organizes all API clients

import axios, { AxiosInstance } from 'axios'
import { createAuthApiClient, type AuthApiClient } from './apiClients/AuthApiClient'
import { createProductApiClient, type ProductApiClient } from './apiClients/ProductApiClient'
import { createFavoriteApiClient, type FavoriteApiClient } from './apiClients/FavoriteApiClient'
import { createRecommendationApiClient, type RecommendationApiClient } from './apiClients/RecommendationApiClient'

const API_BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL || '/api'

/**
 * Factory for creating organized API clients.
 * Centralizes HTTP setup and provides domain-specific clients.
 * Replaces the large flat api.ts file.
 */
export class ApiClientFactory {
  private apiClient: AxiosInstance
  private authClient: AuthApiClient
  private productClient: ProductApiClient
  private favoriteClient: FavoriteApiClient
  private recommendationClient: RecommendationApiClient

  constructor() {
    this.apiClient = this.createHttpClient()
    this.authClient = createAuthApiClient(this.apiClient)
    this.productClient = createProductApiClient(this.apiClient)
    this.favoriteClient = createFavoriteApiClient(this.apiClient)
    this.recommendationClient = createRecommendationApiClient(this.apiClient)
  }

  /**
   * Configure HTTP client with interceptors and defaults.
   */
  private createHttpClient(): AxiosInstance {
    const client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    // Request interceptor: Add JWT token
    client.interceptors.request.use((config) => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })

    // Response interceptor: Handle 401/403 errors
    client.interceptors.response.use(
      (response) => response,
      (error) => {
        console.error('API Error:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data,
          message: error.message,
          url: error.config?.url,
        })

        if ((error.response?.status === 401 || error.response?.status === 403)
          && error.config?.url !== '/v1/auth/login'
          && error.config?.url !== '/v1/auth/register') {
          localStorage.removeItem('token')
          // TODO: Redirect to login if needed
        }

        return Promise.reject(error)
      }
    )

    return client
  }

  // Singleton getters for each client
  public getAuthClient(): AuthApiClient {
    return this.authClient
  }

  public getProductClient(): ProductApiClient {
    return this.productClient
  }

  public getFavoriteClient(): FavoriteApiClient {
    return this.favoriteClient
  }

  public getRecommendationClient(): RecommendationApiClient {
    return this.recommendationClient
  }
}

// Create singleton instance
export const apiFactory = new ApiClientFactory()

// Export convenience functions (backward compatibility)
export const authApi = apiFactory.getAuthClient()
export const productApi = apiFactory.getProductClient()
export const favoriteApi = apiFactory.getFavoriteClient()
export const recommendationApi = apiFactory.getRecommendationClient()
