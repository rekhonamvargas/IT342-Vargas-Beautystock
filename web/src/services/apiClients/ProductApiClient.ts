// web/src/services/apiClients/ProductApiClient.ts
// Factory Pattern: Product endpoint organization

import axios, { AxiosInstance } from 'axios'

export interface ProductApiClient {
  getAll(): Promise<any>
  getById(id: number): Promise<any>
  create(data: any): Promise<any>
  update(id: number, data: any): Promise<any>
  delete(id: number): Promise<void>
  uploadImage(id: number, file: File): Promise<any>
  getExpiring(): Promise<any>
  getDashboard(): Promise<any>
  search(query: string): Promise<any>
  filterByCategory(category: string): Promise<any>
}

export function createProductApiClient(apiClient: AxiosInstance): ProductApiClient {
  return {
    getAll() {
      return apiClient.get('/products')
    },

    getById(id: number) {
      return apiClient.get(`/products/${id}`)
    },

    create(data: any) {
      return apiClient.post('/products', data)
    },

    update(id: number, data: any) {
      return apiClient.put(`/products/${id}`, data)
    },

    delete(id: number) {
      return apiClient.delete(`/products/${id}`)
    },

    uploadImage(id: number, file: File) {
      const formData = new FormData()
      formData.append('file', file)
      return apiClient.post(`/products/${id}/upload-image`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
    },

    getExpiring() {
      return apiClient.get('/products/expiring')
    },

    getDashboard() {
      return apiClient.get('/products/dashboard')
    },

    search(query: string) {
      return apiClient.get(`/products/search?query=${query}`)
    },

    filterByCategory(category: string) {
      return apiClient.get(`/products/category/${category}`)
    },
  }
}
