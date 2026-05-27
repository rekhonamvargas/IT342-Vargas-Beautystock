import { useEffect, useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import { authApi } from '@/services/api'
import { Layout } from '@/features/shared/components/Layout'
import { LandingPage } from '@/features/shared/components/LandingPage'
import { LoginPage } from '@/features/authentication/components/LoginPage'
import { RegisterPage } from '@/features/authentication/components/RegisterPage'
import { Dashboard } from '@/features/dashboard/components/Dashboard'
import { SkincareAdvice } from '@/features/dashboard/components/SkincareAdvice'
import { ProductsPage } from '@/features/products/components/ProductsPage'
import { AddProductPage } from '@/features/products/components/AddProductPage'
import { ProductDetail } from '@/features/products/components/ProductDetail'
import { FavoritesPage } from '@/features/favorites/components/FavoritesPage'
import { ProfilePage } from '@/features/profile/components/ProfilePage'
import OAuth2CallbackPage from '@/features/authentication/components/OAuth2CallbackPage'
import RoleSelectionPage from '@/features/authentication/components/RoleSelectionPage'

function AuthLoader({ children }: { children: React.ReactNode }) {
  const { user, setUser, setIsLoading } = useAuthStore()
  const token = localStorage.getItem('authToken') || localStorage.getItem('token')

  useEffect(() => {
    if (token && !user) {
      setIsLoading(true)
      authApi
        .getMe()
        .then((res) => setUser(res.data))
        .catch(() => {
          localStorage.removeItem('authToken')
          localStorage.removeItem('token')
        })
        .finally(() => setIsLoading(false))
    }
  }, [token])

  return <>{children}</>
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const location = useLocation()
  const navigate = useNavigate()
  const setToken = useAuthStore((state) => state.setToken)
  const isLoading = useAuthStore((state) => state.isLoading)
  const token = localStorage.getItem('authToken') || localStorage.getItem('token')

  const searchParams = new URLSearchParams(location.search)
  const queryToken = searchParams.get('token')

  if (!token && queryToken) {
    return (
      <OAuthBootstrapGate
        queryToken={queryToken}
        locationPathname={location.pathname}
        locationSearch={location.search}
        navigate={navigate}
        setToken={setToken}
      />
    )
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-cream">
        <div className="text-center">
          <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-muted text-sm">Loading your account...</p>
        </div>
      </div>
    )
  }

  if (!token) return <Navigate to="/login" replace />
  return <>{children}</>
}

function OAuthBootstrapGate({
  queryToken,
  locationPathname,
  locationSearch,
  navigate,
  setToken,
}: {
  queryToken: string
  locationPathname: string
  locationSearch: string
  navigate: ReturnType<typeof useNavigate>
  setToken: (token: string) => void
}) {
  const [isBootstrapping, setIsBootstrapping] = useState(true)

  useEffect(() => {
    const searchParams = new URLSearchParams(locationSearch)
    const isNewUser = searchParams.get('isNewUser') === 'true'

    localStorage.setItem('authToken', queryToken)
    setToken(queryToken)

    if (isNewUser) {
      navigate(`/role-selection?token=${encodeURIComponent(queryToken)}`, { replace: true })
      return
    }

    navigate({ pathname: locationPathname, search: '' }, { replace: true })
    setIsBootstrapping(false)
  }, [locationPathname, locationSearch, navigate, queryToken, setToken])

  if (isBootstrapping) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-cream">
        <div className="text-center">
          <h1 className="text-2xl font-serif text-dark mb-4">Completing Sign In...</h1>
          <p className="text-dark/70">Processing your Google authentication</p>
        </div>
      </div>
    )
  }

  return null
}

function PublicOnly({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('authToken') || localStorage.getItem('token')
  if (token) return <Navigate to="/dashboard" replace />
  return <>{children}</>
}

function App() {
  return (
    <Router>
      <AuthLoader>
        <Routes>
          {/* Public routes */}
          <Route
            path="/"
            element={
              <PublicOnly>
                <LandingPage />
              </PublicOnly>
            }
          />
          <Route
            path="/login"
            element={
              <PublicOnly>
                <LoginPage />
              </PublicOnly>
            }
          />
          <Route
            path="/register"
            element={
              <PublicOnly>
                <RegisterPage />
              </PublicOnly>
            }
          />
          <Route
            path="/oauth2/callback"
            element={<OAuth2CallbackPage />}
          />
          <Route
            path="/role-selection"
            element={<RoleSelectionPage />}
          />

          {/* Protected routes with Layout */}
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/products" element={<ProductsPage />} />
            <Route path="/products/new" element={<AddProductPage />} />
            <Route path="/products/:id" element={<ProductDetail />} />
            <Route path="/favorites" element={<FavoritesPage />} />
            <Route path="/skincare-advice" element={<SkincareAdvice />} />
            <Route path="/profile" element={<ProfilePage />} />
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthLoader>
    </Router>
  )
}

export default App
