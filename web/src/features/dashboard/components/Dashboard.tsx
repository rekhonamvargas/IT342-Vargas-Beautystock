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
  status?: string | null
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

  const removeNumbers = (str: string) => str.replace(/\d+/g, '')

  const getAvailability = (status?: string | null) => {
    const normalized = (status || '').toLowerCase()
    if (normalized.includes('out of stock')) return 'Out of stock'
    if (normalized.includes('running')) return 'Running low'
    return 'In stock'
  }

  const getAvailabilityBadge = (status?: string | null) => {
    const availability = getAvailability(status)
    if (availability === 'Running low') {
      return 'px-1.5 py-0.5 rounded-full font-semibold border'
    }
    if (availability === 'Out of stock') {
      return 'px-1.5 py-0.5 rounded-full font-semibold border'
    }
    return 'bg-gradient-to-r from-green-50 to-emerald-50 text-green-700 border-green-200'
  }

  const getAvailabilityStyle = (status?: string | null) => {
    const availability = getAvailability(status)
    if (availability === 'Running low') {
      return { backgroundColor: '#f97316', color: 'white', borderColor: '#ea580c' }
    }
    if (availability === 'Out of stock') {
      return { backgroundColor: '#ef4444', color: 'white', borderColor: '#dc2626' }
    }
    return {}
  }

  const registeredName = [user?.firstName, user?.lastName].filter(Boolean).join(' ').trim()
  const fallbackName = user?.email?.split('@')[0] || 'User'
  const displayName = toTitleCase(removeNumbers(registeredName || fallbackName))

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
             <span className="hello-wave-wrap">{renderWavingText(`Hello, ${displayName}`)}</span>
          </h1>
        </div>
        <Link to="/products/new" className="btn-pink px-5 py-2 rounded-full text-[11px] mt-1">
          + Add Product
        </Link>
      </div>

   {/* Stats Grid */}
<div className="grid grid-cols-1 md:grid-cols-4 gap-3">

  {/* Total Products */}
  <div className="stat-card py-4 border-[#E5CF83]">
    <div className="flex items-center gap-3">
      <div className="w-7 h-7 rounded-md bg-pink-50 flex items-center justify-center text-[11px]">🧴</div>
      
      <div>
        <div className="mystic-number mystic-gold risque-regular">
          {stats?.totalProducts ?? 0}
        </div>
        <p className="text-muted text-xs uppercase tracking-wider font-serif">
          Total Products
        </p>
      </div>
    </div>
  </div>

  {/* Favorites */}
  <div className="stat-card py-4 border-[#E5CF83]">
    <div className="flex items-center gap-3">
      <div className="w-7 h-7 rounded-md bg-[#F7EDFF] flex items-center justify-center text-[11px]">❤️</div>
      
      <div>
        <div className="mystic-number mystic-pink risque-regular">
          {stats?.favoritesCount ?? 0}
        </div>
        <p className="text-muted text-xs uppercase tracking-wider font-serif">
          Favorites
        </p>
      </div>
    </div>
  </div>

  {/* Expiring Soon */}
  <div className="stat-card py-4 border-[#E5CF83]">
    <div className="flex items-center gap-3">
      <div className="w-7 h-7 rounded-md bg-orange-50 flex items-center justify-center text-[11px]">⚠️</div>
      
      <div>
        <div className="mystic-number mystic-orange risque-regular">
          {stats?.expiringCount ?? 0}
        </div>
        <p className="text-muted text-xs uppercase tracking-wider font-serif">
          Expiring Soon
        </p>
      </div>
    </div>
  </div>

  {/* Running Out */}
  <div className="stat-card py-4 border-[#E5CF83]">
    <div className="flex items-center gap-3">
      <div className="w-7 h-7 rounded-md bg-[#FFE8EA] flex items-center justify-center text-[11px]">↘</div>
      
      <div>
        <div className="mystic-number mystic-rose risque-regular">
          {stats?.runningOutCount ?? 0}
        </div>
        <p className="text-muted text-xs uppercase tracking-wider font-serif">
          Running Out
        </p>
      </div>
    </div>
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
           <p className="text-muted text-xxs uppercase tracking-wider mb-1 font-normal font-serif">No products yet</p>
            <p className="text-muted text-xs" style={{ fontFamily: 'Arial, sans-serif' }}>Start by adding your first beauty product</p>
            <Link to="/products/new" className="btn-pink px-8 py-3 rounded-xl text-base mt-4">+ Add Product</Link>
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
                     {product.isFavorite ? '❤️' : '🤍'}
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
                       <div className="flex items-center gap-1">
                         {product.isExpiringWithin15Days && !product.isExpired && (
                           <span className="text-[10px] bg-orange-50 text-orange px-1.5 py-0.5 rounded-full font-semibold">Expiring</span>
                         )}
                         {product.isExpired && (
                           <span className="text-[10px] bg-red-50 text-red px-1.5 py-0.5 rounded-full font-semibold">Expired</span>
                         )}
                         <span className={`text-[9px] px-1.5 py-0.5 rounded-full font-semibold border ${getAvailabilityBadge(product.status)}`} style={getAvailabilityStyle(product.status)}>
                           {getAvailability(product.status)}
                         </span>
                       </div>
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
