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
    <div className="min-h-screen flex items-center justify-center bg-cream px-4 py-8">
      <div className="w-full max-w-[460px] rounded-2xl border border-cream-200 bg-white px-5 py-6 shadow-[0_4px_24px_rgba(26,16,8,.06)]">
        {/* Logo */}
        <div className="text-center mb-5">
          <div className="flex items-center justify-center gap-1.5 mb-2">
            <span className="text-pink text-xs">✦</span>
            <span className="font-serif font-bold text-[#C88A1A] text-sm">BeautyStock</span>
          </div>
          <h2 className="font-serif text-[40px] leading-none font-bold text-dark">Create Account</h2>
          <p className="text-muted text-[12px] mt-2">Join and start tracking your beauty collection</p>
        </div>

        {/* Google */}
        <GoogleSignInButton
          text="signup_with"
        />

        {error && (
          <div className="mt-3 p-2.5 bg-red-50 border border-red-100 rounded-lg text-red text-xs">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="mt-1 space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div className="fg">
              <label className="text-[11px] font-semibold text-dark">First Name</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleInputChange}
                placeholder="First"
                disabled={isLoading}
                required
                className="h-11 rounded-xl text-[13px]"
              />
            </div>
            <div className="fg">
              <label className="text-[11px] font-semibold text-dark">Last Name</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleInputChange}
                placeholder="Last"
                disabled={isLoading}
                required
                className="h-11 rounded-xl text-[13px]"
              />
            </div>
          </div>

          <div className="fg">
            <label className="text-[11px] font-semibold text-dark">Email Address</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              placeholder="your@email.com"
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
              placeholder="Min 8 characters"
              disabled={isLoading}
              required
              className="h-11 rounded-xl text-[13px]"
            />
          </div>

          <div className="fg">
            <label className="text-[11px] font-semibold text-dark">Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              placeholder="••••••••"
              disabled={isLoading}
              required
              className="h-11 rounded-xl text-[13px]"
            />
          </div>

          {/* Age Group Tiles */}
          <div className="fg">
            <label className="text-[11px] font-semibold text-dark">Age Group</label>
            <div className="grid grid-cols-2 gap-3">
              {ageGroups.map((ag) => (
                <button
                  key={ag.value}
                  type="button"
                  onClick={() => setFormData((prev) => ({ ...prev, ageRange: ag.value }))}
                  className={`p-3 rounded-xl border-2 text-center transition-all ${
                    formData.ageRange === ag.value
                      ? 'border-pink bg-pink-50'
                      : 'border-cream-200 bg-white hover:border-pink-200'
                  }`}
                >
                  <div className="text-sm font-semibold">{ag.label}</div>
                  <div className="text-[11px] text-muted mt-0.5">{ag.desc}</div>
                </button>
              ))}
            </div>
          </div>

          <button type="submit" disabled={isLoading} className="btn-pink w-full h-11 rounded-xl text-sm mt-1">
            {isLoading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p className="text-center text-muted text-[12px] mt-4">
          Already have an account?{' '}
          <Link to="/login" className="text-pink font-semibold hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
