import { useState } from 'react'

interface ProductImageProps {
  src?: string | null
  alt: string
  className?: string
  imageClassName?: string
  placeholderClassName?: string
  placeholderText?: string
}

export function ProductImage({
  src,
  alt,
  className = '',
  imageClassName = 'w-full h-full object-cover',
  placeholderClassName = 'bg-cream text-muted',
  placeholderText = 'No image',
}: ProductImageProps) {
  const [hasImageError, setHasImageError] = useState(false)

  if (src && !hasImageError) {
    return (
      <img
        src={src}
        alt={alt}
        className={imageClassName}
        onError={() => setHasImageError(true)}
      />
    )
  }

  return (
    <div className={`flex items-center justify-center ${placeholderClassName} ${className}`}>
      <span className="text-[10px] font-semibold uppercase tracking-[0.16em]">{placeholderText}</span>
    </div>
  )
}