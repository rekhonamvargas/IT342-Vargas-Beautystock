import { useEffect, useState, useCallback } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { productApi, favoriteApi } from '@/services/api'
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

const categories = ['All', 'Skincare', 'Makeup', 'Hair Care', 'Body Care', 'Fragrance', 'Tools', 'Other']
const sortOptions = [
  { value: 'newest', label: 'Newest' },
  { value: 'price-asc', label: 'Price: Low to High' },
  { value: 'price-desc', label: 'Price: High to Low' },
  { value: 'name', label: 'Name A-Z' },
  { value: 'expiring', label: 'Expiring Soon' },
]

export function FavoritesPage() {
  const [favorites, setFavorites] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [activeCategory, setActiveCategory] = useState('All')
  const [sortBy, setSortBy] = useState('newest')
  const location = useLocation()

  useEffect(() => {
    loadFavorites()
  }, [location.pathname])

  const loadFavorites = async () => {
    try {
      setLoading(true)
      const res = await productApi.getAll()
      setFavorites(res.data.filter((p: Product) => p.isFavorite))
    } catch (err) {
      console.error('Failed to load favorites:', err)
    } finally {
      setLoading(false)
    }
  }

  const removeFavorite = async (e: React.MouseEvent, product: Product) => {
    e.preventDefault()
    e.stopPropagation()
    try {
      await favoriteApi.remove(product.id)
      setFavorites((prev) => prev.filter((p) => p.id !== product.id))
    } catch (err) {
      console.error('Failed to remove favorite:', err)
    }
  }

  const formatPrice = (price: number) =>
    new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(price)

  const filteredFavorites = useCallback(() => {
    let result = [...favorites]

    // Search
    if (searchQuery) {
      const q = searchQuery.toLowerCase()
      result = result.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          p.brand.toLowerCase().includes(q) ||
          p.category.toLowerCase().includes(q)
      )
    }

    // Category
    if (activeCategory !== 'All') {
      result = result.filter(
        (p) => p.category.toLowerCase() === activeCategory.toLowerCase()
      )
    }

    // Sort
    switch (sortBy) {
      case 'price-asc':
        result.sort((a, b) => a.price - b.price)
        break
      case 'price-desc':
        result.sort((a, b) => b.price - a.price)
        break
      case 'name':
        result.sort((a, b) => a.name.localeCompare(b.name))
        break
      case 'expiring':
        result.sort((a, b) => {
          if (!a.expirationDate) return 1
          if (!b.expirationDate) return -1
          return new Date(a.expirationDate).getTime() - new Date(b.expirationDate).getTime()
        })
        break
      default: // newest - reverse order (most recently added first)
        result.reverse()
    }

    return result
  }, [favorites, searchQuery, activeCategory, sortBy])

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
        <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  const filtered = filteredFavorites()

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="hello-title text-pink">
            <span className="hello-wave-wrap">{renderWavingText('Favorites')}</span>
          </h1>
          <p className="text-muted text-[10px] uppercase tracking-wider mb-1 font-normal font-serif mt-3">
  {filtered.length} product{filtered.length !== 1 ? 's' : ''} you love
</p>
        </div>
      </div>

        {/* Search & Filter Bar - always visible */}
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1 relative">
            <div className="absolute left-4 top-1/2 -translate-y-1/2 text-muted">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
            </div>
            <input
              type="text"
              placeholder="Search favorites..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-12 pr-5 py-3 border-2 border-pink-100 rounded-xl bg-white text-sm focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink transition-all placeholder:text-muted/60"
            />
          </div>
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="px-5 py-3 border-2 border-pink-100 rounded-xl bg-white text-sm focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink transition-all min-w-[180px]"
          >
            {sortOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>

        {/* Category Chips - always visible */}
        <div className="flex flex-wrap gap-3">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => setActiveCategory(cat)}
              className={`chip ${activeCategory === cat ? 'active' : ''}`}
            >
              {cat}
            </button>
          ))}
        </div>

       {favorites.length === 0 ? (
        <div className="card text-center py-12">
          <div className="text-4xl mb-3">💝</div>
          <p className="text-muted text-xxs uppercase tracking-wider mb-1 font-normal font-serif">No favorites yet</p>
          <p className="text-muted text-xs" style={{ fontFamily: 'Arial, sans-serif' }}>
   Browse your products and tap the heart to add favorites
</p>
         
         <Link to="/products" className="btn-pink mt-4">
  Browse Products
</Link>
        </div>
       ) : (
         <>
           {/* Favorites Grid */}
           {filtered.length === 0 ? (
            <div className="card text-center py-12">
              <div className="text-4xl mb-3">🔍</div>
              <h3 className="font-serif text-lg font-bold text-dark mb-1">No favorites found</h3>
              <p className="text-muted text-sm">Try adjusting your search or filters</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              {filtered.map((product) => (
                <Link key={product.id} to={`/products/${product.id}`} className="pc group">
                  <div className="aspect-square bg-cream flex items-center justify-center overflow-hidden relative">
                    <ProductImage
                      src={product.imageUrl}
                      alt={product.name}
                      className="w-full h-full"
                      placeholderClassName="bg-white text-muted"
                      placeholderText="No image"
                    />
                    {/* Remove from Favorites Button */}
                    <button
                      onClick={(e) => removeFavorite(e, product)}
                      className="absolute top-2 right-2 w-8 h-8 rounded-full bg-white/80 flex items-center justify-center hover:bg-white transition-colors"
                      title="Remove from favorites"
                    >
                      ❤️
                    </button>
                  </div>
                  <div className="p-4">
                    <p className="text-xs text-muted font-semibold uppercase tracking-wide">{product.brand}</p>
                    <p className="text-sm font-bold text-dark mt-0.5 line-clamp-1">{product.name}</p>
                    <div className="flex items-center justify-between mt-2">
                      <span className="text-sm font-bold text-pink">{formatPrice(product.price)}</span>
                      {product.isExpiringWithin15Days && !product.isExpired && (
                        <span className="text-xs bg-orange-50 text-orange px-2 py-0.5 rounded-full font-semibold">Expiring</span>
                      )}
                      {product.isExpired && (
                        <span className="text-xs bg-red-50 text-red px-2 py-0.5 rounded-full font-semibold">Expired</span>
                      )}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
