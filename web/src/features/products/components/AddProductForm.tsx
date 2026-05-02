import { useState } from 'react'
import { productApi } from '@/services/api'

interface AddProductFormProps {
  onSuccess: () => void
  onCancel: () => void
}

export function AddProductForm({ onSuccess, onCancel }: AddProductFormProps) {
  const [formData, setFormData] = useState({
    name: '',
    brand: '',
    category: '',
    description: '',
    price: '',
    purchaseLocation: '',
    expirationDate: '',
    openedDate: '',
  })
  const [file, setFile] = useState<File | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFile(e.target.files[0])
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!formData.name.trim() || !formData.brand.trim() || !formData.price) {
      setError('Please fill in required fields')
      return
    }

    try {
      setIsLoading(true)
      const productData = {
        ...formData,
        price: parseFloat(formData.price),
        expirationDate: formData.expirationDate || null,
        openedDate: formData.openedDate || null,
      }

      const response = await productApi.create(productData)
      const productId = response.data.id

      if (file) {
        await productApi.uploadImage(productId, file)
      }

      onSuccess()
    } catch (err: any) {
      setError(err.response?.data || 'Failed to add product')
      console.error(err)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="card max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold text-gray-900 mb-6">Add New Product</h2>

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
            {error}
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Product Name *</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              placeholder="e.g., Moisturizing Cream"
              className="input-field"
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Brand *</label>
            <input
              type="text"
              name="brand"
              value={formData.brand}
              onChange={handleInputChange}
              placeholder="e.g., Glossier"
              className="input-field"
              disabled={isLoading}
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
            <input
              type="text"
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              placeholder="e.g., Skincare"
              className="input-field"
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Price (₱) *</label>
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleInputChange}
              placeholder="0.00"
              step="0.01"
              className="input-field"
              disabled={isLoading}
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            placeholder="Product description..."
            rows={3}
            className="input-field"
            disabled={isLoading}
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Purchase Location</label>
            <input
              type="text"
              name="purchaseLocation"
              value={formData.purchaseLocation}
              onChange={handleInputChange}
              placeholder="e.g., Sephora"
              className="input-field"
              disabled={isLoading}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Expiration Date</label>
            <input
              type="date"
              name="expirationDate"
              value={formData.expirationDate}
              onChange={handleInputChange}
              className="input-field"
              disabled={isLoading}
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Opened Date</label>
          <input
            type="date"
            name="openedDate"
            value={formData.openedDate}
            onChange={handleInputChange}
            className="input-field"
            disabled={isLoading}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Product Image</label>
          <input
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:bg-rose-50 file:text-rose-600 hover:file:bg-rose-100"
            disabled={isLoading}
          />
        </div>

        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={isLoading}
            className="btn-primary flex-1"
          >
            {isLoading ? 'Creating...' : 'Add Product'}
          </button>
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="btn-secondary flex-1"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
