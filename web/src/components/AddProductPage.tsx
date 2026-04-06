import { useState, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { productApi } from '@/services/api'

const categories = ['Skincare', 'Makeup', 'Hair Care', 'Body Care', 'Fragrance', 'Tools', 'Other']

export function AddProductPage() {
  const navigate = useNavigate()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [imageFile, setImageFile] = useState<File | null>(null)

  const [formData, setFormData] = useState({
    name: '',
    brand: '',
    category: 'Skincare',
    description: '',
    price: '',
    purchaseLocation: '',
    expirationDate: '',
    openedDate: '',
    status: 'New',
  })

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      setImageFile(file)
      const reader = new FileReader()
      reader.onload = () => setImagePreview(reader.result as string)
      reader.readAsDataURL(file)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!formData.name || !formData.brand) {
      setError('Product name and brand are required')
      return
    }

    try {
      setIsSubmitting(true)
      const basePayload = {
        ...formData,
        price: parseFloat(formData.price) || 0,
        expirationDate: formData.expirationDate || null,
        openedDate: formData.openedDate || null,
      }

      let res
      try {
        // Try saving with status first.
        res = await productApi.create(basePayload)
      } catch (createErr: any) {
        // Some backend versions may not support the new `status` field yet.
        const shouldRetryWithoutStatus = createErr?.response?.status === 400 || createErr?.response?.status === 422
        if (!shouldRetryWithoutStatus) throw createErr

        const { status, ...payloadWithoutStatus } = basePayload as any
        res = await productApi.create(payloadWithoutStatus)
      }

      // Upload image if selected
      if (imageFile && res.data?.id) {
        try {
          await productApi.uploadImage(res.data.id, imageFile)
        } catch (uploadErr) {
          // Product is already saved; don't block completion on image upload failure.
          console.warn('Product saved but image upload failed:', uploadErr)
        }
      }

      navigate('/products')
    } catch (err: any) {
      const data = err?.response?.data
      const status = err?.response?.status
      const msg =
        (typeof data === 'string' && data) ||
        data?.message ||
        data?.error ||
        err?.message ||
        'Failed to create product'
      setError(status ? `${msg} (HTTP ${status})` : msg)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="w-full h-[calc(100vh-84px)] flex flex-col overflow-hidden">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-[18px] font-semibold uppercase tracking-[0.08em] text-pink"> Add Product</h1>
        <Link to="/products" className="text-sm text-pink font-semibold hover:underline">
          ← Back to Products
        </Link>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-100 rounded-lg text-red text-sm">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex-1 grid grid-rows-[minmax(0,1fr)_auto] gap-3 min-h-0">
        <div className="grid grid-cols-1 xl:grid-cols-[360px_minmax(0,1fr)] gap-3 items-stretch min-h-0">
          <div className="card !p-4 flex flex-col h-full">
            <button
              type="button"
              className="w-full h-[300px] xl:h-[330px] border-2 border-dashed border-pink-300 rounded-2xl bg-gradient-to-br from-pink-50 via-rose-50 to-pink-100/70 hover:border-pink-400 hover:from-pink-100 hover:to-rose-100 transition-colors flex items-center justify-center"
              onClick={() => fileInputRef.current?.click()}
            >
              {imagePreview ? (
                <img src={imagePreview} alt="Preview" className="w-full h-full object-cover rounded-lg" />
              ) : (
                <div className="text-center px-4">
                  <div className="mx-auto mb-5 h-12 w-12 rounded-full bg-pink-200/80 flex items-center justify-center text-xl text-pink-600 shadow-sm">↥</div>
                  <p className="font-consolas text-[18px] font-semibold text-pink-600 leading-none tracking-[-0.01em]">Upload image</p>
                </div>
              )}
            </button>
            <p className="text-[12px] text-muted mt-auto pt-3 text-center">JPG or PNG · max 5MB</p>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageSelect}
              className="hidden"
            />
          </div>

          <div className="card !p-5 h-full overflow-hidden">
            <h2 className="text-[18px] font-semibold uppercase tracking-[0.08em] text-pink mb-3">Product Details</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="fg">
                <label>Product name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="e.g. Hydrating Face Cream"
                  required
                />
              </div>
              <div className="fg">
                <label>Category</label>
                <select name="category" value={formData.category} onChange={handleChange}>
                  {categories.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-3">
              <div className="fg">
                <label>Brand *</label>
                <input
                  type="text"
                  name="brand"
                  value={formData.brand}
                  onChange={handleChange}
                  placeholder="e.g. CeraVe"
                  required
                />
              </div>
              <div className="fg">
                <label>Purchase location</label>
                <input
                  type="text"
                  name="purchaseLocation"
                  value={formData.purchaseLocation}
                  onChange={handleChange}
                  placeholder="e.g. Watsons"
                />
              </div>
            </div>

            <div className="fg mt-3">
              <label>Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Brief description of the product, key ingredients, skin type..."
                rows={3}
              />
            </div>
          </div>
        </div>

        <div className="card !p-5 flex flex-col justify-between min-h-[190px]">
          <div>
            <h2 className="text-[18px] font-semibold uppercase tracking-[0.08em] text-pink mb-3">Dates & Pricing</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="fg">
                <label>Price (₱)</label>
                <input
                  type="number"
                  name="price"
                  value={formData.price}
                  onChange={handleChange}
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                />
              </div>
              <div className="fg">
                <label>Expiration date</label>
                <input
                  type="date"
                  name="expirationDate"
                  value={formData.expirationDate}
                  onChange={handleChange}
                />
              </div>
              <div className="fg">
                <label>Opened date</label>
                <input
                  type="date"
                  name="openedDate"
                  value={formData.openedDate}
                  onChange={handleChange}
                />
              </div>
              <div className="fg">
                <label>Status</label>
                <select
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                >
                  <option value="New">New</option>
                  <option value="running low">running low</option>
                  <option value="out of stock">out of stock</option>
                </select>
              </div>
            </div>
          </div>

          <div className="mt-5 border-t border-cream-200 pt-4 flex items-center justify-between gap-4 flex-wrap">
            <p className="text-[12px] text-muted">* Required fields</p>

            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => navigate('/products')}
                className="px-4 py-2.5 rounded-xl border border-cream-300 text-dark bg-white hover:bg-cream-50"
              >
                Cancel
              </button>
              <button type="submit" disabled={isSubmitting} className="btn-pink px-7 py-2.5 rounded-xl">
                {isSubmitting ? 'Saving...' : 'Save product'}
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
  )
}
