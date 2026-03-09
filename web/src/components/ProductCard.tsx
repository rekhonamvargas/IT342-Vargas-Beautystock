import { useState } from 'react'
import { productApi, favoriteApi } from '@/services/api'

interface ProductCardProps {
  id: number
  name: string
  brand: string
  price: number
  imageUrl?: string
  expirationDate?: string
  isExpired?: boolean
  isExpiringWithin15Days?: boolean
  isFavorite?: boolean
  onDelete: (id: number) => void
  onFavoriteToggle: (id: number) => void
}

export function ProductCard({
  id,
  name,
  brand,
  price,
  imageUrl,
  expirationDate,
  isExpired,
  isExpiringWithin15Days,
  isFavorite,
  onDelete,
  onFavoriteToggle,
}: ProductCardProps) {
  const [isLoading, setIsLoading] = useState(false)

  const handleDelete = async () => {
    if (confirm('Are you sure you want to delete this product?')) {
      try {
        setIsLoading(true)
        await productApi.delete(id)
        onDelete(id)
      } catch (err) {
        console.error('Failed to delete product:', err)
      } finally {
        setIsLoading(false)
      }
    }
  }

  const handleFavorite = async () => {
    try {
      setIsLoading(true)
      if (isFavorite) {
        await favoriteApi.remove(id)
      } else {
        await favoriteApi.add(id)
      }
      onFavoriteToggle(id)
    } catch (err) {
      console.error('Failed to toggle favorite:', err)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="card hover:shadow-md transition-shadow">
      {imageUrl && (
        <div className="mb-4 bg-gray-100 rounded-lg overflow-hidden h-40 flex items-center justify-center">
          <img src={imageUrl} alt={name} className="w-full h-full object-cover" />
        </div>
      )}

      {isExpired && (
        <div className="mb-2 px-2 py-1 bg-red-100 border border-red-300 rounded text-red-700 text-xs font-medium">
          ⚠️ Expired
        </div>
      )}

      {isExpiringWithin15Days && !isExpired && (
        <div className="mb-2 px-2 py-1 bg-yellow-100 border border-yellow-300 rounded text-yellow-700 text-xs font-medium">
          ⏰ Expires Soon
        </div>
      )}

      <div className="mb-2">
        <h3 className="font-semibold text-gray-900">{name}</h3>
        <p className="text-sm text-gray-600">{brand}</p>
      </div>

      <div className="mb-4 flex items-center justify-between">
        <span className="text-lg font-bold text-rose-600">${price.toFixed(2)}</span>
        {expirationDate && (
          <span className="text-xs text-gray-500">Exp: {new Date(expirationDate).toLocaleDateString()}</span>
        )}
      </div>

      <div className="flex gap-2">
        <button
          onClick={handleFavorite}
          disabled={isLoading}
          className={`flex-1 px-3 py-2 rounded text-sm font-medium transition-colors ${
            isFavorite
              ? 'bg-rose-100 text-rose-700 hover:bg-rose-200'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          {isFavorite ? '❤️' : '🤍'} {isFavorite ? 'Saved' : 'Save'}
        </button>
        <button
          onClick={handleDelete}
          disabled={isLoading}
          className="px-3 py-2 bg-red-50 text-red-600 rounded text-sm font-medium hover:bg-red-100 transition-colors"
        >
          🗑️
        </button>
      </div>
    </div>
  )
}
