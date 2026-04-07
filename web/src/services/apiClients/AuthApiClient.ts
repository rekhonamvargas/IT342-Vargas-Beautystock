// web/src/services/apiClients/AuthApiClient.ts
// Factory Pattern: API endpoint organization by domain

import axios, { AxiosInstance } from 'axios'

export interface AuthApiClient {
  register(data: any): Promise<any>
  login(data: any): Promise<any>
  googleAuth(idToken: string, ageRange?: string): Promise<any>
  logout(): Promise<void>
  getCurrentUser(): Promise<any>
}

export function createAuthApiClient(apiClient: AxiosInstance): AuthApiClient {
  return {
    register(data: any) {
      return apiClient.post('/v1/auth/register', data)
    },

    login(data: any) {
      return apiClient.post('/v1/auth/login', data)
    },

    googleAuth(idToken: string, ageRange?: string) {
      return apiClient.post('/v1/auth/google', { idToken, ageRange })
    },

    logout() {
      return apiClient.post('/v1/auth/logout')
    },

    getCurrentUser() {
      return apiClient.get('/v1/auth/me')
    },
  }
}
