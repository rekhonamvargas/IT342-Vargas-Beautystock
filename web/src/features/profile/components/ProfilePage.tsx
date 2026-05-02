import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore, useDashboardStore } from '@/store/auth'
import { userApi, authApi } from '@/services/api'

export function ProfilePage() {
  const navigate = useNavigate()
  const { user, setUser, clearUser } = useAuthStore()
  const { stats } = useDashboardStore()
  const [city, setCity] = useState(user?.city || '')
  const [cityLoading, setCityLoading] = useState(false)
  const [citySuccess, setCitySuccess] = useState(false)
  const [cityError, setCityError] = useState<string | null>(null)

  const handleUpdateCity = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!city.trim()) return
    setCityError(null)
    setCitySuccess(false)
    try {
      setCityLoading(true)
      await userApi.updateLocation(city.trim())
      // Refresh user data
      const res = await authApi.getMe()
      setUser(res.data)
      setCitySuccess(true)
      setTimeout(() => setCitySuccess(false), 3000)
    } catch (err: any) {
      setCityError(err.response?.data?.message || 'Failed to update city')
    } finally {
      setCityLoading(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    clearUser()
    navigate('/login')
  }

  const roleLabel = user?.role === 'ROLE_YOUTH' ? '🌱 Youth (13–24)' : '🌸 Adult (25+)'

  return (
    <div className="space-y-6">
      <h1 className="font-serif text-3xl font-bold text-dark">My Profile</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Profile Card */}
        <div className="card text-center">
          <div className="w-20 h-20 rounded-full bg-pink-50 flex items-center justify-center text-3xl mx-auto mb-4">
            {user?.firstName?.[0]?.toUpperCase() || '?'}
          </div>
          <h2 className="font-serif text-xl font-bold text-dark">
            {user?.firstName} {user?.lastName}
          </h2>
          <p className="text-muted text-sm mt-0.5">{user?.email}</p>
          <div className="mt-2 inline-block px-3 py-1 bg-pink-50 text-pink text-xs font-semibold rounded-full">
            {roleLabel}
          </div>

          {/* Stats */}
          <div className="grid grid-cols-2 gap-3 mt-6">
            <div className="bg-cream rounded-lg p-3">
              <div className="text-lg font-bold text-dark">{stats?.totalProducts ?? 0}</div>
              <div className="text-xs text-muted">Products</div>
            </div>
            <div className="bg-cream rounded-lg p-3">
              <div className="text-lg font-bold text-dark">{stats?.favoritesCount ?? 0}</div>
              <div className="text-xs text-muted">Favorites</div>
            </div>
          </div>

          <button onClick={handleLogout} className="btn-outline w-full mt-6">
            Sign Out
          </button>
        </div>

        {/* Settings */}
        <div className="lg:col-span-2 space-y-6">
          {/* Account Info */}
          <div className="card space-y-4">
            <h2 className="font-serif text-lg font-bold text-dark">Account Information</h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="fg">
                <label>First Name</label>
                <input type="text" value={user?.firstName || ''} disabled className="bg-cream-50" />
              </div>
              <div className="fg">
                <label>Last Name</label>
                <input type="text" value={user?.lastName || ''} disabled className="bg-cream-50" />
              </div>
            </div>
            <div className="fg">
              <label>Email</label>
              <input type="email" value={user?.email || ''} disabled className="bg-cream-50" />
            </div>
            <div className="fg">
              <label>Age Group</label>
              <input type="text" value={roleLabel} disabled className="bg-cream-50" />
            </div>
          </div>

          {/* Weather City */}
          <div className="card">
            <h2 className="font-serif text-lg font-bold text-dark mb-4">Weather City</h2>
            <p className="text-muted text-sm mb-4">
              Set your city to receive personalized weather-based skincare recommendations.
            </p>
            <form onSubmit={handleUpdateCity} className="flex gap-3">
              <div className="flex-1 fg">
                <input
                  type="text"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  placeholder="e.g. Bangkok, New York, London"
                  required
                />
              </div>
              <button type="submit" disabled={cityLoading} className="btn-pink self-start">
                {cityLoading ? 'Saving...' : 'Update'}
              </button>
            </form>
            {citySuccess && (
              <p className="text-green text-sm mt-2 font-semibold">✓ City updated successfully</p>
            )}
            {cityError && (
              <p className="text-red text-sm mt-2 font-semibold">{cityError}</p>
            )}
          </div>

          {/* Additional Settings */}
          <div className="card space-y-4">
            <h2 className="font-serif text-lg font-bold text-dark">Settings</h2>
            <div className="flex items-center justify-between py-2">
              <div>
                <p className="text-sm font-semibold text-dark">Expiration Notifications</p>
                <p className="text-xs text-muted">Get alerts when products are expiring soon</p>
              </div>
              <div className="w-10 h-6 bg-pink rounded-full relative cursor-pointer">
                <div className="w-4 h-4 bg-white rounded-full absolute top-1 right-1" />
              </div>
            </div>
            <div className="flex items-center justify-between py-2 border-t border-cream-200">
              <div>
                <p className="text-sm font-semibold text-dark">Connected Accounts</p>
                <p className="text-xs text-muted">Google account linked</p>
              </div>
              <span className="text-xs text-green font-semibold">Connected</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
