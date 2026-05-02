import { useEffect, useState } from 'react'
import { recommendationApi } from '@/services/api'
import { useWeatherStore } from '@/store/auth'

interface WeatherDisplayProps {
  userRole: string
}

export function WeatherDisplay({ userRole }: WeatherDisplayProps) {
  const [isLoading, setIsLoading] = useState(true)
  const weather = useWeatherStore((state) => state.weather)
  const setWeather = useWeatherStore((state) => state.setWeather)
  const error = useWeatherStore((state) => state.error)
  const setError = useWeatherStore((state) => state.setError)

  useEffect(() => {
    const fetchWeather = async () => {
      try {
        setIsLoading(true)
        setError(null)
        
        let response
        if (userRole === 'ROLE_YOUTH') {
          response = await recommendationApi.getYouthWeather()
        } else if (userRole === 'ROLE_ADULT') {
          response = await recommendationApi.getAdultWeather()
        }

        if (response?.data) {
          setWeather(response.data)
        }
      } catch (err: any) {
        console.error(err)
        setError(err.response?.data || 'Failed to fetch weather data')
        setWeather(null)
      } finally {
        setIsLoading(false)
      }
    }

    fetchWeather()
  }, [userRole, setWeather, setError])

  if (isLoading) {
    return <div className="text-center py-8">Loading weather data...</div>
  }

  if (error) {
    return (
      <div className="card bg-red-50 border border-red-200 max-w-md mx-auto">
        <p className="text-red-700">⚠️ {error}</p>
      </div>
    )
  }

  if (!weather) {
    return null
  }

  return (
    <div className="card max-w-md mx-auto bg-gradient-to-br from-rose-50 to-sage-50">
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div>
          <p className="text-gray-600 text-sm font-medium">Location</p>
          <p className="text-2xl font-bold text-gray-900">{weather.city}</p>
        </div>
        <div>
          <p className="text-gray-600 text-sm font-medium">Temperature</p>
          <p className="text-2xl font-bold text-rose-600">{weather.temperature}°C</p>
        </div>
      </div>

      <div>
        <p className="text-gray-600 text-sm font-medium mb-2">Humidity</p>
        <div className="bg-white rounded-full h-3 overflow-hidden">
          <div
            className="bg-gradient-to-r from-sage-400 to-sage-500 h-full"
            style={{ width: `${Math.min(weather.humidity, 100)}%` }}
          />
        </div>
        <p className="text-gray-700 text-sm mt-1">{weather.humidity}%</p>
      </div>

      <div className="mt-6 pt-6 border-t border-rose-200">
        <p className="text-gray-600 text-sm font-medium mb-2">Beauty Advice</p>
        <p className="text-gray-800 leading-relaxed italic">✨ {weather.advice}</p>
      </div>
    </div>
  )
}
