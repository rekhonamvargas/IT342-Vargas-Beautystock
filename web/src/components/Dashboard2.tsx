import { useEffect, useState } from 'react'
import { authApi, productApi } from '@/services/api'
import { useAuthStore, useDashboardStore } from '@/store/auth'
import { LocationForm } from './LocationForm'
import { WeatherDisplay } from './WeatherDisplay'
import { ProductCard } from './ProductCard'
import { AddProductForm } from './AddProductForm'

export function Dashboard() {
  const user = useAuthStore((state) => state.user)
  const setUser = useAuthStore((state) => state.setUser)
  const isLoading = useAuthStore((state) => state.isLoading)
  const setIsLoading = useAuthStore((state) => state.setIsLoading)

  const stats = useDashboardStore((state) => state.stats)
  const setStats = useDashboardStore((state) => state.setStats)
  const setStatsLoading = useDashboardStore((state) => state.setIsLoading)

  const [products, setProducts] = useState<any[]>([])
  const [filteredProducts, setFilteredProducts] = useState<any[]>([])
  const [searchQuery, setSearchQuery] = useState('')
  const [showAddForm, setShowAddForm] = useState(false)
  const [activeTab, setActiveTab] = useState<'all' | 'expiring' | 'favorites'>('all')

  useEffect(() => {
    const fetchUser = async () => {
      try {
        setIsLoading(true)
        const response = await authApi.getMe()
        setUser(response.data)
      } catch (err) {
        console.error('Failed to fetch user:', err)
        window.location.href = '/login'
      } finally {
        setIsLoading(false)
      }
    }

    fetchUser()
  }, [setUser, setIsLoading])

  useEffect(() => {
    if (!user) return

    const fetchData = async () => {
      try {
        setStatsLoading(true)
        const [productsRes, statsRes] = await Promise.all([
          productApi.getAll(),
          productApi.getDashboard(),
        ])
        setProducts(productsRes.data)
        setFilteredProducts(productsRes.data)
        setStats(statsRes.data)
      } catch (err) {
        console.error('Failed to fetch data:', err)
      } finally {
        setStatsLoading(false)
      }
    }

    fetchData()
  }, [user, setStats, setStatsLoading])

  const handleSearch = (query: string) => {
    setSearchQuery(query)
    if (query.trim()) {
      const filtered = products.filter(
        (p) =>
          p.name.toLowerCase().includes(query.toLowerCase()) ||
          p.brand.toLowerCase().includes(query.toLowerCase())
      )
      setFilteredProducts(filtered)
    } else {
      setFilteredProducts(products)
    }
  }

  const handleTabChange = (tab: 'all' | 'expiring' | 'favorites') => {
    setActiveTab(tab)
    setSearchQuery('')

    if (tab === 'expiring') {
      setFilteredProducts(products.filter((p) => p.isExpiringWithin15Days || p.isExpired))
    } else if (tab === 'favorites') {
      setFilteredProducts(products.filter((p) => p.isFavorite))
    } else {
      setFilteredProducts(products)
    }
  }

  const handleProductDelete = (id: number) => {
    setProducts(products.filter((p) => p.id !== id))
    setFilteredProducts(filteredProducts.filter((p) => p.id !== id))
  }

  const handleProductAdded = () => {
    setShowAddForm(false)
    productApi.getAll().then((res) => {
      setProducts(res.data)
      setFilteredProducts(res.data)
    })
    productApi.getDashboard().then((res) => {
      setStats(res.data)
    })
  }

  const handleFavoriteToggle = (id: number) => {
    const updatedProducts = products.map((p) =>
      p.id === id ? { ...p, isFavorite: !p.isFavorite } : p
    )
    setProducts(updatedProducts)
    setFilteredProducts(
      filteredProducts.map((p) => (p.id === id ? { ...p, isFavorite: !p.isFavorite } : p))
    )
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-rose-50 to-cream-50">
        <div className="text-center">
          <p className="text-xl text-gray-700">Loading your dashboard...</p>
        </div>
      </div>
    )
  }

  if (!user) {
    return null
  }

  if (showAddForm) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-rose-50 to-cream-50 py-12 px-4">
        <div className="max-w-4xl mx-auto">
          <AddProductForm onSuccess={handleProductAdded} onCancel={() => setShowAddForm(false)} />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-rose-50 to-cream-50 py-12 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-12">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-4xl font-bold text-gray-900 mb-2">
                Welcome, {user.firstName}! 💄
              </h1>
              <p className="text-gray-600">
                {user.role === 'ROLE_YOUTH' ? '🌸 Youth' : '✨ Adult'} • {user.city ? `📍 ${user.city}` : 'City not set'}
              </p>
            </div>
            <button
              onClick={() => setShowAddForm(true)}
              className="btn-primary"
            >
              + Add Product
            </button>
          </div>
        </div>

        {/* Dashboard Stats */}
        {stats && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-12">
            <div className="card text-center">
              <p className="text-3xl font-bold text-rose-600">{stats.totalProducts}</p>
              <p className="text-gray-600 text-sm">Total Products</p>
            </div>
            <div className="card text-center">
              <p className="text-3xl font-bold text-yellow-600">{stats.expiringCount}</p>
              <p className="text-gray-600 text-sm">Expiring Soon</p>
            </div>
            <div className="card text-center">
              <p className="text-3xl font-bold text-red-600">{stats.expiredCount}</p>
              <p className="text-gray-600 text-sm">Expired</p>
            </div>
            <div className="card text-center">
              <p className="text-3xl font-bold text-rose-500">${stats.totalSpent.toFixed(2)}</p>
              <p className="text-gray-600 text-sm">Total Spent</p>
            </div>
          </div>
        )}

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-12">
          {/* Weather Section */}
          {user.city && (
            <div className="lg:col-span-2">
              <h2 className="text-2xl font-semibold text-gray-900 mb-6">Weather-Based Beauty Tips</h2>
              <WeatherDisplay userRole={user.role} />
            </div>
          )}

          {/* Sidebar */}
          <div className="space-y-6">
            {!user.city ? (
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Get Started</h3>
                <LocationForm
                  onSuccess={() => {
                    location.reload()
                  }}
                />
              </div>
            ) : (
              <div className="card">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Change Location</h3>
                <LocationForm
                  onSuccess={() => {
                    location.reload()
                  }}
                />
              </div>
            )}

            {/* User Info Card */}
            <div className="card">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Profile</h3>
              <div className="space-y-3">
                <div>
                  <p className="text-sm text-gray-600">Email</p>
                  <p className="text-gray-900 font-medium break-all text-sm">{user.email}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Role</p>
                  <span className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                    user.role === 'ROLE_YOUTH'
                      ? 'bg-rose-100 text-rose-700'
                      : 'bg-sage-100 text-sage-700'
                  }`}>
                    {user.role === 'ROLE_YOUTH' ? '🌸 Youth' : '✨ Adult'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Products Section */}
        <div>
          <div className="mb-6">
            <h2 className="text-2xl font-semibold text-gray-900 mb-4">My Products</h2>

            {/* Tabs */}
            <div className="flex gap-4 mb-6 border-b border-gray-200">
              <button
                onClick={() => handleTabChange('all')}
                className={`px-4 py-2 font-medium transition-colors ${
                  activeTab === 'all'
                    ? 'border-b-2 border-rose-500 text-rose-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                All ({products.length})
              </button>
              <button
                onClick={() => handleTabChange('expiring')}
                className={`px-4 py-2 font-medium transition-colors ${
                  activeTab === 'expiring'
                    ? 'border-b-2 border-rose-500 text-rose-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Expiring ({stats?.expiringCount || 0})
              </button>
              <button
                onClick={() => handleTabChange('favorites')}
                className={`px-4 py-2 font-medium transition-colors ${
                  activeTab === 'favorites'
                    ? 'border-b-2 border-rose-500 text-rose-600'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                Saved ({stats?.favoritesCount || 0})
              </button>
            </div>

            {/* Search */}
            <input
              type="text"
              placeholder="Search products..."
              value={searchQuery}
              onChange={(e) => handleSearch(e.target.value)}
              className="input-field mb-6"
            />
          </div>

          {/* Product Grid */}
          {filteredProducts.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredProducts.map((product) => (
                <ProductCard
                  key={product.id}
                  {...product}
                  onDelete={handleProductDelete}
                  onFavoriteToggle={handleFavoriteToggle}
                />
              ))}
            </div>
          ) : (
            <div className="card text-center py-12">
              <p className="text-gray-500 text-lg">No products found. Start by adding your first product! 💄</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
