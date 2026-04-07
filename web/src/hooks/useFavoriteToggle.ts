// web/src/hooks/useFavoriteToggle.ts
// Custom Hook Pattern: Encapsulates favorite toggling logic

import { useState } from 'react'
import { ErrorHandler } from '@/services/ErrorHandler'

interface UseFavoriteToggleOptions {
  onSuccess?: (isFavorite: boolean) => void
  onError?: (error: string) => void
}

/**
 * Hook for handling favorite toggle with optimistic updates.
 * Reduces code duplication across product-related components.
 * 
 * @example
 * const { isFavorite, toggleFavorite, isLoading } = useFavoriteToggle(
 *   productId,
 *   initialIsFavorite,
 *   { onSuccess: () => console.log('Updated!') }
 * )
 */
export function useFavoriteToggle(
  productId: number,
  initialIsFavorite: boolean,
  addFn: (id: number) => Promise<any>,
  removeFn: (id: number) => Promise<any>,
  options: UseFavoriteToggleOptions = {}
) {
  const { onSuccess, onError } = options

  const [isFavorite, setIsFavorite] = useState(initialIsFavorite)
  const [isLoading, setIsLoading] = useState(false)

  const toggleFavorite = async () => {
    try {
      setIsLoading(true)

      // Optimistic update
      const nextIsFavorite = !isFavorite
      setIsFavorite(nextIsFavorite)

      // Call API
      if (nextIsFavorite) {
        await addFn(productId)
      } else {
        await removeFn(productId)
      }

      // Success callback
      onSuccess?.(nextIsFavorite)
    } catch (err) {
      // Revert optimistic update on error
      setIsFavorite(!isFavorite)
      const message = ErrorHandler.getMessage(err)
      onError?.(message)
    } finally {
      setIsLoading(false)
    }
  }

  return {
    isFavorite,
    toggleFavorite,
    isLoading,
  }
}
