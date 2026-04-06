import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuthStore, useWeatherStore } from '@/store/auth'
import { recommendationApi } from '@/services/api'

const youthEssentials = [
  { icon: '🧴', title: 'Gentle Cleanser', desc: 'Use a mild, pH-balanced cleanser twice daily' },
  { icon: '💧', title: 'Light Moisturizer', desc: 'Oil-free hydration to keep skin balanced' },
  { icon: '☀️', title: 'SPF Protection', desc: 'Apply SPF 30+ sunscreen every morning' },
]

const adultEssentials = [
  { icon: '✨', title: 'Vitamin C Serum', desc: 'Antioxidant protection and brightening' },
  { icon: '💧', title: 'Hyaluronic Acid', desc: 'Deep hydration for plump, youthful skin' },
  { icon: '🌙', title: 'Retinol Night Cream', desc: 'Cell turnover and anti-aging benefits' },
]

export function SkincareAdvice() {
  const { user } = useAuthStore()
  const { weather, setWeather, setError } = useWeatherStore()
  const [loading, setLoading] = useState(false)

  const isYouth = user?.role === 'ROLE_YOUTH'
  const essentials = isYouth ? youthEssentials : adultEssentials

  useEffect(() => {
    if (!weather) {
      loadWeather()
    }
  }, [])

  const loadWeather = async () => {
    try {
      setLoading(true)
      const res = isYouth
        ? await recommendationApi.getYouthWeather()
        : await recommendationApi.getAdultWeather()
      setWeather(res.data)
    } catch {
      setError('Set your city in Profile to get weather-based skincare tips')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-5 max-w-[760px]">
      <div>
        <h1 className="font-serif text-[35px] font-bold text-dark leading-tight tracking-[-0.01em]">
          {isYouth ? 'Youth' : 'Adult'} Skincare Advice ✨
        </h1>
        <p className="text-muted mt-1 text-xs">
          Personalized tips based on your local weather and skin profile
        </p>
      </div>

      {/* Weather Advice Card */}
      {loading ? (
        <div className="rounded-2xl border border-[#E5CF83] bg-white text-center py-10">
          <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-muted text-sm">Loading weather data...</p>
        </div>
      ) : weather ? (
        <div className="rounded-2xl border border-[#E5CF83] bg-white p-4">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h3 className="font-semibold text-sm text-dark flex items-center gap-2">
                ☁️ {isYouth ? 'Youth' : 'Adult'} Weather Skin Advice
              </h3>
              <p className="text-muted text-xs mt-1 flex items-center gap-4">
                <span>🌡 {weather.temperature}°C</span>
                <span>💧 {weather.humidity}%</span>
                <span>{weather.city}</span>
              </p>
            </div>
          </div>
          <div className="mt-3 rounded-md border border-pink-200 bg-pink-50 px-3 py-2">
            <p className="text-[11px] text-pink-700 leading-relaxed">{weather.advice}</p>
          </div>
        </div>
      ) : (
        <div className="rounded-2xl border border-[#E5CF83] bg-white text-center py-12 px-6">
          <div className="text-3xl mb-2">📍</div>
          <h3 className="font-serif text-3xl font-bold text-dark mb-2">No location set</h3>
          <p className="text-muted text-base">
            Set your city in your <Link to="/profile" className="text-pink font-semibold hover:underline">profile</Link> to get personalized weather-based skincare tips
          </p>
        </div>
      )}

      {/* Essentials */}
      <div>
        <h2 className="font-serif text-[24px] font-bold text-dark mb-4">
          {isYouth ? '🌱 Youth' : '🌸 Adult'} Skincare Essentials
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {essentials.map((item) => (
            <div key={item.title} className="rounded-2xl border border-[#E5CF83] bg-white p-4">
              <div className="text-xl mb-2">{item.icon}</div>
              <h3 className="font-semibold text-dark text-sm mb-1">{item.title}</h3>
              <p className="text-muted text-xs leading-relaxed">{item.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
