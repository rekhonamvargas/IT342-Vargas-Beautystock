// web/src/hooks/useAsyncData.ts
// Custom Hook Pattern: Encapsulates data fetching logic

import { useEffect, useState } from 'react'
import { ErrorHandler } from '@/services/ErrorHandler'

interface UseAsyncDataOptions {
  skip?: boolean
  retryCount?: number
  cacheKey?: string
}

/**
 * Hook for handling async data fetching with loading/error states.
 * Eliminates repetitive data fetching logic in components.
 * 
 * @example
 * const { data, isLoading, error, refetch } = useAsyncData(
 *   async () => productApi.getAll(),
 *   { cacheKey: 'products' }
 * )
 */
export function useAsyncData<T>(
  fetchFn: () => Promise<any>,
  options: UseAsyncDataOptions = {}
) {
  const { skip = false, retryCount = 0 } = options

  const [data, setData] = useState<T | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [retries, setRetries] = useState(0)

  const fetchData = async () => {
    if (skip) {
      setIsLoading(false)
      return
    }

    try {
      setIsLoading(true)
      setError(null)

      const response = await fetchFn()
      setData(response.data || response)
      setRetries(0)
    } catch (err) {
      const message = ErrorHandler.getMessage(err)
      setError(message)

      // Retry logic
      if (retries < retryCount) {
        setRetries(retries + 1)
        await new Promise((resolve) => setTimeout(resolve, 1000 * (retries + 1)))
        fetchData() // Retry
      }
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [skip])

  return {
    data,
    isLoading,
    error,
    refetch: fetchData,
  }
}
