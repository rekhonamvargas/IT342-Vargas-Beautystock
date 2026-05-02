import { useEffect, useRef, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { productApi, favoriteApi } from '@/services/api'
import { ProductImage } from './ProductImage'

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

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file && file.type.startsWith('image/')) {
      setImageFile(file)
      const reader = new FileReader()
      reader.onload = () => setImagePreview(reader.result as string)
      reader.readAsDataURL(file)
    }
  }

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.stopPropagation()
  }

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault()
    e.stopPropagation()
    const file = e.dataTransfer.files?.[0]
    if (file && file.type.startsWith('image/')) {
      setImageFile(file)
      const reader = new FileReader()
      reader.onload = () => setImagePreview(reader.result as string)
      reader.readAsDataURL(file)
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

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  if (!product) return null

  const availability = getAvailability(product.status)

  return (
    <div className="space-y-4 w-full max-w-7xl mx-auto px-4">
      <div className="flex items-center gap-2 text-base">
        <Link to="/products" className="text-pink font-semibold hover:underline">
          Products
        </Link>
        <span className="text-muted">/</span>
        <span className="text-muted font-semibold">{product.name}</span>
      </div>

      <div className="xl:min-h-[calc(100vh-200px)] xl:flex xl:items-stretch">
        <div className="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-4 items-stretch w-full">
          <div className="space-y-3 flex flex-col">
          {isEditing ? (
            // Image Upload Area (Edit Mode)
            <div className="w-full aspect-square bg-gradient-to-br from-pink/10 to-orange/10 border-2 border-dashed border-pink rounded-2xl flex flex-col items-center justify-center cursor-pointer hover:bg-pink/5 hover:border-pink transition-all group"
              onClick={() => fileInputRef.current?.click()}
              onDragOver={handleDragOver}
              onDrop={handleDrop}
            >
              <div className="text-center space-y-3">
                <div className="text-5xl group-hover:scale-110 transition-transform">📷</div>
                <div>
                  <p className="text-base font-bold text-dark">Click to upload</p>
                  <p className="text-xs text-muted">or drag and drop</p>
                </div>
                {imageFile && (
                  <div className="pt-2 border-t border-pink/30">
                    <p className="text-sm font-semibold text-pink">✓ {imageFile.name}</p>
                  </div>
                )}
                {imagePreview && !imageFile && (
                  <div className="pt-2 border-t border-pink/30">
                    <p className="text-xs text-emerald-700 font-semibold">✓ Image selected</p>
                  </div>
                )}
              </div>
            </div>
          ) : (
            // Image Preview Area (View Mode)
            <div className="w-full aspect-square bg-white rounded-2xl border border-cream-200 overflow-hidden shadow-sm">
              <ProductImage
                src={imagePreview}
                alt={product.name}
                className="w-full h-full"
                placeholderClassName="bg-white text-muted"
                placeholderText="NO IMAGE"
              />
            </div>
          )}

          <div className="card !p-3 rounded-2xl flex flex-col">
            {isEditing ? (
              <>
                <div className="flex flex-col gap-2">
                  <button
                    onClick={handleSaveChanges}
                    type="button"
                    className="btn-pink w-full h-12 text-base font-semibold"
                    disabled={isSaving}
                  >
                    {isSaving ? 'Saving...' : 'Save Changes'}
                  </button>
                  <button
                    onClick={() => {
                      setIsEditing(false)
                      if (product) resetEditStateFromProduct(product)
                    }}
                    type="button"
                    className="btn-outline w-full h-12 text-base"
                    disabled={isSaving}
                  >
                    Cancel
                  </button>
                </div>
              </>
            ) : (
              <>
                <div className="flex flex-col gap-2">
                  {actionError && (
                    <div className="p-2 bg-red-50 border border-red-100 rounded-lg text-red text-xs">
                      {actionError}
                    </div>
                  )}

                  <button
                    type="button"
                    onClick={toggleFavorite}
                    className="btn-outline w-full h-12 text-base"
                    disabled={isTogglingFavorite || isDeleting}
                  >
                    {isTogglingFavorite
                      ? 'Updating...'
                      : product.isFavorite
                        ? '❤️ Favorited'
                        : '♡ Add to favorites'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setIsEditing(true)
                      resetEditStateFromProduct(product)
                    }}
                    className="btn-pink w-full h-12 text-base font-semibold"
                    disabled={isDeleting}
                  >
                    ✎ Edit product
                  </button>
                  <button
                    onClick={handleDelete}
                    type="button"
                    className="w-full h-12 border-2 border-red/30 text-red rounded-xl text-base font-semibold hover:bg-red-50 transition-colors"
                    disabled={isDeleting}
                  >
                    {isDeleting ? 'Deleting...' : '🗑 Delete'}
                  </button>
                </div>
              </>
            )}
          </div>
          </div>

          <div className="card !p-6 rounded-2xl h-full flex flex-col overflow-hidden">
          {isEditing ? (
            <div className="space-y-3 overflow-y-auto flex-1">
              {editError && (
                <div className="p-3 bg-red-50 border border-red-100 rounded-lg text-red text-sm">
                  {editError}
                </div>
              )}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="fg">
                  <label>Name</label>
                  <input name="name" value={editForm.name} onChange={handleEditField} />
                </div>
                <div className="fg">
                  <label>Brand</label>
                  <input name="brand" value={editForm.brand} onChange={handleEditField} />
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="fg">
                  <label>Category</label>
                  <input name="category" value={editForm.category} onChange={handleEditField} />
                </div>
                <div className="fg">
                  <label>Price (₱)</label>
                  <input name="price" value={editForm.price} onChange={handleEditField} type="number" min="0" step="0.01" />
                </div>
              </div>
              <div className="fg">
                <label>Availability</label>
                <select name="status" value={editForm.status} onChange={handleEditField}>
                  <option value="">—</option>
                  <option value="In stock">In stock</option>
                  <option value="Running low">Running low</option>
                  <option value="Out of stock">Out of stock</option>
                </select>
              </div>
              <div className="fg">
                <label>Description</label>
                <textarea name="description" value={editForm.description} onChange={handleEditField} rows={3} />
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="fg">
                  <label>Purchase Location</label>
                  <input name="purchaseLocation" value={editForm.purchaseLocation} onChange={handleEditField} />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div className="fg">
                  <label>Expiration Date</label>
                  <input name="expirationDate" value={editForm.expirationDate} onChange={handleEditField} type="date" />
                </div>
                <div className="fg">
                  <label>Opened Date</label>
                  <input name="openedDate" value={editForm.openedDate} onChange={handleEditField} type="date" />
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
            <div className="flex flex-col space-y-4 flex-1 overflow-y-auto pr-2">
              <div className="flex flex-col items-start">
                <p className="text-sm text-muted font-semibold uppercase tracking-wide">{product.brand}</p>
                <h1 className="hello-title text-4xl md:text-5xl text-dark mt-1 leading-none">{product.name}</h1>
                <p className="text-2xl md:text-3xl font-semibold text-pink mt-4 leading-none">{formatPrice(product.price)}</p>
              </div>

              <div className="flex items-center gap-3 mt-4 mb-4 flex-wrap">
                <span className="px-4 py-2 rounded-full bg-pink text-white text-base font-semibold capitalize">{product.category}</span>
                {product.isExpired && (
                  <span className="px-4 py-2 rounded-full bg-red-50 text-red border border-red-200 text-base font-semibold">Expired</span>
                )}
                {product.isExpiringWithin15Days && !product.isExpired && (
                  <span className="px-4 py-2 rounded-full bg-orange-50 text-orange border border-orange-200 text-base font-semibold">Expiring soon</span>
                )}
              </div>

              <div className="border-t border-cream-200 pt-4" />

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-4">
                <div className="flex flex-col gap-2">
                  <p className="text-sm text-muted font-semibold">Expiry date</p>
                  <div className="rounded-xl border border-cream-200 p-3 bg-white h-[60px] flex items-center justify-center text-center">
                    <p className="text-sm font-medium text-orange leading-tight">{formatDate(product.expirationDate)}</p>
                  </div>
                </div>

                <div className="flex flex-col gap-2">
                  <p className="text-sm text-muted font-semibold">Opened date</p>
                  <div className="rounded-xl border border-cream-200 p-3 bg-white h-[60px] flex items-center justify-center text-center">
                    <p className="text-sm font-medium text-dark leading-tight">{formatDate(product.openedDate)}</p>
                  </div>
                </div>

                <div className="flex flex-col gap-2">
                  <p className="text-sm text-muted font-semibold">Purchase location</p>
                  <div className="rounded-xl border border-cream-200 p-3 bg-white h-[60px] flex items-center justify-center text-center">
                    <p className="text-sm font-medium text-dark leading-tight">{product.purchaseLocation || '—'}</p>
                  </div>
                </div>

                <div className="flex flex-col gap-2">
                  <p className="text-sm text-muted font-semibold">Availability</p>
                  <div className="rounded-xl border border-cream-200 p-3 bg-white h-[60px] flex items-center justify-center text-center">
                    <p className="text-sm font-medium text-emerald-700 leading-tight">{availability}</p>
                  </div>
                </div>

                <div className="md:col-span-2 mt-1">
                  <p className="text-sm text-muted font-semibold uppercase tracking-wide">Description</p>
                </div>
                <div className="rounded-xl border border-cream-200 p-3 bg-white md:col-span-2 h-[70px] overflow-y-auto flex flex-col items-start justify-start text-left">
                  <p className="text-sm font-normal leading-relaxed text-muted">{product.description || 'No description added yet.'}</p>
                </div>
              </div>
            </div>
          )}
          </div>
        </div>
      </div>
    </div>
  )
}
