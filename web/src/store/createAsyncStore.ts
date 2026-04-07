// web/src/store/createAsyncStore.ts
// Factory Pattern: Creates consistent async stores with loading/error states

import { create } from 'zustand'

/**
 * Factory function for creating async data stores.
 * Applies consistent patterns for loading and error handling.
 * Eliminates code duplication across stores.
 */
export interface AsyncStoreState<T> {
  data: T | null
  isLoading: boolean
  error: string | null
}

export interface AsyncStoreActions<T> {
  setData: (data: T | null) => void
  setIsLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  reset: () => void
}

export type AsyncStore<T> = AsyncStoreState<T> & AsyncStoreActions<T>

/**
 * Factory to create async stores with consistent pattern.
 * 
 * @example
 * const useUserStore = createAsyncStore<User>('UserStore')
 */
export function createAsyncStore<T>(storeName: string) {
  return create<AsyncStore<T>>((set) => ({
    // Initial state
    data: null,
    isLoading: false,
    error: null,

    // Actions
    setData: (data) => set({ data }),
    setIsLoading: (isLoading) => set({ isLoading }),
    setError: (error) => set({ error }),
    reset: () => set({ data: null, isLoading: false, error: null }),
  }))
}
