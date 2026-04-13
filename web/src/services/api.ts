import axios from 'axios'

const API_BASE_URL = (import.meta as any).env?.VITE_API_BASE_URL || '/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add token to requests if available
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken') || localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Add error interceptor: clear stale token on auth failure and redirect
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      message: error.message,
      url: error.config?.url,
    })

    // If we get 401 or 403 on an authenticated request, token is invalid
    const status = error.response?.status
    if ((status === 401 || status === 403)
      && error.config?.url !== '/v1/auth/login'
      && error.config?.url !== '/v1/auth/register') {
      localStorage.removeItem('token')
      localStorage.removeItem('authToken')
    }

    return Promise.reject(error)
  }
)

export const authApi = {
  register: (data: any) => apiClient.post('/v1/auth/register', data),
  login: (data: any) => apiClient.post('/v1/auth/login', data),
  googleAuth: (idToken: string, ageRange?: string) => apiClient.post('/v1/auth/google', { idToken, ageRange }),
  logout: () => apiClient.post('/v1/auth/logout'),
  getMe: () => apiClient.get('/v1/auth/me'),
}

export const userApi = {
  updateLocation: (city: string) => apiClient.put('/users/me/location', { city }),
}

export const productApi = {
  getAll: () => apiClient.get('/products'),
  getById: (id: number) => apiClient.get(`/products/${id}`),
  create: (data: any) => apiClient.post('/products', data),
  update: (id: number, data: any) => apiClient.put(`/products/${id}`, data),
  delete: (id: number) => apiClient.delete(`/products/${id}`),
  uploadImage: (id: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post(`/products/${id}/upload-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  getExpiring: () => apiClient.get('/products/expiring'),
  getDashboard: () => apiClient.get('/products/dashboard'),
  search: (query: string) => apiClient.get(`/products/search?query=${query}`),
  filterByCategory: (category: string) => apiClient.get(`/products/category/${category}`),
}

export const favoriteApi = {
  add: (productId: number) => apiClient.post(`/favorites/${productId}`),
  remove: (productId: number) => apiClient.delete(`/favorites/${productId}`),
  isFavorite: (productId: number) => apiClient.get(`/favorites/${productId}/is-favorite`),
}

export const recommendationApi = {
  getYouthWeather: () => apiClient.get('/recommendations/youth/weather'),
  getAdultWeather: () => apiClient.get('/recommendations/adult/weather'),
}
