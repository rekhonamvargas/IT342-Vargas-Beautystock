import { useState, useRef, useEffect, type ChangeEvent, type FormEvent } from 'react'
import { useAuthStore, useDashboardStore } from '@/store/auth'
import { userApi, authApi } from '@/services/api'

// Popular cities worldwide for autocomplete
// Prioritizing Philippine cities for weather-based skincare recommendations
const POPULAR_CITIES = [
  // Metro Manila
  'Manila','Quezon City','Caloocan','Las Piñas','Makati','Malabon','Mandaluyong','Marikina','Muntinlupa','Navotas','Parañaque','Pasay','Pasig','San Juan','Taguig','Valenzuela',

  // Luzon
  'Alaminos','Angeles','Antipolo','Bacoor','Baguio','Balanga','Batac','Batangas City','Baybay','Biñan','Cabanatuan','Cabuyao','Cagayan de Oro','Calamba','Calapan','Caloocan',
  'Candon','Cavite City','Dagupan','Dasmariñas','Digos','Dipolog','El Salvador','Gapan','General Mariano Alvarez','General Santos','General Trias','Himamaylan','Ilagan',
  'Iligan','Iloilo City','Imus','Iriga','Kidapawan','Laoag','Lapu-Lapu','Legazpi','Ligao','Lipa','Lucena','Maasin','Mabalacat','Malaybalay','Malolos','Mandaue','Masbate City',
  'Mati','Meycauayan','Muñoz','Naga','Olongapo','Ormoc','Oroquieta','Ozamiz','Pagadian','Palayan','Panabo','Parañaque','Pasig','Puerto Princesa','Quezon City','Roxas',
  'Sagay','Samal','San Carlos','San Fernando','San Jose','San Jose del Monte','San Juan','San Pablo','San Pedro','Santa Rosa','Santiago','Silay','Sipalay','Sorsogon City',
  'Surigao','Tabaco','Tabuk','Tacloban','Tagaytay','Tagbilaran','Tagum','Talisay','Tanauan','Tandag','Tangub','Tarlac City','Tayabas','Toledo','Trece Martires','Tuguegarao',
  'Urdaneta','Valencia','Victorias','Vigan','Zamboanga City',

  // Visayas
  'Bacolod','Bais','Bayawan','Bogo','Borongan','Calbayog','Carcar','Catbalogan','Cebu City','Danao','Dumaguete','Escalante','Guihulngan','Himamaylan','Kabankalan',
  'La Carlota','Maasin','Mandaue','Ormoc','Sagay','San Carlos','Silay','Tacloban','Tagbilaran','Talisay','Toledo',

  // Mindanao
  'Butuan','Cabadbaran','Cagayan de Oro','Dapitan','Davao City','Digos','Dipolog','El Salvador','General Santos','Gingoog','Iligan','Isabela City','Kidapawan','Koronadal',
  'Lamitan','Ligao','Malaybalay','Marawi','Mati','Oroquieta','Ozamiz','Pagadian','Panabo','Samal','Surigao','Tacurong','Tangub','Tandag','Valencia','Zamboanga City',


  // 🌏 Asia
  'Tokyo','Osaka','Kyoto','Nagoya','Sapporo','Fukuoka',
  'Seoul','Busan','Incheon','Daegu',
  'Beijing','Shanghai','Guangzhou','Shenzhen','Chengdu','Hong Kong',
  'Bangkok','Chiang Mai','Phuket','Pattaya','Krabi',
  'Singapore','Kuala Lumpur','Johor Bahru','Penang','Ipoh',
  'Jakarta','Surabaya','Bandung','Medan','Yogyakarta','Bali',
  'Hanoi','Ho Chi Minh City','Da Nang','Hue',
  'Mumbai','Delhi','Bangalore','Hyderabad','Chennai','Kolkata',
  'Dubai','Abu Dhabi','Doha','Riyadh','Jeddah',
  'Istanbul','Ankara','Tehran','Jerusalem','Tel Aviv',

  // 🌍 Europe
  'London','Manchester','Birmingham','Liverpool','Edinburgh',
  'Paris','Lyon','Marseille','Nice',
  'Berlin','Munich','Hamburg','Frankfurt',
  'Madrid','Barcelona','Valencia','Seville',
  'Rome','Milan','Naples','Florence','Venice',
  'Amsterdam','Rotterdam','The Hague',
  'Brussels','Antwerp',
  'Vienna','Salzburg',
  'Prague','Brno',
  'Budapest',
  'Warsaw','Krakow',
  'Stockholm','Gothenburg',
  'Copenhagen',
  'Oslo',
  'Helsinki',
  'Dublin',
  'Lisbon','Porto',
  'Athens',

  // 🌎 Americas
  'New York','Los Angeles','Chicago','Houston','Phoenix','Philadelphia','San Antonio','San Diego','Dallas','San Jose',
  'Toronto','Vancouver','Montreal','Calgary','Ottawa',
  'Mexico City','Guadalajara','Monterrey',
  'São Paulo','Rio de Janeiro','Brasília','Salvador',
  'Buenos Aires','Santiago','Lima','Bogotá','Caracas',

  // 🌍 Africa
  'Cairo','Alexandria',
  'Lagos','Abuja',
  'Nairobi',
  'Johannesburg','Cape Town','Durban',
  'Casablanca','Rabat',
  'Addis Ababa',

  // 🌏 Oceania
  'Sydney','Melbourne','Brisbane','Perth','Adelaide',
  'Auckland','Wellington','Christchurch'
].sort();

