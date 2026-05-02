import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'

export default function RoleSelectionPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [selectedRole, setSelectedRole] = useState('ADULT')
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
        return
      }

      // Update user role
      const response = await fetch('/api/v1/auth/me/role', {
        method: 'PATCH',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ role: selectedRole === 'YOUTH' ? 'ROLE_YOUTH' : 'ROLE_ADULT' }),
      })

      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData || 'Failed to update role')
      }

      // Get updated user info
      const userData = await response.json()
      setUser(userData)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to set role')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-cream via-rose-50 to-cream">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-2xl p-8 border-2 border-rose-100">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="text-4xl mb-3">✦</div>
            <h1 className="text-3xl font-serif text-dark mb-2">BeautyStock</h1>
            <p className="text-rose-600 font-medium">Select Your Age Group</p>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
              {error}
            </div>
          )}

          {/* Role Selection */}
          <div className="space-y-3 mb-8">
            {/* Youth Button */}
            <button
              onClick={() => setSelectedRole('YOUTH')}
              className={`w-full p-4 rounded-xl border-2 transition-all text-left ${
                selectedRole === 'YOUTH'
                  ? 'border-rose-400 bg-rose-50 shadow-lg'
                  : 'border-gray-200 bg-white hover:border-rose-200'
              }`}
            >
              <div className="flex items-center gap-3">
                <div className={`text-2xl ${selectedRole === 'YOUTH' ? 'scale-110' : ''}`}>
                  🌱
                </div>
                <div>
                  <div className="font-semibold text-dark">Youth</div>
                  <div className="text-sm text-gray-600">Age 13–24</div>
                </div>
                {selectedRole === 'YOUTH' && (
                  <div className="ml-auto text-rose-400">✓</div>
                )}
              </div>
            </button>

            {/* Adult Button */}
            <button
              onClick={() => setSelectedRole('ADULT')}
              className={`w-full p-4 rounded-xl border-2 transition-all text-left ${
                selectedRole === 'ADULT'
                  ? 'border-rose-400 bg-rose-50 shadow-lg'
                  : 'border-gray-200 bg-white hover:border-rose-200'
              }`}
            >
              <div className="flex items-center gap-3">
                <div className={`text-2xl ${selectedRole === 'ADULT' ? 'scale-110' : ''}`}>
                  🌸
                </div>
                <div>
                  <div className="font-semibold text-dark">Adult</div>
                  <div className="text-sm text-gray-600">Age 25–44</div>
                </div>
                {selectedRole === 'ADULT' && (
                  <div className="ml-auto text-rose-400">✓</div>
                )}
              </div>
            </button>
          </div>

          {/* Confirm Button */}
          <button
            onClick={handleConfirmRole}
            disabled={loading}
            className={`w-full py-3 rounded-xl font-semibold transition-all ${
              loading
                ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                : 'bg-gradient-to-r from-rose-500 to-pink-500 text-white hover:shadow-lg hover:scale-105'
            }`}
          >
            {loading ? 'Setting up...' : 'Continue to Dashboard'}
          </button>

          {/* Info Message */}
          <p className="text-center text-sm text-gray-600 mt-6">
            We'll personalize your skincare advice based on your age group
          </p>
        </div>
      </div>
    </div>
  )
}
