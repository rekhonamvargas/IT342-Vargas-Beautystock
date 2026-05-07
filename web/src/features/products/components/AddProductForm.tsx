import { useState } from 'react'
import { productApi } from '@/services/api'

// Compress and enhance image quality using canvas
const enhanceImage = (file: File, maxWidth = 1200, quality = 0.92): Promise<File> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }

        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        if (!ctx) {
          reject(new Error('Failed to get canvas context'))
          return
        }

        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(new File([blob], file.name, { type: 'image/jpeg' }))
            } else {
              reject(new Error('Failed to create blob'))
            }
          },
          'image/jpeg',
          quality
        )
      }
      img.onerror = () => reject(new Error('Failed to load image'))
      img.src = e.target?.result as string
    }
    reader.onerror = () => reject(new Error('Failed to read file'))
    reader.readAsDataURL(file)
  })
}

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

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      try {
        const enhancedFile = await enhanceImage(file)
        setFile(enhancedFile)
      } catch (err) {
        console.error('Image enhancement failed:', err)
        setFile(file)
      }
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
    <div className="card max-w-2xl mx-auto font-sans text-[13px]">
      <h2 className="text-lg font-bold text-gray-800 mb-4 tracking-wide">
        Add New Product
      </h2>

      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="p-2 bg-red-50 border border-red-200 rounded text-red-600 text-xs">
            {error}
          </div>
        )}

        <div className="grid grid-cols-2 gap-3">
          <div>
             <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Product Name *</p>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              placeholder="Moisturizing Cream"
              className="input-field text-xs py-1.5"
              disabled={isLoading}
            />
          </div>

          <div>
            <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Brand *</p>
            <input
              type="text"
              name="brand"
              value={formData.brand}
              onChange={handleInputChange}
              placeholder="Glossier"
              className="input-field text-xs py-1.5"
              disabled={isLoading}
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
             <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Category</p>
            <input
              type="text"
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              placeholder="Skincare"
              className="input-field text-xs py-1.5"
              disabled={isLoading}
            />
          </div>

          <div>
             <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Price (₱) *</p>
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleInputChange}
              placeholder="0.00"
              step="0.01"
              className="input-field text-xs py-1.5"
              disabled={isLoading}
            />
          </div>
        </div>

        <div>
           <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Description</p>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            placeholder="Product description..."
            rows={2}
            className="input-field text-xs py-1.5"
            disabled={isLoading}
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
             <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Purchase Location</p>
            <input
              type="text"
              name="purchaseLocation"
              value={formData.purchaseLocation}
              onChange={handleInputChange}
              placeholder="Sephora"
              className="input-field text-xs py-1.5"
              disabled={isLoading}
            />
          </div>

          <div>
             <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Expiration Date</p>
            <input
              type="date"
              name="expirationDate"
              value={formData.expirationDate}
              onChange={handleInputChange}
              className="input-field text-xs py-1.5"
              disabled={isLoading}
            />
          </div>
        </div>

        <div>
           <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Opened Date</p>
          <input
            type="date"
            name="openedDate"
            value={formData.openedDate}
            onChange={handleInputChange}
            className="input-field text-xs py-1.5"
            disabled={isLoading}
          />
        </div>

        <div>
           <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Product Image</p>
          <input
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            className="block w-full text-xs text-gray-500 file:mr-3 file:py-1 file:px-3 file:rounded file:border-0 file:bg-pink-50 file:text-pink-600 hover:file:bg-pink-100"
            disabled={isLoading}
          />
        </div>

        <div className="flex gap-3 pt-3">
          <button
            type="submit"
            disabled={isLoading}
            className="flex-1 bg-pink-500 hover:bg-pink-600 text-white text-xs font-semibold py-2 rounded-lg transition"
          >
            {isLoading ? 'Creating...' : 'Add Product'}
          </button>

          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="flex-1 border border-gray-300 text-gray-600 text-xs py-2 rounded-lg hover:bg-gray-50 transition"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}