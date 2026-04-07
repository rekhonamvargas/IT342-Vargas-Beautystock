// web/src/services/apiClients/RecommendationApiClient.ts
// Factory Pattern: Recommendation endpoint organization

import axios, { AxiosInstance } from 'axios'

export interface RecommendationApiClient {
  getYouthWeather(): Promise<any>
  getAdultWeather(): Promise<any>
}

export function createRecommendationApiClient(apiClient: AxiosInstance): RecommendationApiClient {
  return {
    getYouthWeather() {
      return apiClient.get('/recommendations/youth/weather')
    },

    getAdultWeather() {
      return apiClient.get('/recommendations/adult/weather')
    },
  }
}
