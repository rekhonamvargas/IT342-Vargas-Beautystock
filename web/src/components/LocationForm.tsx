import { useState } from 'react'
import { userApi } from '@/services/api'

interface LocationFormProps {
  onSuccess: () => void
}

export function LocationForm({ onSuccess }: LocationFormProps) {
  const [city, setCity] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    
    if (!city.trim()) {
      setError('Please enter a city name')
      return
    }

    try {
      setIsLoading(true)
      await userApi.updateLocation(city)
      setCity('')
      onSuccess()
    } catch (err) {
      setError('Failed to update location. Please try again.')
      console.error(err)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="card max-w-md mx-auto">
      <h3 className="text-xl font-semibold mb-4 text-rose-600">Set Your Location</h3>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="city" className="block text-sm font-medium text-gray-700 mb-2">
            City
          </label>
          <input
            type="text"
            id="city"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            placeholder="e.g., Cebu"
            className="input-field"
            disabled={isLoading}
          />
        </div>
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <button
          type="submit"
          disabled={isLoading}
          className="btn-primary w-full"
        >
          {isLoading ? 'Updating...' : 'Update Location'}
        </button>
      </form>
    </div>
  )
}
