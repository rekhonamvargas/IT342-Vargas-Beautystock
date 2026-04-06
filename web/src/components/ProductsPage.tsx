import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
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

const categories = ['All', 'Skincare', 'Makeup', 'Hair Care', 'Body Care', 'Fragrance', 'Tools', 'Other']
const sortOptions = [
  { value: 'newest', label: 'Newest' },
  { value: 'price-asc', label: 'Price: Low to High' },
  { value: 'price-desc', label: 'Price: High to Low' },
  { value: 'name', label: 'Name A-Z' },
  { value: 'expiring', label: 'Expiring Soon' },
]

export function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [activeCategory, setActiveCategory] = useState('All')
  const [sortBy, setSortBy] = useState('newest')

  useEffect(() => {
    loadProducts()
  }, [])

  const loadProducts = async () => {
    try {
      setLoading(true)
      const res = await productApi.getAll()
      setProducts(res.data)
    } catch (err) {
      console.error('Failed to load products:', err)
    } finally {
      setLoading(false)
    }
  }

  const toggleFavorite = async (e: React.MouseEvent, product: Product) => {
    e.preventDefault()
    e.stopPropagation()
    try {
      console.log(`Toggling favorite for product ${product.id}: ${product.isFavorite}`)
      if (product.isFavorite) {
        await favoriteApi.remove(product.id)
      } else {
        await favoriteApi.add(product.id)
      }
      setProducts((prev) =>
        prev.map((p) => (p.id === product.id ? { ...p, isFavorite: !p.isFavorite } : p))
      )
      console.log(`Successfully toggled favorite for product ${product.id}`)
    } catch (err) {
      console.error('Failed to toggle favorite:', err)
    }
  }

  const formatPrice = (price: number) =>
    new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(price)

  const filteredProducts = useCallback(() => {
    let result = [...products]

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
  }, [products, searchQuery, activeCategory, sortBy])

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  const filtered = filteredProducts()

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-pink" style={{ fontFamily: "'Playfair Display', serif" }}>My Products</h1>
          <p className="text-muted mt-1">{products.length} product{products.length !== 1 ? 's' : ''} in your collection</p>
        </div>
        <Link to="/products/new" className="btn-pink">+ Add Product</Link>
      </div>

      {/* Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="flex-1 relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted">🔍</span>
          <input
            type="text"
            placeholder="Search products..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-cream-200 rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink"
          />
        </div>
        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="px-4 py-2.5 border border-cream-200 rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-pink-200"
        >
          {sortOptions.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>

      {/* Category Chips */}
      <div className="flex flex-wrap gap-2">
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

      {/* Product Grid */}
      {filtered.length === 0 ? (
        <div className="card text-center py-12">
          <div className="text-4xl mb-3">🔍</div>
          <h3 className="font-serif text-lg font-bold text-dark mb-1">No products found</h3>
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
                {/* Favorite Button */}
                <button
                  onClick={(e) => toggleFavorite(e, product)}
                  className="absolute top-2 right-2 w-8 h-8 rounded-full bg-white/80 flex items-center justify-center hover:bg-white transition-colors"
                >
                  {product.isFavorite ? '❤️' : '🤍'}
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
    </div>
  )
}
