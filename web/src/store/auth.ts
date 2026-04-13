import { create } from 'zustand'

interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  role: string
  city: string | null
}

interface AuthStore {
  user: User | null
  token: string | null
  setUser: (user: User) => void
  setToken: (token: string) => void
  clearUser: () => void
  logout: () => void
  isLoading: boolean
  setIsLoading: (loading: boolean) => void
}

export const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  token: localStorage.getItem('authToken'),
  setUser: (user) => set({ user }),
  setToken: (token) => set({ token }),
  clearUser: () => set({ user: null, token: null }),
  logout: () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('token')
    set({ user: null, token: null })
  },
  isLoading: false,
  setIsLoading: (loading) => set({ isLoading: loading }),
}))

interface WeatherData {
  city: string
  temperature: number
  humidity: number
  advice: string
}

interface WeatherStore {
  weather: WeatherData | null
  setWeather: (data: WeatherData | null) => void
  clearWeather: () => void
  error: string | null
  setError: (error: string | null) => void
}

export const useWeatherStore = create<WeatherStore>((set) => ({
  weather: null,
  setWeather: (data) => set({ weather: data }),
  clearWeather: () => set({ weather: null }),
  error: null,
  setError: (error) => set({ error }),
}))

interface DashboardStats {
  totalProducts: number
  expiringCount: number
  runningOutCount: number
  expiredCount: number
  favoritesCount: number
  totalSpent: number
}

interface DashboardStore {
  stats: DashboardStats | null
  setStats: (stats: DashboardStats) => void
  isLoading: boolean
  setIsLoading: (loading: boolean) => void
}

export const useDashboardStore = create<DashboardStore>((set) => ({
  stats: null,
  setStats: (stats) => set({ stats }),
  isLoading: false,
  setIsLoading: (loading) => set({ isLoading: loading }),
}))
