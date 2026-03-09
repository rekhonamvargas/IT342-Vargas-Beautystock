import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '@/services/api'
import { useAuthStore } from '@/store/auth'
import { GoogleSignInButton } from './GoogleSignInButton'

const C = {
  bg: '#F2EDE8',
  card: '#FFFFFF',
  border: '#E8DDD5',
  pink: '#C94B6E',
  pinkBg: '#FAE8ED',
  gold: '#D4921E',
  dark: '#1A1008',
  muted: '#8A7A72',
}

export function LoginPage() {
  const navigate = useNavigate()
  const setUser = useAuthStore((state) => state.setUser)
  const [formData, setFormData] = useState({ email: '', password: '' })
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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
      const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.message || 'Google login failed'
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
      const raw = err.response?.data
      const msg = typeof raw === 'string' ? raw : raw?.message || 'Invalid email or password'
      setError(msg)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div style={{ background: C.bg, minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '2.5rem 1rem', fontFamily: "'Nunito', sans-serif" }}>
      <div style={{ background: C.card, border: `1px solid ${C.border}`, borderRadius: 20, padding: '2.2rem', width: '100%', maxWidth: 420, boxShadow: '0 4px 24px rgba(26,16,8,0.06)' }}>

        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem', marginBottom: '1.4rem' }}>
          <span style={{ color: C.pink, fontSize: '1.1rem' }}>✦</span>
          <span style={{ fontFamily: "'Playfair Display', serif", fontSize: '1.1rem', fontWeight: 700, color: C.gold }}>BeautyStock</span>
        </div>

        <h2 style={{ fontFamily: "'Playfair Display', serif", fontSize: '1.55rem', fontWeight: 700, textAlign: 'center', marginBottom: '0.3rem', color: C.dark }}>Welcome back</h2>
        <p style={{ fontSize: '0.82rem', color: C.muted, textAlign: 'center', marginBottom: '1.4rem' }}>Your inventory is ready for you.</p>

        {/* Google */}
        <GoogleSignInButton onSuccess={handleGoogleSuccess} onError={(e) => setError(e)} text="signin_with" />

        {/* Divider */}
        <div style={{ textAlign: 'center', color: C.muted, fontSize: '0.73rem', margin: '0.7rem 0', position: 'relative' }}>
          <span style={{ position: 'relative', zIndex: 1, background: C.card, padding: '0 0.6rem' }}>or</span>
          <div style={{ position: 'absolute', top: '50%', left: 0, right: 0, height: 1, background: C.border, zIndex: 0 }} />
        </div>

        {error && (
          <div style={{ background: '#FDECEA', border: '1px solid #F5C6C0', borderRadius: 8, padding: '0.65rem 0.9rem', fontSize: '0.82rem', color: '#B03525', marginBottom: '1rem' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Email */}
          <div style={{ marginBottom: '1.1rem' }}>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 700, color: C.dark, marginBottom: '0.38rem', letterSpacing: '0.01em' }}>Email Address</label>
            <input
              type="email" name="email" value={formData.email} onChange={handleChange}
              placeholder="you@example.com" disabled={isLoading}
              style={inputStyle}
              onFocus={(e) => (e.target.style.borderColor = C.pink)}
              onBlur={(e) => (e.target.style.borderColor = C.border)}
            />
          </div>

          {/* Password */}
          <div style={{ marginBottom: '0.5rem' }}>
            <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 700, color: C.dark, marginBottom: '0.38rem', letterSpacing: '0.01em' }}>Password</label>
            <input
              type="password" name="password" value={formData.password} onChange={handleChange}
              placeholder="••••••••" disabled={isLoading}
              style={inputStyle}
              onFocus={(e) => (e.target.style.borderColor = C.pink)}
              onBlur={(e) => (e.target.style.borderColor = C.border)}
            />
          </div>

          {/* Forgot */}
          <div style={{ textAlign: 'right', marginBottom: '1rem' }}>
            <a href="#" style={{ fontSize: '0.77rem', color: C.pink, fontWeight: 600, textDecoration: 'none' }}>Forgot password?</a>
          </div>

          <button type="submit" disabled={isLoading} style={btnPinkStyle}>
            {isLoading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: '0.79rem', color: C.muted, marginTop: '0.95rem' }}>
          Don't have an account?{' '}
          <a href="#" onClick={(e) => { e.preventDefault(); navigate('/register') }} style={{ color: C.pink, fontWeight: 700, textDecoration: 'none' }}>Create one</a>
        </p>
      </div>
    </div>
  )
}

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '0.68rem 0.9rem',
  border: `1.5px solid #E8DDD5`,
  borderRadius: 10,
  fontFamily: "'Nunito', sans-serif",
  fontSize: '0.875rem',
  color: '#1A1008',
  background: 'white',
  outline: 'none',
  boxSizing: 'border-box',
  transition: 'border-color 0.15s',
}

const btnPinkStyle: React.CSSProperties = {
  width: '100%',
  background: '#C94B6E',
  color: 'white',
  border: 'none',
  borderRadius: 10,
  padding: '0.78rem',
  fontSize: '0.9rem',
  fontWeight: 700,
  cursor: 'pointer',
  fontFamily: "'Nunito', sans-serif",
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}
