import { useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import LogoutConfirmation from './LogoutConfirmation'

const navLinks = [
  { to: '/dashboard', label: 'Dashboard', icon: '◫' },
  { to: '/products', label: 'Products', icon: '◻' },
  { to: '/favorites', label: 'Favorites', icon: '♡' },
  { to: '/skincare-advice', label: 'Skincare Advice', icon: '✦' },
]

export function Layout() {
  const [showLogoutConfirmation, setShowLogoutConfirmation] = useState(false)
  const { user, clearUser } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const isAddProductRoute = location.pathname === '/products/new'
  const isDashboardRoute = location.pathname === '/dashboard'
  const isProductDetailRoute = location.pathname.startsWith('/products/') && location.pathname !== '/products/new'

  const toTitleCase = (value: string) =>
    value
      .trim()
      .split(/\s+/)
      .filter(Boolean)
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
      .join(' ')

  const registeredName = [user?.firstName, user?.lastName].filter(Boolean).join(' ').trim()
  const fallbackName = user?.email?.split('@')[0] || 'Account'
  const displayName = toTitleCase(registeredName || fallbackName)

  const handleLogout = () => {
    setShowLogoutConfirmation(true)
  }

  const confirmLogout = () => {
    localStorage.removeItem('token')
    clearUser()
    setShowLogoutConfirmation(false)
    navigate('/login')
  }

  const cancelLogout = () => {
    setShowLogoutConfirmation(false)
  }

  return (
    <div className="min-h-screen bg-cream">
      <LogoutConfirmation
        isOpen={showLogoutConfirmation}
        onConfirm={confirmLogout}
        onCancel={cancelLogout}
      />
      {/* Navigation */}
      <nav className="appnav">
        <NavLink to="/dashboard" className="flex items-center gap-2 mr-8">
          <span className="text-pink text-sm">✦</span>
          <span className="font-serif font-bold text-[#C88A1A] text-sm">BeautyStock</span>
        </NavLink>

        <div className="flex items-center gap-4">
          {navLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `inline-flex items-center gap-1.5 text-[11px] font-semibold transition-colors ${
                  isActive
                    ? 'text-pink'
                    : 'text-muted hover:text-dark'
                }`
              }
            >
              <span className="text-[10px]">{link.icon}</span>
              {link.label}
            </NavLink>
          ))}
        </div>

        <div className="ml-auto flex items-center gap-3">
          <NavLink
            to="/profile"
            className={({ isActive }) =>
              `text-[11px] font-semibold transition-colors ${
                isActive ? 'text-pink' : 'text-dark hover:text-pink'
              }`
            }
          >
            {displayName}
          </NavLink>
          <button
            onClick={handleLogout}
            className="text-[11px] font-semibold text-muted hover:text-pink transition-colors"
          >
            ↪
          </button>
        </div>
      </nav>

      {/* Main content */}
      <main
        className={
          isAddProductRoute || isDashboardRoute || isProductDetailRoute
            ? 'w-full max-w-[1320px] mx-auto px-4 md:px-6 lg:px-8 py-5'
            : 'max-w-[1120px] mx-auto px-8 py-7'
        }
      >
        <Outlet />
      </main>
    </div>
  )
}
