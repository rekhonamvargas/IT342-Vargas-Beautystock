import { useEffect, useRef, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { productApi, favoriteApi } from '@/services/api'
import { ProductImage } from './ProductImage'

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

const categories = ['Skincare', 'Makeup', 'Hair Care', 'Body Care', 'Fragrance', 'Tools', 'Other']

interface Product {
  id: number
  name: string
  brand: string
  category: string
  description: string
  price: number
  purchaseLocation: string
  imageUrl: string | null
  expirationDate: string | null
  openedDate: string | null
  status?: string | null
  isExpired: boolean
  isFavorite: boolean
  isExpiringWithin15Days: boolean
}

export function ProductDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [isEditing, setIsEditing] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [editError, setEditError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [isTogglingFavorite, setIsTogglingFavorite] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imagePreview, setImagePreview] = useState<string | null>(null)
  const [editForm, setEditForm] = useState({
    name: '',
    brand: '',
    category: '',
    description: '',
    price: '',
    purchaseLocation: '',
    expirationDate: '',
    openedDate: '',
    status: '',
  })

  useEffect(() => {
    if (id) loadProduct(parseInt(id))
  }, [id])

  const getErrorMessage = (err: any, fallback: string) => {
    const status = err?.response?.status
    const data = err?.response?.data
    const msg =
      (typeof data === 'string' && data) ||
      data?.message ||
      data?.error ||
      err?.message ||
      fallback

    return status ? `${msg} (HTTP ${status})` : msg
  }

  const loadProduct = async (productId: number) => {
    try {
      setLoading(true)
      const res = await productApi.getById(productId)
      setProduct(res.data)
      setEditForm({
        name: res.data.name || '',
        brand: res.data.brand || '',
        category: res.data.category || 'Skincare',
        description: res.data.description || '',
        price: res.data.price?.toString() || '',
        purchaseLocation: res.data.purchaseLocation || '',
        expirationDate: res.data.expirationDate ? res.data.expirationDate.slice(0, 10) : '',
        openedDate: res.data.openedDate ? res.data.openedDate.slice(0, 10) : '',
        status: res.data.status || '',
      })
      setImagePreview(res.data.imageUrl || null)
      setImageFile(null)
      setEditError(null)
      setActionError(null)
    } catch (err) {
      console.error('Failed to load product:', err)
      navigate('/products')
    } finally {
      setLoading(false)
    }
  }

  const resetEditStateFromProduct = (nextProduct: Product) => {
    setEditForm({
      name: nextProduct.name || '',
      brand: nextProduct.brand || '',
      category: nextProduct.category || 'Skincare',
      description: nextProduct.description || '',
      price: nextProduct.price?.toString() || '',
      purchaseLocation: nextProduct.purchaseLocation || '',
      expirationDate: nextProduct.expirationDate ? nextProduct.expirationDate.slice(0, 10) : '',
      openedDate: nextProduct.openedDate ? nextProduct.openedDate.slice(0, 10) : '',
      status: nextProduct.status || '',
    })
    setImagePreview(nextProduct.imageUrl || null)
    setImageFile(null)
    setEditError(null)
    setActionError(null)
  }

  const toggleFavorite = async () => {
    if (!product) return
    try {
      if (isTogglingFavorite || isDeleting || isSaving) return
      setActionError(null)
      setIsTogglingFavorite(true)

      const nextIsFavorite = !product.isFavorite
      // Optimistic UI update (immediately reflect change)
      setProduct((prev) => (prev ? { ...prev, isFavorite: nextIsFavorite } : prev))

      if (nextIsFavorite) {
        await favoriteApi.add(product.id)
      } else {
        await favoriteApi.remove(product.id)
      }
    } catch (err) {
      console.error('Failed to toggle favorite:', err)
      // Roll back optimistic update
      setProduct((prev) => (prev ? { ...prev, isFavorite: !prev.isFavorite } : prev))
      setActionError(getErrorMessage(err, 'Failed to update favorites. Please try again.'))
    } finally {
      setIsTogglingFavorite(false)
    }
  }

  const handleDelete = async () => {
    if (!product || !confirm('Are you sure you want to delete this product?')) return
    try {
      if (isDeleting || isSaving || isTogglingFavorite) return
      setActionError(null)
      setIsDeleting(true)
      await productApi.delete(product.id)
      // Replace so browser back won't return to a deleted detail page.
      navigate('/products', { replace: true })
    } catch (err) {
      console.error('Failed to delete product:', err)
      setActionError(getErrorMessage(err, 'Failed to delete product. Please try again.'))
    } finally {
      setIsDeleting(false)
    }
  }

   const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
     const file = e.target.files?.[0]
     if (file && file.type.startsWith('image/')) {
       try {
         const enhancedFile = await enhanceImage(file)
         setImageFile(enhancedFile)
         const reader = new FileReader()
         reader.onload = () => setImagePreview(reader.result as string)
         reader.readAsDataURL(enhancedFile)
       } catch (err) {
         console.error('Image enhancement failed:', err)
         setImageFile(file)
         const reader = new FileReader()
         reader.onload = () => setImagePreview(reader.result as string)
         reader.readAsDataURL(file)
       }
     }
   }

   const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
     e.preventDefault()
     e.stopPropagation()
   }

   const handleDrop = async (e: React.DragEvent<HTMLDivElement>) => {
     e.preventDefault()
     e.stopPropagation()
     const file = e.dataTransfer.files?.[0]
     if (file && file.type.startsWith('image/')) {
       try {
         const enhancedFile = await enhanceImage(file)
         setImageFile(enhancedFile)
         const reader = new FileReader()
         reader.onload = () => setImagePreview(reader.result as string)
         reader.readAsDataURL(enhancedFile)
       } catch (err) {
         console.error('Image enhancement failed:', err)
         setImageFile(file)
         const reader = new FileReader()
         reader.onload = () => setImagePreview(reader.result as string)
         reader.readAsDataURL(file)
       }
     }
   }

  const handleEditField = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target
    setEditForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSaveChanges = async () => {
    if (!product) return
    setEditError(null)
    if (!editForm.name.trim() || !editForm.brand.trim()) {
      setEditError('Product name and brand are required.')
      return
    }

    try {
      setIsSaving(true)
      const payload = {
        name: editForm.name,
        brand: editForm.brand,
        category: editForm.category,
        description: editForm.description,
        price: parseFloat(editForm.price) || 0,
        purchaseLocation: editForm.purchaseLocation,
        expirationDate: editForm.expirationDate || null,
        openedDate: editForm.openedDate || null,
        status: editForm.status || null,
      }

      try {
        await productApi.update(product.id, payload)
      } catch (updateErr: any) {
        // Some backend versions may not accept the `status` field yet.
        const shouldRetryWithoutStatus = updateErr?.response?.status === 400 || updateErr?.response?.status === 422
        if (!shouldRetryWithoutStatus) throw updateErr

        const { status, ...payloadWithoutStatus } = payload as any
        await productApi.update(product.id, payloadWithoutStatus)
      }

      if (imageFile) {
        try {
          console.log('Uploading image:', imageFile.name, 'Size:', imageFile.size)
          const uploadResult = await productApi.uploadImage(product.id, imageFile)
          console.log('Image upload successful:', uploadResult)
        } catch (uploadErr) {
          console.error('Image upload failed:', uploadErr)
          setEditError(getErrorMessage(uploadErr, 'Failed to upload image. Please try again.'))
          setIsSaving(false)
          return
        }
      }

      await loadProduct(product.id)
      setIsEditing(false)
    } catch (err) {
      console.error('Failed to update product:', err)
      setEditError(getErrorMessage(err, 'Failed to save changes. Please try again.'))
    } finally {
      setIsSaving(false)
    }
  }

  const formatPrice = (price: number) =>
    new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(price)

  const formatDate = (date: string | null) => {
    if (!date) return '—'
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  }

  const getAvailability = (status?: string | null) => {
    const normalized = (status || '').toLowerCase()
    if (normalized.includes('out of stock')) return 'Out of stock'
    if (normalized.includes('running')) return 'Running low'
    return 'In stock'
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

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="text-center">
          <div className="w-10 h-10 border-2 border-pink border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-muted text-sm">Loading product...</p>
        </div>
      </div>
    )
  }

  if (!product) return null

  const availability = getAvailability(product.status)

  return (
    <div className="space-y-4 w-full max-w-7xl mx-auto px-4 relative">
      {/* Decorative elements */}
      <div className="absolute top-6 left-4 text-pink opacity-10 animate-bounce" style={{animationDuration: '3s'}}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
        </svg>
      </div>

      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm">
        <Link to="/products" className="text-pink font-semibold hover:underline flex items-center gap-1">
          <span>←</span> Products
        </Link>
        <span className="text-muted">/</span>
        <span className="text-muted font-medium truncate max-w-[200px]">{product.name}</span>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-4 items-stretch w-full">
          <div className="space-y-3 flex flex-col">
            {isEditing ? (
              // Image Upload Area (Edit Mode)
              <div 
                className="w-full aspect-square bg-gradient-to-br from-pink-50 via-rose-50 to-pink-100 border-2 border-dashed border-pink-200 rounded-2xl flex flex-col items-center justify-center cursor-pointer hover:bg-pink-100 hover:border-pink-300 transition-all duration-300 group shadow-sm hover:shadow-md relative overflow-hidden"
                onClick={() => fileInputRef.current?.click()}
                onDragOver={handleDragOver}
                onDrop={handleDrop}
              >
                {/* Decorative dots */}
                <div className="absolute top-4 right-4 w-2 h-2 bg-pink-300 rounded-full"></div>
                <div className="absolute bottom-6 left-6 w-1.5 h-1.5 bg-rose-300 rounded-full"></div>
                <div className="absolute top-1/2 left-4 w-1 h-1 bg-pink-200 rounded-full"></div>
                
                <div className="text-center space-y-3 z-10">
                  <div className="text-6xl group-hover:scale-125 transition-transform duration-300">📷</div>
                  <div>
                    <p className="text-lg font-bold text-dark">Click to upload</p>
                    <p className="text-sm text-muted">or drag and drop</p>
                  </div>
                  {imageFile && (
                    <div className="pt-3 border-t border-pink-200">
                      <p className="text-sm font-semibold text-pink flex items-center justify-center gap-1">
                        <span>✓</span> {imageFile.name}
                      </p>
                    </div>
                  )}
                  {imagePreview && !imageFile && (
                    <div className="pt-3 border-t border-pink-200">
                      <p className="text-sm text-emerald-600 font-semibold">✓ Image selected</p>
                    </div>
                  )}
                </div>
              </div>
            ) : (
              // Image Preview Area (View Mode)
              <div className="w-full aspect-square bg-gradient-to-br from-pink-50/30 to-rose-50/30 rounded-2xl border border-pink-100 overflow-hidden shadow-lg hover:shadow-xl transition-shadow duration-300 relative group">
                {/* Decorative corner */}
                <div className="absolute top-0 right-0 w-16 h-16 bg-gradient-to-br from-pink-200/50 to-transparent rounded-bl-full"></div>
                
                <ProductImage
                  src={imagePreview}
                  alt={product.name}
                  className="w-full h-full"
                  placeholderClassName="bg-white/80 text-muted"
                  placeholderText="NO IMAGE"
                />
                
                {!imagePreview && (
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="text-6xl opacity-30">📦</div>
                  </div>
                )}
              </div>
            )}

            {/* Action Buttons Card */}
            <div className="card !p-4 rounded-2xl shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col relative overflow-hidden">
              {/* Accent bar */}
              <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-pink-300 via-rose-300 to-pink-300"></div>
              
              {isEditing ? (
                <div className="flex flex-col gap-2.5">
                  <button
                    onClick={handleSaveChanges}
                    type="button"
                    className="btn-pink w-full h-11 text-sm font-semibold shadow-sm hover:shadow-md transition-all duration-200 flex items-center justify-center gap-2"
                    disabled={isSaving}
                  >
                    <span>{isSaving ? '⏳' : ''}</span>
                    {isSaving ? 'Saving...' : 'Save Changes'}
                  </button>
                  <button
                    onClick={() => {
                      setIsEditing(false)
                      if (product) resetEditStateFromProduct(product)
                    }}
                    type="button"
                    className="btn-outline w-full h-11 text-sm border-pink-200 text-pink hover:bg-pink-50 font-semibold transition-all duration-200"
                    disabled={isSaving}
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  {actionError && (
                    <div className="p-2.5 bg-red-50 border border-red-200 rounded-xl text-red text-xs flex items-center gap-1.5 animate-fade-in">
                      <span>⚠️</span> {actionError}
                    </div>
                  )}

                  <button
                    type="button"
                    onClick={toggleFavorite}
                    className={`w-full h-11 text-sm font-semibold rounded-xl transition-all duration-200 flex items-center justify-center gap-2 ${
                      product.isFavorite
                        ? 'bg-gradient-to-r from-rose-50 to-pink-50 border-2 border-rose-200 text-rose-700 hover:from-rose-100 hover:to-pink-100'
                        : 'border-2 border-pink-200 text-pink hover:bg-pink-50'
                    }`}
                    disabled={isTogglingFavorite || isDeleting}
                  >
                    <span>{isTogglingFavorite ? '⏳' : product.isFavorite ? '❤️' : '♡'}</span>
                    {isTogglingFavorite
                      ? 'Updating...'
                      : product.isFavorite
                        ? 'Favorited'
                        : 'Add to favorites'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setIsEditing(true)
                      resetEditStateFromProduct(product)
                    }}
                    className="btn-pink w-full h-11 text-sm font-semibold shadow-sm hover:shadow-md transition-all duration-200 flex items-center justify-center gap-2"
                    disabled={isDeleting}
                  >
                    <span>✎</span> Edit product
                  </button>
                  <button
                    onClick={handleDelete}
                    type="button"
                    className="w-full h-11 border-2 border-red-200 text-red rounded-xl text-sm font-semibold hover:bg-red-50 hover:border-red-300 transition-all duration-200 flex items-center justify-center gap-2"
                    disabled={isDeleting}
                  >
                    <span>{isDeleting ? '⏳' : '🗑'}</span>
                    {isDeleting ? 'Deleting...' : 'Delete product'}
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Product Details Card */}
           <div className="card !p-6 rounded-2xl shadow-md hover:shadow-lg transition-shadow duration-300 flex flex-col relative overflow-hidden">
            {/* Decorative accent */}
            <div className="absolute top-0 left-0 w-1 h-full bg-gradient-to-b from-pink-300 via-rose-300 to-pink-300"></div>
            
             {isEditing ? (
               <div className="space-y-3 pr-2">
                {editError && (
                  <div className="p-3 bg-red-50 border border-red-200 rounded-xl text-red text-sm flex items-center gap-2 animate-fade-in">
                    <span>⚠️</span> {editError}
                  </div>
                )}
                 <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                   <div className="fg">
                     <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Product name *</p>
                    <input 
                      name="name" 
                      value={editForm.name} 
                      onChange={handleEditField}
                      className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                      placeholder="e.g. Hydrating Face Cream"
                      required
                    />
                  </div>
                   <div className="fg">
                      <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Brand *</p>
                    <input 
                      name="brand" 
                      value={editForm.brand} 
                      onChange={handleEditField}
                      className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                      placeholder="e.g. CeraVe"
                      required
                    />
                  </div>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                   <div className="fg">
                     <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Category</p>
                    <select 
                      name="category" 
                      value={editForm.category} 
                      onChange={handleEditField}
                      className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                    >
                      {categories.map((c) => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                    </select>
                  </div>
                   <div className="fg">
                     <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Price (₱)</p>
                    <input 
                      name="price" 
                      value={editForm.price} 
                      onChange={handleEditField}
                      className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                      placeholder="0.00"
                      type="number"
                      min="0"
                      step="0.01"
                    />
                  </div>
                </div>
                 <div className="fg">
                   <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Availability</p>
                  <select 
                    name="status" 
                    value={editForm.status} 
                    onChange={handleEditField}
                    className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                  >
                    <option value="">—</option>
                    <option value="In stock">In stock</option>
                    <option value="Running low">Running low</option>
                    <option value="Out of stock">Out of stock</option>
                  </select>
                </div>
                 <div className="fg">
                   <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Description</p>
                  <textarea 
                    name="description" 
                    value={editForm.description} 
                    onChange={handleEditField}
                    className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow resize-none"
                    placeholder="Brief description of the product, key ingredients, skin type..."
                    rows={3}
                  />
                </div>
                 <div className="fg">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Purchase Location</p>
                  <input 
                    name="purchaseLocation" 
                    value={editForm.purchaseLocation} 
                    onChange={handleEditField}
                    className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                    placeholder="e.g. Watsons"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                 <div className="fg">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Expiration Date</p>
                    <input 
                      name="expirationDate" 
                      value={editForm.expirationDate} 
                      onChange={handleEditField}
                      className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                      type="date"
                    />
                  </div>
                 <div className="fg">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Opened Date</p>
                    <input 
                      name="openedDate" 
                      value={editForm.openedDate} 
                      onChange={handleEditField}
                      className="w-full px-3 py-2 text-sm border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-shadow"
                      type="date"
                    />
                  </div>
                </div>

                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={handleImageSelect}
                />
              </div>
             ) : (
               <div className="flex flex-col space-y-4 pr-2">
                 {/* Product Title Section */}
                 <div className="flex flex-col items-start pb-4 border-b border-pink-100">
                   <div className="flex items-center gap-2 mb-2">
                     <span className="text-lg">✨</span>
                     <p className="text-sm text-muted font-semibold uppercase tracking-wider">{product.brand}</p>
                   </div>
                    <h1 className="hello-title text-pink text-3xl md:text-4xl leading-none">
                      <span className="hello-wave-wrap">{renderWavingText(product.name)}</span>
                    </h1>
                    <p className="text-muted text-xs uppercase tracking-wider mt-3 font-normal font-serif">{formatPrice(product.price)}</p>
                 </div>

                 {/* Badges */}
                 <div className="flex items-center gap-2 flex-wrap">
                   <span className="px-4 py-1.5 rounded-full bg-gradient-to-r from-pink-100 to-rose-100 text-pink-700 text-sm font-semibold border border-pink-200 capitalize shadow-sm">
                     {product.category}
                   </span>
                   {product.isExpired && (
                     <span className="px-4 py-1.5 rounded-full bg-red-50 text-red-600 border border-red-200 text-sm font-semibold flex items-center gap-1">
                       <span>⛔</span> Expired
                     </span>
                   )}
                   {product.isExpiringWithin15Days && !product.isExpired && (
                     <span className="px-4 py-1.5 rounded-full bg-orange-50 text-orange-600 border border-orange-200 text-sm font-semibold flex items-center gap-1">
                       <span>⏰</span> Expiring soon
                     </span>
                   )}
                 </div>

                 {/* Details Grid */}
                 <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-3">
                  <div className="flex flex-col gap-1.5">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Expiry Date</p>
                     <div className="rounded-xl border-2 border-pink-100 bg-gradient-to-br from-pink-50/50 to-white p-3 h-[60px] flex items-center justify-center text-center shadow-sm">
                       <p className="text-sm font-semibold text-orange-600 leading-tight">{formatDate(product.expirationDate)}</p>
                     </div>
                   </div>

                  <div className="flex flex-col gap-1.5">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Opened Date</p>
                     <div className="rounded-xl border-2 border-pink-100 bg-gradient-to-br from-pink-50/50 to-white p-3 h-[60px] flex items-center justify-center text-center shadow-sm">
                       <p className="text-sm font-semibold text-dark leading-tight">{formatDate(product.openedDate)}</p>
                     </div>
                   </div>

                  <div className="flex flex-col gap-1.5">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Purchase Location</p>
                     <div className="rounded-xl border-2 border-pink-100 bg-gradient-to-br from-pink-50/50 to-white p-3 h-[60px] flex items-center justify-center text-center shadow-sm">
                       <p className="text-sm font-semibold text-dark leading-tight">{product.purchaseLocation || '—'}</p>
                     </div>
                   </div>

                  <div className="flex flex-col gap-1.5">
                    <p className="text-muted text-xs uppercase tracking-wider mb-1 font-normal font-serif">Availability</p>
                      <div className={`rounded-xl border-2 p-3 h-[60px] flex items-center justify-center text-center shadow-sm ${
                        availability === 'In stock' 
                          ? 'bg-gradient-to-br from-green-50 to-emerald-50 border-green-200'
                          : availability === 'Running low'
                          ? 'bg-gradient-to-br from-orange-50 to-amber-50 border-orange-200'
                          : 'bg-gradient-to-br from-red-50 to-rose-50 border-red-200'
                      }`}>
                       <p className={`text-sm font-semibold leading-tight ${
                         availability === 'In stock' 
                           ? 'text-green-600'
                           : availability === 'Running low'
                           ? 'text-orange-600'
                           : 'text-red-600'
                       }`}>{availability}</p>
                     </div>
                   </div>
                 </div>

                   {/* Description */}
                   <div className="mt-2">
                     <p className="text-pink text-lg mb-2 font-normal font-serif">Description</p>
                   <div className="rounded-xl border-2 border-pink-100 bg-gradient-to-br from-pink-50/30 to-white p-4 min-h-[100px] flex flex-col items-start justify-start shadow-sm">
                     <p className="text-sm leading-relaxed text-dark/80 font-medium">
                       {product.description || 'No description added yet.'}
                     </p>
                   </div>
                 </div>
               </div>
             )}
          </div>
        </div>
      </div>
  )
}
