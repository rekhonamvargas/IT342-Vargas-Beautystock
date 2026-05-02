import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

export default function OAuth2CallbackPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [error, setError] = useState<string | null>(null)
  const setUser = useAuthStore((state) => state.setUser)
  const setToken = useAuthStore((state) => state.setToken)

  useEffect(() => {
    const token = searchParams.get('token')
    const errorParam = searchParams.get('error')
    const isNewUser = searchParams.get('isNewUser') === 'true'

    if (errorParam) {
      setError(decodeURIComponent(errorParam))
      navigate('/login', { replace: true })
      return
    }

    if (token) {
      // Store token in localStorage and Zustand store
      localStorage.setItem('authToken', token)
      setToken(token)

      // If new user from Google OAuth, redirect to role selection
      if (isNewUser) {
        navigate(`/role-selection?token=${token}`, { replace: true })
      } else {
        // For existing users, go directly to dashboard
        fetchUserInfo(token)
      }
    } else {
      setError('No token received from OAuth2 provider')
      navigate('/login', { replace: true })
    }
  }, [searchParams, navigate, setUser, setToken])

  const fetchUserInfo = async (token: string) => {
    try {
      const response = await fetch('/api/v1/auth/me', {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error('Failed to fetch user info')
      }

      const userData = await response.json()
      setUser(userData)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to complete OAuth2 login')
      navigate('/login', { replace: true })
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-cream">
      <div className="text-center">
        <h1 className="text-2xl font-serif text-dark mb-4">Completing Sign In...</h1>
        <p className="text-dark/70">Processing your Google authentication</p>
        {error && <p className="text-red-600 mt-4">{error}</p>}
      </div>
    </div>
  )
}
