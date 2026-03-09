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

export function RegisterPage() {
  const navigate = useNavigate()
  const setUser = useAuthStore((state) => state.setUser)
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
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

  const handleGoogleSuccess = async (idToken: string) => {
    setError(null)
    try {
      setIsLoading(true)
      const response = await authApi.googleAuth(idToken, formData.ageRange)
      localStorage.setItem('token', response.data.token)
      setUser(response.data.user)
      navigate('/dashboard')
    } catch (err: any) {
      const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.message || 'Google sign-up failed'
      setError(msg)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    if (!formData.fullName.trim()) { setError('Full name is required'); return }
    if (formData.password !== formData.confirmPassword) { setError('Passwords do not match'); return }
    if (!/^(?=.*[A-Z])(?=.*\d).{8,}$/.test(formData.password)) {
      setError('Password must be at least 8 characters with 1 uppercase letter and 1 digit'); return
    }
    try {
      setIsLoading(true)
      const response = await authApi.register(formData)
      localStorage.setItem('token', response.data.token)
      setUser(response.data.user)
      navigate('/dashboard')
    } catch (err: any) {
      const raw = err.response?.data
      setError(typeof raw === 'string' ? raw : raw?.message || 'Registration failed')
    } finally {
      setIsLoading(false)
    }
  }

  const inp: React.CSSProperties = {
    width: '100%', padding: '0.68rem 0.9rem', border: `1.5px solid ${C.border}`,
    borderRadius: 10, fontFamily: "'Nunito', sans-serif", fontSize: '0.875rem',
    color: C.dark, background: 'white', outline: 'none', boxSizing: 'border-box',
  }
  const lbl: React.CSSProperties = {
    display: 'block', fontSize: '0.75rem', fontWeight: 700,
    color: C.dark, marginBottom: '0.38rem', letterSpacing: '0.01em',
  }

  return (
    <div style={{ background: C.bg, minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '2.5rem 1rem', fontFamily: "'Nunito', sans-serif" }}>
      <div style={{ background: C.card, border: `1px solid ${C.border}`, borderRadius: 20, padding: '2.2rem', width: '100%', maxWidth: 420, boxShadow: '0 4px 24px rgba(26,16,8,0.06)' }}>

        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem', marginBottom: '1.4rem' }}>
          <span style={{ color: C.pink, fontSize: '1.1rem' }}>✦</span>
          <span style={{ fontFamily: "'Playfair Display', serif", fontSize: '1.1rem', fontWeight: 700, color: C.gold }}>BeautyStock</span>
        </div>

        <h2 style={{ fontFamily: "'Playfair Display', serif", fontSize: '1.55rem', fontWeight: 700, textAlign: 'center', marginBottom: '0.3rem', color: C.dark }}>Create Account</h2>
        <p style={{ fontSize: '0.82rem', color: C.muted, textAlign: 'center', marginBottom: '1.4rem' }}>Join BeautyStock and take control of your routine.</p>

        {/* Google */}
        <GoogleSignInButton onSuccess={handleGoogleSuccess} onError={(e) => setError(e)} text="signup_with" />

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
          {/* Full Name */}
          <div style={{ marginBottom: '1.1rem' }}>
            <label style={lbl}>Full Name</label>
            <input type="text" name="fullName" value={formData.fullName} onChange={handleInputChange}
              placeholder="e.g. Rekhona Vargas" disabled={isLoading} style={inp}
              onFocus={(e) => (e.target.style.borderColor = C.pink)}
              onBlur={(e) => (e.target.style.borderColor = C.border)} />
          </div>

          {/* Email */}
          <div style={{ marginBottom: '1.1rem' }}>
            <label style={lbl}>Email Address</label>
            <input type="email" name="email" value={formData.email} onChange={handleInputChange}
              placeholder="you@example.com" disabled={isLoading} style={inp}
              onFocus={(e) => (e.target.style.borderColor = C.pink)}
              onBlur={(e) => (e.target.style.borderColor = C.border)} />
          </div>

          {/* Password row */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.1rem' }}>
            <div>
              <label style={lbl}>Password</label>
              <input type="password" name="password" value={formData.password} onChange={handleInputChange}
                placeholder="Min. 8 characters" disabled={isLoading} style={inp}
                onFocus={(e) => (e.target.style.borderColor = C.pink)}
                onBlur={(e) => (e.target.style.borderColor = C.border)} />
            </div>
            <div>
              <label style={lbl}>Confirm Password</label>
              <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleInputChange}
                placeholder="Repeat password" disabled={isLoading} style={inp}
                onFocus={(e) => (e.target.style.borderColor = C.pink)}
                onBlur={(e) => (e.target.style.borderColor = C.border)} />
            </div>
          </div>

          {/* Age Group tiles */}
          <div style={{ marginBottom: '1.4rem' }}>
            <label style={lbl}>Age Group</label>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.55rem', marginTop: '0.38rem' }}>
              {(['YOUTH', 'ADULT'] as const).map((v) => {
                const sel = formData.ageRange === v
                return (
                  <div key={v} onClick={() => !isLoading && setFormData((p) => ({ ...p, ageRange: v }))}
                    style={{ border: `2px solid ${sel ? C.pink : C.border}`, borderRadius: 10, padding: '0.85rem',
                      cursor: 'pointer', textAlign: 'center', background: sel ? C.pinkBg : 'white' }}>
                    <div style={{ fontSize: '0.62rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.07em', color: sel ? C.pink : C.muted }}>
                      {v === 'YOUTH' ? 'Youth' : 'Adult'}
                    </div>
                    <div style={{ fontFamily: "'Playfair Display', serif", fontSize: '1.05rem', color: C.dark, marginTop: '0.15rem' }}>
                      {v === 'YOUTH' ? '13 – 24' : '25 – 44'}
                    </div>
                  </div>
                )
              })}
            </div>
          </div>

          <button type="submit" disabled={isLoading}
            style={{ width: '100%', background: isLoading ? '#d4768e' : C.pink, color: 'white', border: 'none',
              borderRadius: 10, padding: '0.78rem', fontSize: '0.9rem', fontWeight: 700,
              cursor: isLoading ? 'not-allowed' : 'pointer', fontFamily: "'Nunito', sans-serif",
              display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {isLoading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: '0.79rem', color: C.muted, marginTop: '0.95rem' }}>
          Already have an account?{' '}
          <a href="#" onClick={(e) => { e.preventDefault(); navigate('/login') }}
            style={{ color: C.pink, fontWeight: 700, textDecoration: 'none' }}>Sign in</a>
        </p>
      </div>
    </div>
  )
}
