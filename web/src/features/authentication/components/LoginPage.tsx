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
      setError(err.response?.data?.message || err.message || 'Login failed')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-pink-50/30 via-cream to-rose-50/20 px-4 py-8 relative overflow-hidden">
      {/* Decorative glowing orbs */}
      <div className="absolute top-10 left-10 w-32 h-32 bg-pink-300/20 rounded-full blur-3xl animate-pulse"></div>
      <div className="absolute bottom-20 right-10 w-40 h-40 bg-rose-300/20 rounded-full blur-3xl animate-pulse" style={{animationDelay: '1s'}}></div>
      <div className="absolute top-1/2 left-1/4 w-24 h-24 bg-yellow-200/10 rounded-full blur-2xl animate-pulse" style={{animationDelay: '2s'}}></div>
      
      <div className="w-full max-w-[420px] rounded-3xl border-2 border-pink-200 bg-gradient-to-br from-white/90 to-pink-50/50 backdrop-blur-sm px-8 py-8 shadow-[0_0_40px_rgba(236,72,153,0.15)] hover:shadow-[0_0_60px_rgba(236,72,153,0.25)] transition-shadow duration-300 relative overflow-hidden">
        {/* Glowing accent line */}
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-pink-400 to-transparent opacity-60"></div>
        {/* Logo */}
        <div className="text-center mb-6">
          <div className="flex items-center justify-center gap-1.5 mb-2">
            <span className="text-pink text-xs">✦</span>
            <span className="hello-title text-sm text-[#C88A1A]">BeautyStock</span>
          </div>
          <h2 className="font-serif text-[44px] leading-none italic text-dark">Welcome back</h2>
          <p className="text-muted text-xs mt-2 font-medium">Your inventory is ready for you.</p>
        </div>

        {/* Google */}
        <GoogleSignInButton
          text="signin_with"
        />

        {/* Form */}
        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {error && (
            <div className="p-3 bg-red-50 border-2 border-red-200 rounded-xl text-red text-sm flex items-center gap-2 animate-fade-in">
              <span>⚠️</span> {error}
            </div>
          )}
          
          <div className="space-y-4">
            <div className="fg">
              <label className="text-sm font-semibold text-dark">Email Address</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="yourname@example.com"
                disabled={isLoading}
                required
                className="w-full px-4 py-2.5 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all placeholder:text-muted/60"
              />
            </div>
            <div className="fg">
              <label className="text-sm font-semibold text-dark">Password</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                placeholder="Enter your password"
                disabled={isLoading}
                required
                className="w-full px-4 py-2.5 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all placeholder:text-muted/60"
              />
            </div>
          </div>
          
          <div className="text-right">
            <button type="button" className="text-sm text-pink hover:text-pink-700 font-semibold hover:underline transition-colors">
              Forgot password?
            </button>
          </div>
          
          <button 
            type="submit" 
            disabled={isLoading} 
            className="btn-pink w-full h-11 rounded-xl text-sm font-semibold shadow-sm hover:shadow-md transition-all duration-200"
          >
            {isLoading ? '⏳ Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="mt-5 pt-5 border-t border-pink-100">
          <p className="text-center text-muted text-sm">
            Don't have an account?{' '}
            <Link to="/register" className="text-pink font-semibold hover:underline">
              Create one
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