export function ProfilePage() {
  const { user, setUser } = useAuthStore()
  const { stats } = useDashboardStore()
  

  // City/Location state
  const [city, setCity] = useState(user?.city || '')
  const [cityLoading, setCityLoading] = useState(false)
  const [citySuccess, setCitySuccess] = useState(false)
  const [cityError, setCityError] = useState<string | null>(null)
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const suggestionsRef = useRef<HTMLDivElement>(null)

const [profileFormData, setProfileFormData] = useState({
  firstName: user?.firstName || '',
  lastName: user?.lastName || '',
  email: user?.email || '',
  password: '',
})
const [profileLoading, setProfileLoading] = useState(false)
const [profileSuccess, setProfileSuccess] = useState(false)
const [profileError, setProfileError] = useState<string | null>(null)

  // Image upload state
  const [, setImageLoading] = useState(false)
  const [imageError, setImageError] = useState<string | null>(null)
  const [imageSuccess, setImageSuccess] = useState(false)
  const [imageLoadError, setImageLoadError] = useState(false)
  const imageInputRef = useRef<HTMLInputElement>(null)

  // Notification state
  const [notificationsEnabled, setNotificationsEnabled] = useState(user?.notificationsEnabled || false)
  const [notificationLoading, setNotificationLoading] = useState(false)
  const [notificationError, setNotificationError] = useState<string | null>(null)
  const [notificationSuccess, setNotificationSuccess] = useState(false)
  const isGoogleConnected = user?.googleId != null && user?.googleId !== ''

 



useEffect(() => {
  const handleClickOutside = (e: MouseEvent) => {
    if (suggestionsRef.current && !suggestionsRef.current.contains(e.target as Node)) {
      setShowSuggestions(false)
    }
  }

  document.addEventListener('mousedown', handleClickOutside)
  return () => document.removeEventListener('mousedown', handleClickOutside)
}, [])


  const handleCityChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value
    setCity(value)

    if (value.trim().length === 0) {
      setSuggestions([])
      setShowSuggestions(false)
    } else {
      const filtered = POPULAR_CITIES.filter(c =>
        c.toLowerCase().startsWith(value.toLowerCase())
      ).slice(0, 8)
      setSuggestions(filtered)
      setShowSuggestions(true)
    }
  }

  const handleSelectCity = (selectedCity: string) => {
    setCity(selectedCity)
    setShowSuggestions(false)
  }

  const handleUpdateProfile = async (e: FormEvent) => {
  e.preventDefault()

  if (!profileFormData.firstName.trim()) {
    setProfileError('First name is required')
    return
  }

  if (!profileFormData.email.trim()) {
    setProfileError('Email is required')
    return
  }

  setProfileError(null)
  setProfileSuccess(false)

  try {
    setProfileLoading(true)

    const updateData = {
      firstName: profileFormData.firstName,
      lastName: profileFormData.lastName,
      email: profileFormData.email,
      ...(profileFormData.password && { password: profileFormData.password }),
    }

    await userApi.updateProfile(updateData)

    setUser({
      ...user!,
      firstName: profileFormData.firstName,
      lastName: profileFormData.lastName,
      email: profileFormData.email,
    })

    setProfileSuccess(true)

    setTimeout(() => setProfileSuccess(false), 4000)
  } catch (err: any) {
    setProfileError(err.response?.data?.message || 'Failed to update profile')
  } finally {
    setProfileLoading(false)
  }
}

 const handleProfileInputChange = (e: ChangeEvent<HTMLInputElement>) => {
  const { name, value } = e.target
  setProfileFormData(prev => ({ ...prev, [name]: value }))
}

  const handleImageUpload = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    if (!file.type.startsWith('image/')) {
      setImageError('Please select an image file')
      return
    }

    if (file.size > 5 * 1024 * 1024) {
      setImageError('Image size must be less than 5MB')
      return
    }

    setImageError(null)
    setImageSuccess(false)

    try {
      setImageLoading(true)
      await userApi.uploadProfileImage(file)
      const res = await authApi.getMe()
      setUser(res.data)

      setImageSuccess(true)
      setImageLoadError(false)
      setTimeout(() => setImageSuccess(false), 4000)
      if (imageInputRef.current) {
        imageInputRef.current.value = ''
      }
    } catch (err: any) {
      setImageError(err.response?.data?.message || 'Failed to upload image')
    } finally {
      setImageLoading(false)
    }
  }

  const getProfileImageUrl = () => {
    if (!user?.profileImageUrl || imageLoadError) return undefined
    // Return URL as-is; Vite proxy will handle both /api and /uploads paths
    return user.profileImageUrl
  }

  const handleToggleNotifications = async (enabled: boolean) => {
    setNotificationLoading(true)
    setNotificationError(null)
    setNotificationSuccess(false)
    try {
      await userApi.updateNotificationSettings({ enabled })
      setNotificationsEnabled(enabled)
      setNotificationSuccess(true)
      setTimeout(() => setNotificationSuccess(false), 4000)
    } catch (err: any) {
      setNotificationError(err.response?.data?.message || 'Failed to update notifications')
    } finally {
      setNotificationLoading(false)
    }
  }

    const handleUpdateCity = async (e: FormEvent) => {
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
        setTimeout(() => setCitySuccess(false), 4000)
      } catch (err: any) {
        setCityError(err.response?.data?.message || 'Failed to update city')
      } finally {
        setCityLoading(false)
      }
    }

    const renderWavingText = (text: string) =>
    Array.from(text).map((character, index) => (
      <span
        key={`${character}-${index}`}
        className="hello-wave"
        style={{ animationDelay: `${index * 0.08}s` }}
      >
        {character === ' ' ? '\u00A0' : character}
      </span>
    ))

  return (
    <div className="min-h-[calc(100vh-80px)] w-full overflow-hidden relative">
      {/* Decorative floating rose petals / sparkles */}
      <div className="absolute top-10 left-6 text-pink-400 opacity-20 animate-bounce" style={{ animationDuration: '3s' }}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
        </svg>
      </div>
      <div className="absolute top-16 right-10 text-rose-300 opacity-20 animate-bounce" style={{ animationDuration: '2.5s', animationDelay: '0.5s' }}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
        </svg>
      </div>
      <div className="absolute bottom-32 left-8 text-pink-300 opacity-15 animate-bounce" style={{ animationDuration: '3.5s', animationDelay: '1s' }}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 17.27L18.18 21L16.54 13.97L22 9.24L14.81 8.62L12 2L9.19 8.62L2 9.24L7.46 13.97L5.82 21L12 17.27Z" />
        </svg>
      </div>
      <div className="absolute top-40 right-4 text-rose-300 opacity-15 animate-bounce" style={{ animationDuration: '4s', animationDelay: '1.5s' }}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
        </svg>
      </div>

      <div className="mx-auto flex min-h-full w-full max-w-[1600px] flex-col gap-5 px-3 py-6 sm:px-5 lg:px-6">
        <h1 className="hello-title text-pink mb-2 text-2xl tracking-tight sm:text-3xl">
          <span className="hello-wave-wrap">{renderWavingText('My Profile')}</span>
        </h1>

        <div className="grid h-full min-h-fit gap-6 lg:grid-cols-[1.3fr_0.7fr]">
            {/* Left column - Profile card */}
            <div className="relative rounded-[32px] border border-pink-200/60 bg-transparent backdrop-blur-sm p-5 shadow-xl z-10">
             <div className="flex justify-center mb-5">
               <div
                 className="relative w-full max-w-[180px] h-[180px] rounded-2xl border-2 border-pink-200 shadow-lg overflow-hidden cursor-pointer hover:shadow-xl transition-all"
                 onClick={() => imageInputRef.current?.click()}
               >
                 {getProfileImageUrl() && !imageLoadError ? (
                   <img
                     src={getProfileImageUrl()}
                     alt="Profile"
                     className="w-full h-full object-cover"
                     loading="eager"
                     decoding="async"
                     onError={() => {
                       console.error('Image failed to load:', getProfileImageUrl())
                       setImageLoadError(true)
                     }}
                   />
                 ) : (
                   <div className="flex h-full w-full items-center justify-center">
                     <span className="text-pink-600 font-serif text-[3.5rem] font-bold uppercase">
                       {user?.firstName?.[0]?.toUpperCase() || '?'}
                     </span>
                   </div>
                 )}
               </div>
             </div>

            <input
              ref={imageInputRef}
              type="file"
              accept="image/*"
              onChange={handleImageUpload}
              className="hidden"
            />

            {/* Success/Error messages */}
            {imageSuccess && (
              <div className="mt-4 p-3 rounded-2xl bg-pink-50/80 border border-pink-200/60 w-full max-w-xs mx-auto">
                <p className="text-[10px] text-pink-700 font-medium flex items-center justify-center gap-1">
                  <span>✓</span> Photo updated!
                </p>
              </div>
            )}
            {imageError && (
              <div className="mt-4 p-3 rounded-2xl bg-red-50/80 border border-red-200/60 w-full max-w-xs mx-auto">
                <p className="text-[10px] text-red-700 font-medium flex items-center justify-center gap-1">
                  <span>✕</span> {imageError}
                </p>
              </div>
            )}

            <div className="mt-6 w-full border-t border-pink-100 pt-5 text-center">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="text-left sm:text-left">
                  <h2 className="font-serif text-xl font-bold text-pink-900">
                    {user?.firstName} {user?.lastName}
                  </h2>
                  <p className="text-[11px] text-pink-600/80 mt-2">{user?.email}</p>
                </div>
              </div>

              <div className="mt-4 inline-flex items-center gap-2 px-3 py-1 rounded-full border border-pink-200 bg-pink-50/50 text-pink-800 text-[11px] font-semibold justify-center mx-auto">
                <span className="text-xs">{user?.role === 'ROLE_YOUTH' ? '🌱' : '🌸'}</span>
                {user?.role === 'ROLE_YOUTH' ? 'Youth' : 'Adult'}
              </div>

               <div className="mt-4 grid grid-cols-2 gap-3">
                 <div className="rounded-3xl bg-gradient-to-br from-pink-50/70 to-rose-50/70 border border-pink-200/50 p-3 shadow-sm">
                   <div className="text-2xl font-bold text-pink-900">{stats?.totalProducts ?? 0}</div>
                   <div className="text-[10px] text-pink-700/70 mt-1">Products</div>
                 </div>
                 <div className="rounded-3xl bg-gradient-to-br from-rose-50/70 to-pink-50/70 border border-pink-200/50 p-3 shadow-sm">
                   <div className="text-2xl font-bold text-pink-900">{stats?.favoritesCount ?? 0}</div>
                   <div className="text-[10px] text-pink-700/70 mt-1">Favorites</div>
                 </div>
               </div>

               <p className="text-[11px] text-pink-600/80 mb-2 text-center italic">
                 Manage your account details below.
               </p>

               <form onSubmit={handleUpdateProfile} className="space-y-2 rounded-3xl border border-pink-200/60 bg-white/50 backdrop-blur-sm p-3 shadow-sm">
                <div className="grid grid-cols-2 gap-1">
                  <label className="block text-[10px] font-semibold text-pink-800">
                    First name
                    <input
                      name="firstName"
                      value={profileFormData.firstName}
                      onChange={handleProfileInputChange}
                      className="mt-1 w-full rounded-2xl border-2 border-pink-100 bg-white px-2 py-1.5 text-xs text-pink-900 focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink-300 transition-all placeholder:text-pink-300"
                    />
                  </label>
                  <label className="block text-[10px] font-semibold text-pink-800">
                    Last name
                    <input
                      name="lastName"
                      value={profileFormData.lastName}
                      onChange={handleProfileInputChange}
                      className="mt-1 w-full rounded-2xl border-2 border-pink-100 bg-white px-2 py-1.5 text-xs text-pink-900 focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink-300 transition-all placeholder:text-pink-300"
                    />
                  </label>
                  <label className="block text-[10px] font-semibold text-pink-800">
                    Email
                    <input
                      name="email"
                      type="email"
                      value={profileFormData.email}
                      onChange={handleProfileInputChange}
                      className="mt-1 w-full rounded-2xl border-2 border-pink-100 bg-white px-2 py-1.5 text-xs text-pink-900 focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink-300 transition-all placeholder:text-pink-300"
                    />
                  </label>
                  <label className="block text-[10px] font-semibold text-pink-800">
                    Password
                    <input
                      name="password"
                      type="password"
                      value={profileFormData.password}
                      onChange={handleProfileInputChange}
                      placeholder="Leave blank to keep current"
                      className="mt-1 w-full rounded-2xl border-2 border-pink-100 bg-white px-2 py-1.5 text-xs text-pink-900 focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink-300 transition-all placeholder:text-pink-300"
                    />
                  </label>
                </div>

                {profileError && (
                  <div className="text-[10px] text-red-600 bg-red-50/80 p-1 rounded-lg border border-red-200/60">{profileError}</div>
                )}

                <div className="flex flex-col gap-2 sm:flex-row sm:justify-end">
                  <button
                    type="submit"
                    disabled={profileLoading}
                    className="btn-pink rounded-2xl px-2 py-0.5 text-[10px] font-semibold shadow-sm hover:shadow-md transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed"
                  >
                    {profileLoading ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>

                {profileSuccess && (
                  <div className="rounded-2xl bg-pink-50/90 border border-pink-200/60 p-1 text-[10px] text-pink-700 text-center">
                    Profile updated successfully!
                  </div>
                )}
              </form>
            </div>
          </div>

          {/* Right column - Settings cards */}
          <div className="grid gap-4">
            {/* Weather City Card */}
            <div className="rounded-[28px] border border-pink-200/60 bg-gradient-to-br from-pink-50/60 to-rose-50/60 p-4 shadow-xl backdrop-blur-sm">
              <div className="flex items-start gap-3 mb-4">
                <span className="text-2xl">🌤️</span>
                <div>
                  <h2 className="font-serif text-lg font-bold text-pink-900">Weather City</h2>
                  <p className="text-[10px] text-pink-700/70">Set your city for personalized weather-based skincare recommendations.</p>
                </div>
              </div>
              <form onSubmit={handleUpdateCity} className="space-y-4">
                <div className="relative">
                  <label className="text-[10px] font-semibold text-pink-800 block mb-2">Your City</label>
                  <input
                    type="text"
                    value={city}
                    onChange={handleCityChange}
                    onFocus={() => city.length > 0 && setShowSuggestions(true)}
                    placeholder="Search for a city..."
                    required
                    autoComplete="off"
                    className="w-full px-3 py-2.5 text-sm border-2 border-pink-200 rounded-2xl bg-white text-pink-900 focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink-400 transition-all placeholder:text-pink-300"
                  />
                  {showSuggestions && suggestions.length > 0 && (
                    <div className="absolute top-full left-0 right-0 mt-2 bg-white border-2 border-pink-200 rounded-2xl shadow-lg z-50 max-h-44 overflow-y-auto">
                      {suggestions.map((suggCity) => (
                        <button
                          key={suggCity}
                          type="button"
                          onClick={() => handleSelectCity(suggCity)}
                          className="w-full text-left px-3 py-2.5 hover:bg-pink-50 text-sm text-pink-900 transition-all font-medium border-b border-pink-100 last:border-b-0 flex items-center gap-2"
                        >
                          <span className="text-pink-500 text-xs">📍</span>
                          {suggCity}
                        </button>
                      ))}
                    </div>
                  )}
                  {showSuggestions && suggestions.length === 0 && city.length > 0 && (
                    <p className="text-[10px] text-pink-600 mt-2 flex items-center gap-1">
                      <span className="text-pink-500 text-xs">📍</span> No cities found matching "{city}"
                    </p>
                  )}
                </div>
                <button
                  type="submit"
                  disabled={cityLoading}
                  className="btn-pink w-full rounded-2xl py-2.5 text-sm font-semibold shadow-sm hover:shadow-md transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {cityLoading ? '⏳ Updating...' : 'Update City'}
                </button>
              </form>
              {citySuccess && (
                <div className="mt-3 p-3 rounded-2xl bg-pink-50/90 border border-pink-200/60">
                  <p className="text-[10px] text-pink-700 font-medium flex items-center gap-1">
                    <span>✅</span> City updated!
                  </p>
                </div>
              )}
              {cityError && (
                <div className="mt-3 p-3 rounded-2xl bg-red-50/80 border border-red-200/60">
                  <p className="text-[10px] text-red-700 font-medium flex items-center gap-1">
                    <span>✕</span> {cityError}
                  </p>
                </div>
              )}
            </div>

            {/* Notifications Card */}
            <div className="rounded-[28px] border border-pink-200/60 bg-gradient-to-br from-pink-50/60 to-rose-50/60 p-4 shadow-xl backdrop-blur-sm">
              <div className="flex items-center gap-3 mb-4">
                <span className="text-2xl">🔔</span>
                <div>
                  <h3 className="font-serif text-lg font-bold text-pink-900">Notifications</h3>
                  <p className="text-[10px] text-pink-700/70">Get alerts when products are expiring soon.</p>
                </div>
              </div>

              {isGoogleConnected ? (
                <div className="rounded-3xl bg-white/50 border border-pink-200/50 p-3 shadow-sm mb-3">
                  <p className="text-[11px] font-semibold text-pink-900 flex items-center gap-2">
                    <span>✓</span> Connected to {user?.email}
                  </p>
                  <p className="text-[11px] text-pink-700/80">Email notifications active</p>
                  <div className="mt-2.5 flex items-center justify-between">
                    <p className="text-[11px] font-semibold text-pink-900">Receive notifications?</p>
                    <button
                      onClick={() => handleToggleNotifications(!notificationsEnabled)}
                      disabled={notificationLoading}
                      className={`relative w-12 h-6 rounded-full transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed shadow-inner ${notificationsEnabled ? 'bg-gradient-to-r from-pink-400 to-rose-400' : 'bg-pink-200'}`}
                      aria-label="Toggle notifications"
                    >
                      <span className={`absolute top-0.5 left-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-300 ${notificationsEnabled ? 'translate-x-6' : 'translate-x-0'}`} />
                    </button>
                  </div>
                </div>
              ) : (
                <div className="rounded-3xl bg-white/50 border border-pink-200/50 p-3 shadow-sm mb-3">
                  <p className="text-[11px] font-semibold text-pink-900 flex items-center gap-2">
                    <span>⚠️</span> Google Account Required
                  </p>
                  <p className="text-[11px] text-pink-700/80">Connect your Google account to enable email notifications.</p>
                  <button
                    onClick={() => {
                      window.location.href = '/api/oauth2/authorization/google'
                    }}
                    className="mt-3 w-full btn-pink rounded-2xl px-3 py-2.5 text-xs font-semibold shadow-sm hover:shadow-md transition-all duration-200"
                  >
                    Connect Google
                  </button>
                </div>
              )}

              {notificationError && (
                <div className="p-3 rounded-2xl bg-red-50/80 border border-red-200/60">
                  <p className="text-[10px] text-red-700 font-medium flex items-center gap-1">
                    <span>✕</span> {notificationError}
                  </p>
                </div>
              )}
              {notificationSuccess && (
                <div className="p-3 rounded-2xl bg-pink-50/90 border border-pink-200/60">
                  <p className="text-[10px] text-pink-700 font-medium flex items-center gap-1">
                    <span>✨</span> Settings saved!
                  </p>
                </div>
              )}

              {/* Connected Accounts */}
              <div className="pt-3 border-t border-pink-200/50">
                <div className="flex items-center gap-2 mb-1.5">
                  <span className="text-base">🔗</span>
                  <h3 className="font-serif text-sm font-bold text-pink-900">Connected Accounts</h3>
                </div>
                <p className="text-[10px] text-pink-700/70 mb-3">Manage your connected social accounts</p>

                <div className="flex items-center justify-between p-2.5 bg-white/50 rounded-xl border border-pink-200/50 shadow-sm">
                  <div className="flex items-center gap-3">
                    <svg width="18" height="18" viewBox="0 0 24 24" className="flex-shrink-0 text-pink-600">
                      <circle cx="12" cy="12" r="10" fill="currentColor" />
                    </svg>
                    <div>
                      <p className="text-sm font-semibold text-pink-900">Google</p>
                      <p className="text-[11px] text-pink-700/70">{isGoogleConnected ? 'Connected' : 'Not connected'}</p>
                    </div>
                  </div>
                  <span className={isGoogleConnected ? 'px-3 py-1 rounded-full bg-pink-100/80 text-pink-900 text-[11px] font-semibold border border-pink-200/60' : 'px-3 py-1 rounded-full bg-pink-50 text-pink-700 text-[11px] font-semibold border border-pink-200/60'}>
                    {isGoogleConnected ? 'Connected' : 'Not connected'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <style>{`
        /* Hide scrollbars while keeping scroll functionality */
        div::-webkit-scrollbar {
          width: 6px;
        }
        div::-webkit-scrollbar-track {
          background: transparent;
        }
        div::-webkit-scrollbar-thumb {
          background: rgba(236, 72, 153, 0.2);
          border-radius: 3px;
        }
        div::-webkit-scrollbar-thumb:hover {
          background: rgba(236, 72, 153, 0.4);
        }
      `}</style>
    </div>
  )
 }
