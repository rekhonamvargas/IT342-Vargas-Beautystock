import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { authApi } from '@/services/api'
import { useAuthStore } from '@/store/auth'
import { GoogleSignInButton } from './GoogleSignInButton'

const ageGroups = [
  { value: 'YOUTH', label: '🌱 Youth', desc: 'Age 13–24' },
  { value: 'ADULT', label: '🌸 Adult', desc: 'Age 25–44' },
]

export function RegisterPage() {
  const navigate = useNavigate()
  const setUser = useAuthStore((state) => state.setUser)
  const [formData, setFormData] = useState({
    email: '',
    firstName: '',
    lastName: '',
    password: '',
    confirmPassword: '',
    ageRange: 'YOUTH',
  })
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match')
      return
    }
    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }

    try {
      setIsLoading(true)
      const response = await authApi.register(formData)
      localStorage.setItem('token', response.data.token)
      setUser(response.data.user)
      navigate('/dashboard')
    } catch (err: any) {
      const errorMessage =
        typeof err.response?.data === 'string'
          ? err.response.data
          : err.response?.data?.message || err.message || 'Registration failed'
      setError(errorMessage)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-pink-50/30 via-cream to-rose-50/20 px-4 py-6 relative overflow-hidden">
      {/* Decorative glowing orbs */}
      <div className="absolute top-10 left-10 w-32 h-32 bg-pink-300/20 rounded-full blur-3xl animate-pulse"></div>
      <div className="absolute bottom-20 right-10 w-40 h-40 bg-rose-300/20 rounded-full blur-3xl animate-pulse" style={{animationDelay: '1s'}}></div>
      <div className="absolute top-1/2 left-1/4 w-24 h-24 bg-yellow-200/10 rounded-full blur-2xl animate-pulse" style={{animationDelay: '2s'}}></div>
      
      <div className="w-full max-w-[520px] rounded-3xl border-2 border-pink-200 bg-gradient-to-br from-white/90 to-pink-50/50 backdrop-blur-sm px-6 py-6 shadow-[0_0_40px_rgba(236,72,153,0.15)] hover:shadow-[0_0_60px_rgba(236,72,153,0.25)] transition-shadow duration-300 relative overflow-hidden">
        {/* Glowing accent line */}
        <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-pink-400 to-transparent opacity-60"></div>
        
        {/* Logo */}
        <div className="text-center mb-5">
          <div className="flex items-center justify-center gap-1.5 mb-2">
            <span className="text-pink text-xs">✦</span>
            <span className="hello-title text-sm text-[#C88A1A]">BeautyStock</span>
          </div>
          <h2 className="font-serif text-[36px] leading-none italic text-dark">Create Account</h2>
          <p className="text-muted text-xs mt-2 font-medium">Join and start tracking your beauty collection</p>
         </div>

        {/* Google Sign-In */}
        <div className="mb-4">
          <GoogleSignInButton
            text="signup_with"
          />
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border-2 border-red-200 rounded-xl text-red text-sm flex items-center gap-2 animate-fade-in">
            <span>⚠️</span> {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="mt-3 space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div className="fg">
              <label className="text-xs font-semibold text-dark">First Name</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                placeholder="First"
                disabled={isLoading}
                required
                className="w-full px-3 py-2 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all text-sm"
              />
            </div>
            <div className="fg">
              <label className="text-xs font-semibold text-dark">Last Name</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                placeholder="Last"
                disabled={isLoading}
                required
                className="w-full px-3 py-2 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all text-sm"
              />
            </div>
          </div>

          <div className="fg">
            <label className="text-xs font-semibold text-dark">Email Address</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              placeholder="your@email.com"
              disabled={isLoading}
              required
              className="w-full px-3 py-2 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all text-sm placeholder:text-muted/60"
            />
          </div>

          <div className="fg">
            <label className="text-xs font-semibold text-dark">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="Min 8 characters"
              disabled={isLoading}
              required
              className="w-full px-3 py-2 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all text-sm placeholder:text-muted/60"
            />
          </div>

          <div className="fg">
            <label className="text-xs font-semibold text-dark">Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              placeholder="Enter your password"
              disabled={isLoading}
              required
              className="w-full px-3 py-2 border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white transition-all text-sm placeholder:text-muted/60"
            />
          </div>

          {/* Age Group Tiles */}
          <div className="fg">
            <label className="text-xs font-semibold text-dark">Age Group</label>
            <div className="grid grid-cols-2 gap-3">
              {ageGroups.map((ag) => (
                <button
                  key={ag.value}
                  type="button"
                  onClick={() => setFormData((prev) => ({ ...prev, ageRange: ag.value }))}
                  className={`p-2.5 rounded-xl border-2 text-center transition-all ${
                    formData.ageRange === ag.value
                      ? 'border-pink bg-pink-50 shadow-sm'
                      : 'border-pink-100 bg-white hover:border-pink-200 hover:shadow-sm'
                  }`}
                >
                  <div className="text-sm font-semibold">{ag.label}</div>
                  <div className="text-[11px] text-muted mt-0.5">{ag.desc}</div>
                </button>
              ))}
            </div>
          </div>

          <button 
            type="submit" 
            disabled={isLoading} 
            className="btn-pink w-full h-10 rounded-xl text-sm font-semibold shadow-sm hover:shadow-md transition-all duration-200 mt-2"
          >
            {isLoading ? '⏳ Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="mt-4 pt-4 border-t border-pink-100">
          <p className="text-center text-muted text-sm">
            Already have an account?{' '}
            <Link to="/login" className="text-pink font-semibold hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
