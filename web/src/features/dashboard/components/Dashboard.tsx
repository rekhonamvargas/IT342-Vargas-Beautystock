import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuthStore, useWeatherStore, useDashboardStore } from '@/store/auth'
import { productApi, recommendationApi, favoriteApi } from '@/services/api'
import { ProductImage } from '@/features/products/components/ProductImage'

interface Product {
  id: number
  name: string
  brand: string
  category: string
  price: number
  imageUrl: string | null
  expirationDate: string | null
  isExpired: boolean
  isFavorite: boolean
  isExpiringWithin15Days: boolean
}

export function Dashboard() {
  const { user } = useAuthStore()
  const { weather, error: weatherError, setWeather, setError: setWeatherError } = useWeatherStore()
  const { stats, setStats } = useDashboardStore()
  const [recentProducts, setRecentProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [weatherLoading, setWeatherLoading] = useState(false)
  const [togglingFavorite, setTogglingFavorite] = useState<number | null>(null)

  const productPastelBackgrounds = ['bg-[#F8E6EB]', 'bg-[#E7F0EB]', 'bg-[#EDE7F8]', 'bg-[#FBEEDC]']

  const toTitleCase = (value: string) =>
    value
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
      .join(' ')

  const registeredName = [user?.firstName, user?.lastName].filter(Boolean).join(' ').trim()
  const fallbackName = user?.email?.split('@')[0] || 'User'
  const displayName = toTitleCase(registeredName || fallbackName)

  useEffect(() => {
    loadData()
  }, [])

  const loadWeatherAdvice = async () => {
    try {
      setWeatherLoading(true)
      setWeatherError(null)

      const weatherRes =
        user?.role === 'ROLE_YOUTH'
          ? await recommendationApi.getYouthWeather()
          : await recommendationApi.getAdultWeather()

      setWeather(weatherRes.data)
    } catch {
      setWeather(null)
      setWeatherError('Unable to load weather advice. Set your city in Profile and try again.')
    } finally {
      setWeatherLoading(false)
    }
  }

  const loadData = async () => {
    try {
      setLoading(true)
      const [dashRes, prodRes] = await Promise.all([
        productApi.getDashboard(),
        productApi.getAll(),
      ])
      setStats(dashRes.data)
      setRecentProducts(prodRes.data.slice(0, 4))

      await loadWeatherAdvice()
    } catch (err) {
      console.error('Dashboard load error:', err)
    } finally {
      setLoading(false)
    }
  }

  const toggleFavorite = async (e: React.MouseEvent, productId: number) => {
    e.preventDefault()
    e.stopPropagation()

    const product = recentProducts.find(p => p.id === productId)
    if (!product) return

    try {
      setTogglingFavorite(productId)
      const nextIsFavorite = !product.isFavorite

      console.log(`Toggling favorite for product ${productId}: ${product.isFavorite} → ${nextIsFavorite}`)

      // Optimistic update
      setRecentProducts((prev) =>
        prev.map((p) =>
          p.id === productId ? { ...p, isFavorite: nextIsFavorite } : p
        )
      )

      // API call
      if (nextIsFavorite) {
        console.log(`Adding to favorites: ${productId}`)
        await favoriteApi.add(productId)
      } else {
        console.log(`Removing from favorites: ${productId}`)
        await favoriteApi.remove(productId)
      }
      console.log(`Successfully toggled favorite for product ${productId}`)
    } catch (err) {
      console.error('Failed to toggle favorite:', err)
      // Rollback on error
      setRecentProducts((prev) =>
        prev.map((p) =>
          p.id === productId ? { ...p, isFavorite: product.isFavorite } : p
        )
      )
    } finally {
      setTogglingFavorite(null)
    }
  }

  const formatPrice = (price: number) =>
    new Intl.NumberFormat('en-PH', {
      style: 'currency',
      currency: 'PHP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(price)

  const renderWavingText = (text: string) =>
    Array.from(text).map((character, index) => (
      <span
        key={`${character}-${index}`}
        className="hello-wave"
        style={{ animationDelay: `${index * 0.08}s` }}
      >
        {character === ' ' ? '\u00A0' : character}
      </span>
    ))

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="text-center">
          <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-muted text-sm">Loading your dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-5 w-full font-sans">
      {/* Header */}
      <div className="flex w-full items-start justify-between gap-6 pt-1">
        <div className="min-w-0 flex-1">
          <h1
            className="hello-title text-pink"
          >
            <span className="hello-wave-wrap">{renderWavingText(`Hello, ${displayName} ✨`)}</span>
          </h1>
        </div>
        <Link to="/products/new" className="btn-pink px-5 py-2 rounded-full text-[11px] mt-1">
          + Add Product
        </Link>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        <div className="stat-card py-4 border-[#E5CF83]">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded-md bg-pink-50 flex items-center justify-center text-[11px]">🧴</div>
            <div>
              <div className="text-2xl font-bold text-dark leading-none">{stats?.totalProducts ?? 0}</div>
              <div className="text-xs text-muted mt-1">Total Products</div>
            </div>
          </div>
        </div>
        <div className="stat-card py-4 border-[#E5CF83]">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded-md bg-[#F7EDFF] flex items-center justify-center text-[11px]">❤️</div>
            <div>
              <div className="text-2xl font-bold text-dark leading-none">{stats?.favoritesCount ?? 0}</div>
              <div className="text-xs text-muted mt-1">Favorites</div>
            </div>
          </div>
        </div>
        <div className="stat-card py-4 border-[#E5CF83]">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded-md bg-orange-50 flex items-center justify-center text-[11px]">⚠️</div>
            <div>
              <div className="text-2xl font-bold text-orange leading-none">{stats?.expiringCount ?? 0}</div>
              <div className="text-xs text-muted mt-1">Expiring Soon</div>
            </div>
          </div>
        </div>
        <div className="stat-card py-4 border-[#E5CF83]">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded-md bg-[#FFE8EA] flex items-center justify-center text-[11px]">↘</div>
            <div>
              <div className="text-2xl font-bold text-[#D94A6D] leading-none">{stats?.runningOutCount ?? 0}</div>
              <div className="text-xs text-muted mt-1">Running Out</div>
            </div>
          </div>
        </div>
      </div>

      {/* Weather Card */}
      <div className="rounded-2xl border border-[#E5CF83] bg-white p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <h3 className="font-semibold text-sm text-dark flex items-center gap-2">
              ☁️ {user?.role === 'ROLE_YOUTH' ? 'Youth' : 'Adult'} Skincare Advice
            </h3>

            {weatherLoading ? (
              <p className="text-muted text-xs mt-2">Loading weather advice...</p>
            ) : weather ? (
              <p className="text-muted text-xs mt-1 flex items-center gap-4">
                <span>🌡 {weather.temperature}°C</span>
                <span>💧 {weather.humidity}%</span>
                <span>{weather.city}</span>
              </p>
            ) : (
              <p className="text-muted text-xs mt-1">No weather data yet.</p>
            )}
          </div>

          <button
            onClick={loadWeatherAdvice}
            className="text-[11px] font-semibold text-pink hover:underline"
            type="button"
          >
            Refresh
          </button>
        </div>

        <div className="mt-3 rounded-md border border-pink-200 bg-pink-50 px-3 py-2">
          <p className="text-[11px] text-pink-700 leading-relaxed">
            {weather?.advice || weatherError || 'Set your city in Profile to load weather-based skincare advice.'}
          </p>
        </div>
      </div>

      {/* Recent Products */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-[24px] font-bold text-pink">Recent Products</h2>
          <Link to="/products" className="text-xs font-semibold text-pink hover:underline">
            View all →
          </Link>
        </div>

        {recentProducts.length === 0 ? (
          <div className="rounded-2xl border border-[#E5CF83] bg-white text-center py-14 px-6 min-h-[320px] flex flex-col items-center justify-center">
            <div className="text-4xl mb-4">📦</div>
            <h3 className="text-4xl leading-tight font-bold text-dark mb-2">No products yet</h3>
            <p className="text-muted text-xl leading-tight mb-7">Start by adding your first beauty product</p>
            <Link to="/products/new" className="btn-pink px-8 py-3 rounded-xl text-base">+ Add Product</Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {recentProducts.slice(0, 3).map((product, idx) => (
              <div key={product.id} className="pc rounded-xl border border-cream-200 overflow-hidden">
                <div className={`aspect-[16/10] ${productPastelBackgrounds[idx % productPastelBackgrounds.length]} flex flex-col items-center justify-center overflow-hidden relative`}>
                  <button
                    type="button"
                    onClick={(e) => {
                      e.preventDefault()
                      e.stopPropagation()
                      toggleFavorite(e, product.id)
                    }}
                    disabled={togglingFavorite === product.id}
                    className="absolute top-2 right-2 text-lg hover:scale-125 transition-transform disabled:opacity-50 z-10"
                    style={{
                      textShadow: '2px 2px 4px rgba(0,0,0,0.3), -2px -2px 4px rgba(255,255,255,0.5)',
                      filter: 'drop-shadow(1px 1px 2px rgba(200,100,100,0.4))',
                    }}
                    title={product.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                  >
                    {product.isFavorite ? '❤️' : '♡'}
                  </button>
                  <Link to={`/products/${product.id}`} className="block w-full h-full flex items-center justify-center">
                    <ProductImage
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-full h-full"
                      placeholderClassName="bg-white/60 text-muted"
                      placeholderText="No image"
                    />
                  </Link>
                </div>
                <div className="p-3 space-y-2">
                  <Link to={`/products/${product.id}`} className="block">
                    <p className="text-[10px] text-pink font-semibold uppercase tracking-wide">{product.brand}</p>
                    <p className="text-sm font-semibold text-dark mt-1 line-clamp-1">{product.name}</p>
                    <div className="flex items-center justify-between mt-2">
                      <span className="text-sm font-bold text-dark">{formatPrice(product.price)}</span>
                      {product.isExpiringWithin15Days && !product.isExpired && (
                        <span className="text-[10px] bg-orange-50 text-orange px-2 py-0.5 rounded-full font-semibold">Expiring</span>
                      )}
                      {product.isExpired && (
                        <span className="text-[10px] bg-red-50 text-red px-2 py-0.5 rounded-full font-semibold">Expired</span>
                      )}
                    </div>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
