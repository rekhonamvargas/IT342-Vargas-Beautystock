import { useState, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
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

        // Resize if image is too large (maintain aspect ratio)
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

        // Draw with high quality smoothing
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'high'
        ctx.drawImage(img, 0, 0, width, height)

        // Convert to blob with high quality
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

  const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      try {
        const enhancedFile = await enhanceImage(file)
        setImageFile(enhancedFile)
        const reader = new FileReader()
        reader.onload = () => setImagePreview(reader.result as string)
        reader.readAsDataURL(enhancedFile)
      } catch (err) {
        console.error('Image enhancement failed:', err)
        // Fallback to original file
        setImageFile(file)
        const reader = new FileReader()
        reader.onload = () => setImagePreview(reader.result as string)
        reader.readAsDataURL(file)
      }
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

  return (
    <div className="w-full h-[calc(100vh-84px)] flex flex-col overflow-hidden relative">
      {/* Decorative sparkle */}
      <div className="absolute top-6 right-6 text-pink opacity-10 animate-bounce" style={{animationDuration: '2.5s'}}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
        </svg>
      </div>

        {/* Header */}
        <div className="flex items-center justify-between mb-3 px-1">
          <div className="flex items-center gap-2">
            <span className="text-xl">✨</span>
            <h1 className="hello-title text-pink text-2xl">
              <span className="hello-wave-wrap">{renderWavingText('Add Product')}</span>
            </h1>
          </div>
         <Link to="/products" className="text-xs text-pink font-semibold hover:underline flex items-center gap-1 transition-colors">
           <span>←</span> Back
         </Link>
       </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border-2 border-red-200 rounded-xl text-red text-sm flex items-center gap-2 animate-fade-in mx-1">
          <span>⚠️</span> {error}
        </div>
      )}

       <form onSubmit={handleSubmit} className="flex-1 grid grid-rows-[minmax(0,1fr)_auto] gap-2 min-h-0 px-1">
        <div className="grid grid-cols-1 xl:grid-cols-[360px_minmax(0,1fr)] gap-2 items-stretch min-h-0">
          {/* Image Upload Card */}
          <div className="card !p-3 flex flex-col h-full relative overflow-hidden">
            {/* Accent bar */}
            <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-pink-300 via-rose-300 to-pink-300"></div>
            
            <button
              type="button"
              className="w-full h-[260px] xl:h-[280px] border-2 border-dashed border-pink-200 rounded-2xl bg-gradient-to-br from-pink-50 via-rose-50 to-pink-100/80 hover:border-pink-300 hover:from-pink-100 hover:to-rose-100 transition-all duration-300 flex items-center justify-center group shadow-sm hover:shadow-md relative overflow-hidden"
              onClick={() => fileInputRef.current?.click()}
            >
              {/* Decorative elements */}
              <div className="absolute top-4 right-4 w-2 h-2 bg-pink-300 rounded-full"></div>
              <div className="absolute bottom-6 left-6 w-1.5 h-1.5 bg-rose-300 rounded-full"></div>
              
              {imagePreview ? (
                <img src={imagePreview} alt="Preview" className="w-full h-full object-cover rounded-lg relative z-10" />
              ) : (
                <div className="text-center px-4 z-10">
                  <div className="mx-auto mb-5 h-12 w-12 rounded-full bg-gradient-to-br from-pink-200 to-rose-200 flex items-center justify-center text-xl text-pink-600 shadow-sm group-hover:scale-110 transition-transform duration-300">↥</div>
                   <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Upload image</p>
                  <p className="text-xs text-muted mt-1">JPG or PNG · max 5MB</p>
                </div>
              )}
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageSelect}
              className="hidden"
            />
          </div>

          {/* Product Details Form Card */}
          <div className="card !p-4 h-full overflow-hidden flex flex-col relative">
            {/* Accent bar */}
            <div className="absolute top-0 left-0 w-1 h-full bg-gradient-to-b from-pink-300 via-rose-300 to-pink-300"></div>
            
              <h2 className="text-base text-pink mb-2 flex items-center gap-2 font-bold">
                <span className="text-lg">📝</span>
                Product Details
              </h2>

             <div className="grid grid-cols-1 md:grid-cols-2 gap-3 overflow-y-auto pr-2 flex-1">
                <div className="fg">
                  <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Product name *</p>
                 <input
                   type="text"
                   name="name"
                   value={formData.name}
                   onChange={handleChange}
                   placeholder="e.g. Hydrating Face Cream"
                   required
                   className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow placeholder:text-muted/60"
                 />
               </div>
                <div className="fg">
                  <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Category</p>
                 <select
                   name="category"
                   value={formData.category}
                   onChange={handleChange}
                   className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow placeholder:text-muted/60"
                 >
                   {categories.map((c) => (
                     <option key={c} value={c}>{c}</option>
                   ))}
                 </select>
               </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-3 overflow-y-auto pr-2 flex-1">
                <div className="fg">
                  <label className="text-xs font-normal text-dark">Brand *</label>
                 <input
                   type="text"
                   name="brand"
                   value={formData.brand}
                   onChange={handleChange}
                   placeholder="e.g. CeraVe"
                   required
                   className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow placeholder:text-muted/60"
                 />
                </div>
               <div className="fg">
                 <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Purchase Location</p>
                <input
                  type="text"
                  name="purchaseLocation"
                  value={formData.purchaseLocation}
                  onChange={handleChange}
                  placeholder="e.g. Watsons"
                  className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                />
              </div>
             </div>

            <div className="fg">
              <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Description</p>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Brief description of the product, key ingredients, skin type..."
                rows={3}
                className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow resize-none placeholder:text-muted/60"
              />
            </div>
          </div>
        </div>

        {/* Dates & Pricing Card */}
           <div className="card !p-4 flex flex-col justify-between min-h-[160px] shadow-md hover:shadow-lg transition-shadow duration-300 relative overflow-hidden">
          {/* Decorative top border */}
          <div className="absolute top-0 left-4 right-4 h-0.5 bg-gradient-to-r from-transparent via-pink-300 to-transparent"></div>
          
          <div>
            <h2 className="text-base text-pink mb-2 flex items-center gap-2 font-bold">
              <span className="text-lg">📅</span>
              Dates & Pricing
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
              <div className="fg">
                  <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Price (₱)</p>
                <input
                  type="number"
                  name="price"
                  value={formData.price}
                  onChange={handleChange}
                  placeholder="0.00"
                  step="0.01"
                  min="0"
                  className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow placeholder:text-muted/60"
                />
              </div>
                <div className="fg">
                  <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Expiry Date</p>
                  <input
                    type="date"
                    name="expirationDate"
                    value={formData.expirationDate}
                    onChange={handleChange}
                    className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                  />
                </div>
                <div className="fg">
                  <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Opened Date</p>
                  <input
                    type="date"
                    name="openedDate"
                    value={formData.openedDate}
                    onChange={handleChange}
                    className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                  />
                </div>
                <div className="fg">
                  <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Availability</p>
                  <select
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                  >
                    <option value="New">New</option>
                    <option value="running low">Running low</option>
                    <option value="out of stock">Out of stock</option>
                  </select>
                </div>
            </div>
          </div>

         <div className="mt-3 border-t border-pink-100 pt-3 flex items-center justify-between gap-4 flex-wrap">
  <p className="text-xs text-muted">* Required fields</p>

  <div className="flex gap-3">
    
    {/* CANCEL BUTTON */}
    <button
      type="button"
      onClick={() => navigate('/products')}
      className="px-4 py-2 rounded-xl border-2 border-pink-200 text-pink bg-white hover:bg-pink-50 hover:border-pink-300 font-semibold transition-all duration-200 text-sm"
    >
      Cancel
    </button>

    {/* SAVE BUTTON */}
    <button
      type="submit"
      disabled={isSubmitting}
      className="px-5 py-2 rounded-xl bg-gradient-to-r from-pink-500 to-rose-500 text-white font-semibold hover:from-pink-600 hover:to-rose-600 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm hover:shadow transition-all duration-200 relative text-sm"
    >
      {/* Spinner (left) */}
      {isSubmitting && (
        <span className="absolute left-4">⏳</span>
      )}

      {/* PERFECTLY CENTERED TEXT */}
      <span className="block w-full text-center">
        {isSubmitting ? 'Saving...' : 'Save product'}
      </span>
    </button>

  </div>
</div>
        </div>
      </form>
    </div>
  )
}
