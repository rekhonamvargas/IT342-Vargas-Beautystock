// web/src/services/apiClients/FavoriteApiClient.ts
// Factory Pattern: Favorite endpoint organization

import axios, { AxiosInstance } from 'axios'

export interface FavoriteApiClient {
  add(productId: number): Promise<void>
  remove(productId: number): Promise<void>
  isFavorite(productId: number): Promise<{ isFavorite: boolean }>
}

export function createFavoriteApiClient(apiClient: AxiosInstance): FavoriteApiClient {
  return {
    add(productId: number) {
      return apiClient.post(`/favorites/${productId}`)
    },

    remove(productId: number) {
      return apiClient.delete(`/favorites/${productId}`)
    },

    isFavorite(productId: number) {
      return apiClient.get(`/favorites/${productId}/is-favorite`)
    },
  }
}
