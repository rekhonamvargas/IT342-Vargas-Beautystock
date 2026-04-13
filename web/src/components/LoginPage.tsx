import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '@/services/api'
import { useAuthStore } from '@/store/auth'
import { GoogleSignInButton } from './GoogleSignInButton'

export function LoginPage() {
  const navigate = useNavigate()
  const setUser = useAuthStore((state) => state.setUser)
  const [formData, setFormData] = useState({ email: '', password: '' })
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleGoogleSuccess = async (idToken: string) => {
    setError(null)
    try {
      setIsLoading(true)
      const response = await authApi.googleAuth(idToken)
      localStorage.setItem('token', response.data.token)
      setUser(response.data.user)
      navigate('/dashboard')
    } catch (err: any) {
      const msg =
        typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message || 'Google login failed'
      setError(msg)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    try {
      setIsLoading(true)
      const response = await authApi.login(formData)
      localStorage.setItem('token', response.data.token)
      setUser(response.data.user)
      navigate('/dashboard')
    } catch (err: any) {
      setError(err.response?.data || 'Login failed')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-cream px-4 py-8">
      <div className="w-full max-w-[420px] rounded-2xl border border-cream-200 bg-white px-5 py-6 shadow-[0_4px_24px_rgba(26,16,8,.06)]">
        {/* Logo */}
        <div className="text-center mb-5">
          <div className="flex items-center justify-center gap-1.5 mb-2">
            <span className="text-pink text-xs">✦</span>
            <span className="font-serif font-bold text-[#C88A1A] text-sm">BeautyStock</span>
          </div>
          <h2 className="font-serif text-[44px] leading-none font-bold text-dark">Welcome back</h2>
          <p className="text-muted text-[12px] mt-2">Your inventory is ready for you.</p>
        </div>

        {/* Google */}
        <GoogleSignInButton
          text="signin_with"
        />

        {error && (
          <div className="mt-3 p-2.5 bg-red-50 border border-red-100 rounded-lg text-red text-xs">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="mt-1 space-y-3">
          <div className="fg">
            <label className="text-[11px] font-semibold text-dark">Email Address</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              placeholder="rekhona@example.com"
              disabled={isLoading}
              required
              className="h-11 rounded-xl text-[13px]"
            />
          </div>
          <div className="fg">
            <label className="text-[11px] font-semibold text-dark">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="••••••••"
              disabled={isLoading}
              required
              className="h-11 rounded-xl text-[13px]"
            />
          </div>
          <div className="text-right pt-0.5">
            <button type="button" className="text-[11px] text-pink font-semibold hover:underline">
              Forgot password?
            </button>
          </div>
          <button type="submit" disabled={isLoading} className="btn-pink w-full h-11 rounded-xl text-sm">
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <p className="text-center text-muted text-[12px] mt-4">
          Don't have an account?{' '}
          <Link to="/register" className="text-pink font-semibold hover:underline">
            Create one
          </Link>
        </p>
      </div>
    </div>
  )
}
