import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { productApi, favoriteApi } from '@/services/api'
import { ProductImage } from './ProductImage'

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

export function FavoritesPage() {
  const [favorites, setFavorites] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
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

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-pink" style={{ fontFamily: "'Playfair Display', serif" }}>Favorites ❤️</h1>
        <p className="text-muted mt-1">
          {favorites.length} product{favorites.length !== 1 ? 's' : ''} you love
        </p>
      </div>

      {favorites.length === 0 ? (
        <div className="card text-center py-12">
          <div className="text-4xl mb-3">💝</div>
          <h3 className="font-serif text-lg font-bold text-dark mb-1">No favorites yet</h3>
          <p className="text-muted text-sm mb-4">
            Browse your products and tap the heart to add favorites
          </p>
          <Link to="/products" className="btn-pink">
            Browse Products
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {favorites.map((product) => (
            <Link key={product.id} to={`/products/${product.id}`} className="pc group">
              <div className="aspect-square bg-cream flex items-center justify-center overflow-hidden relative">
                <ProductImage
                  src={product.imageUrl}
                  alt={product.name}
                  className="w-full h-full"
                  placeholderClassName="bg-white text-muted"
                  placeholderText="No image"
                />
                <button
                  onClick={(e) => removeFavorite(e, product)}
                  className="absolute top-2 right-2 w-8 h-8 rounded-full bg-white/80 flex items-center justify-center hover:bg-white transition-colors"
                  title="Remove from favorites"
                >
                  ❤️
                </button>
              </div>
              <div className="p-4">
                <p className="text-xs text-muted font-semibold uppercase tracking-wide">
                  {product.brand}
                </p>
                <p className="text-sm font-bold text-dark mt-0.5 line-clamp-1">{product.name}</p>
                <div className="flex items-center justify-between mt-2">
                  <span className="text-sm font-bold text-pink">{formatPrice(product.price)}</span>
                  {product.isExpiringWithin15Days && !product.isExpired && (
                    <span className="text-xs bg-orange-50 text-orange px-2 py-0.5 rounded-full font-semibold">
                      Expiring
                    </span>
                  )}
                  {product.isExpired && (
                    <span className="text-xs bg-red-50 text-red px-2 py-0.5 rounded-full font-semibold">
                      Expired
                    </span>
                  )}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
