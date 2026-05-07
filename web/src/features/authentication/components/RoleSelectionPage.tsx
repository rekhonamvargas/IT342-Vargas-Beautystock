import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'

export default function RoleSelectionPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()

  const [selectedRole, setSelectedRole] = useState<'YOUTH' | 'ADULT'>('ADULT')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const setUser = useAuthStore((state: any) => state.setUser)

  const handleConfirmRole = async () => {
    setLoading(true)
    setError(null)

    try {
      const token = searchParams.get('token')

      if (!token) {
        setError('No authentication token found')
        setLoading(false)
        return
      }

      const response = await fetch('/api/v1/auth/me/role', {
        method: 'PATCH',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          role: selectedRole === 'YOUTH' ? 'ROLE_YOUTH' : 'ROLE_ADULT',
        }),
      })

      const data = await response.json()

      if (!response.ok) {
        throw new Error(data?.message || 'Failed to update role')
      }

      // Save user
      setUser(data)

      // Redirect
      navigate('/dashboard', { replace: true })

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set role')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-pink-50/30 via-cream to-rose-50/20 px-4 py-6 relative overflow-hidden">

      {/* Background Effects */}
      <div className="absolute top-10 left-10 w-32 h-32 bg-pink-300/20 rounded-full blur-3xl animate-pulse"></div>
      <div className="absolute bottom-20 right-10 w-40 h-40 bg-rose-300/20 rounded-full blur-3xl animate-pulse"></div>

      <div className="w-full max-w-md rounded-3xl border-2 border-pink-200 bg-white px-6 py-6 shadow-lg">

        {/* Header */}
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-dark">Select Your Age Group</h1>
          <p className="text-muted text-sm mt-1">
            We'll personalize your experience
          </p>
        </div>

        {/* Error */}
        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-red text-sm">
            {error}
          </div>
        )}

        {/* OPTIONS */}
        <div className="space-y-3 mb-6">

          <button
            onClick={() => setSelectedRole('YOUTH')}
            className={`w-full p-4 rounded-xl border text-left ${
              selectedRole === 'YOUTH'
                ? 'border-pink bg-pink-50'
                : 'border-gray-200'
            }`}
          >
            🌱 <strong>Youth</strong>
          </button>

          <button
            onClick={() => setSelectedRole('ADULT')}
            className={`w-full p-4 rounded-xl border text-left ${
              selectedRole === 'ADULT'
                ? 'border-pink bg-pink-50'
                : 'border-gray-200'
            }`}
          >
            🌸 <strong>Adult</strong>
          </button>

        </div>

        {/* BUTTON */}
        <button
          onClick={handleConfirmRole}
          disabled={loading}
          className="w-full bg-pink text-white py-2 rounded-lg font-semibold"
        >
          {loading ? 'Setting up...' : 'Continue'}
        </button>

      </div>
    </div>
  )
}