import { useState, useRef, useEffect } from 'react'
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

const [editingProfile, setEditingProfile] = useState(false)
const [profileFormData, setProfileFormData] = useState({
  firstName: user?.firstName || '',
  lastName: user?.lastName || '',
})
const [profileLoading, setProfileLoading] = useState(false)
const [profileSuccess, setProfileSuccess] = useState(false)
const [profileError, setProfileError] = useState<string | null>(null)

  // Image upload state
  const [imageLoading, setImageLoading] = useState(false)
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


  const handleCityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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

  const handleUpdateProfile = async (e: React.FormEvent) => {
  e.preventDefault()

  if (!profileFormData.firstName.trim()) {
    setProfileError('First name is required')
    return
  }

  setProfileError(null)
  setProfileSuccess(false)

  try {
    setProfileLoading(true)

    await userApi.updateProfile(profileFormData)

    setUser({
      ...user!,
      firstName: profileFormData.firstName,
      lastName: profileFormData.lastName,
    })

    setEditingProfile(false)
    setProfileSuccess(true)

    setTimeout(() => setProfileSuccess(false), 4000)
  } catch (err: any) {
    setProfileError(err.response?.data?.message || 'Failed to update profile')
  } finally {
    setProfileLoading(false)
  }
}

 const handleProfileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
  const { name, value } = e.target
  setProfileFormData(prev => ({ ...prev, [name]: value }))
}

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
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
    <div className="h-[calc(100vh-80px)] overflow-hidden flex flex-col pb-2 relative">
      {/* Decorative floating elements - subtle sparkles */}
      <div className="absolute top-10 left-6 text-pink opacity-15 animate-bounce" style={{animationDuration: '3s'}}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
        </svg>
      </div>
      <div className="absolute top-16 right-10 text-rose opacity-15 animate-bounce" style={{animationDuration: '2.5s', animationDelay: '0.5s'}}>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
        </svg>
      </div>
      <div className="absolute bottom-32 left-8 text-pink opacity-12 animate-bounce" style={{animationDuration: '3.5s', animationDelay: '1s'}}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 17.27L18.18 21L16.54 13.97L22 9.24L14.81 8.62L12 2L9.19 8.62L2 9.24L7.46 13.97L5.82 21L12 17.27Z"/>
        </svg>
      </div>
      <div className="absolute top-40 right-4 text-rose opacity-10 animate-bounce" style={{animationDuration: '4s', animationDelay: '1.5s'}}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
        </svg>
      </div>

       <h1 className="hello-title text-pink mb-6 px-2">
         <span className="hello-wave-wrap">{renderWavingText('My Profile')}</span>
       </h1>

       {/* Main content - full height, uses flex to distribute */}
       <div className="flex flex-col lg:flex-row gap-4">
        {/* Left Column - 40% width */}
        <div className="lg:w-2/5 flex flex-col gap-2 min-h-0">
          {/* Profile Card - fixed height portion */}
          <div className="card p-3 text-center bg-gradient-to-br from-pink-50 via-rose-50 to-pink-50 border-pink-200 flex-shrink-0 relative overflow-hidden shadow-md hover:shadow-lg transition-shadow duration-300">
            {/* Decorative circle background */}
            <div className="absolute top-0 right-0 w-24 h-24 bg-pink opacity-5 rounded-full blur-2xl -translate-y-1/2 translate-x-1/2"></div>
            <div className="absolute bottom-0 left-0 w-20 h-20 bg-rose opacity-5 rounded-full blur-xl translate-y-1/2 -translate-x-1/2"></div>

             {/* Profile Image - Image Upload */}
            <div className="relative w-28 h-28 mx-auto mb-4">
                <div className="w-28 h-28 rounded-2xl border-4 border-pink-200 bg-white flex items-center justify-center text-2xl mx-auto shadow-md cursor-pointer hover:border-pink hover:shadow-lg transition-all" onClick={() => imageInputRef.current?.click()}>
                  {getProfileImageUrl() ? (
                    <img
                      src={getProfileImageUrl()}
                      alt="Profile"
                      className="w-full h-full object-cover rounded-2xl"
                      onError={(e) => {
                        console.error('Image failed to load:', getProfileImageUrl())
                        setImageLoadError(true)
                        e.currentTarget.style.display = 'none'
                      }}
                    />
                  ) : (
                    <span className="text-pink-600 font-serif text-6xl">
                      {user?.firstName?.[0]?.toUpperCase() || '?'}
                    </span>
                  )}
                </div>
             </div>

             {/* Image Upload */}
             <div className="mb-4">
               <p className="text-[11px] font-semibold text-dark mb-2 text-center">Upload your photo</p>
               <input
                 ref={imageInputRef}
                 type="file"
                 accept="image/*"
                 onChange={handleImageUpload}
                 disabled={imageLoading}
                 className="hidden"
               />
               <button
                 type="button"
                 onClick={() => imageInputRef.current?.click()}
                 disabled={imageLoading}
                 className="w-full px-3 py-1.5 text-[11px] bg-gradient-to-r from-pink-500 to-rose-500 text-white rounded-xl hover:from-pink-600 hover:to-rose-600 font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm hover:shadow"
               >
                 {imageLoading ? '⏳ Uploading...' : '📸 Choose Photo'}
               </button>
               {imageError && (
                 <div className="mt-2 p-2 rounded-lg bg-red-50 border border-red-200 animate-fade-in">
                   <p className="text-[10px] text-red-600 flex items-center justify-center gap-1">
                     <span>⚠️</span> {imageError}
                   </p>
                 </div>
               )}
               {imageSuccess && (
                 <div className="mt-2 p-2 rounded-lg bg-green-50 border border-green-200 animate-fade-in">
                   <p className="text-[10px] text-green-600 flex items-center justify-center gap-1">
                     <span>✓</span> Photo updated!
                   </p>
                 </div>
               )}
             </div>
<h2 className="font-serif text-base font-bold text-dark mb-0">
  {user?.firstName} {user?.lastName}
</h2>

<p className="text-muted text-[11px] mt-0">
  {user?.email}
</p>

<div className="mt-1.5 inline-flex items-center gap-1 px-2 py-0.5 bg-white border border-pink-200 text-pink text-[10px] font-semibold rounded-full shadow-sm">
  <span className="text-xs">
    {user?.role === 'ROLE_YOUTH' ? '🌱' : '🌸'}
  </span>
  {user?.role === 'ROLE_YOUTH' ? 'Youth' : 'Adult'}
</div>

{/* Stats */}
<div className="grid grid-cols-2 gap-1.5 mt-2">
  <div className="bg-white rounded-lg border border-pink-100 p-2 shadow-sm hover:shadow transition-all duration-200">
    <div className="flex items-center justify-center mb-0.5">
      <span className="text-base"></span>
    </div>
    <div className="text-lg font-bold text-dark">
      {stats?.totalProducts ?? 0}
    </div>
    <div className="text-[10px] text-muted">Products</div>
  </div>

  <div className="bg-white rounded-lg border border-rose-100 p-2 shadow-sm hover:shadow transition-all duration-200">
    <div className="flex items-center justify-center mb-0.5">
      <span className="text-base"></span>
    </div>
    <div className="text-lg font-bold text-dark">
      {stats?.favoritesCount ?? 0}
    </div>
    <div className="text-[10px] text-muted">Favorites</div>
  </div>
</div>
</div>

          {/* Account Information - takes remaining space */}
          <div className="card p-4 space-y-3 flex-1 flex flex-col min-h-0 relative">
            {/* Decorative accent */}
            <div className="absolute top-0 left-4 w-1 h-6 bg-pink rounded-full"></div>

            <div className="flex items-center justify-between border-b border-pink-100 pb-2.5">
              <div>
                <h2 className="font-serif text-base font-bold text-dark flex items-center gap-2">
                  <span className="text-pink">✨</span>
                  Account Information
                </h2>
                <p className="text-[11px] text-muted mt-0.5">Manage your personal details</p>
              </div>
              <button
                onClick={() => {
                  setProfileFormData({
                    firstName: user?.firstName || '',
                    lastName: user?.lastName || '',
                  })
                  setEditingProfile(true)
                }}
                className="px-2.5 py-1 text-[11px] bg-gradient-to-r from-pink-500 to-rose-500 text-white rounded-xl hover:from-pink-600 hover:to-rose-600 font-semibold transition-all duration-200 shadow-sm hover:shadow whitespace-nowrap"
              >
                ✏️ Edit
              </button>
            </div>

            {editingProfile ? (
              <form onSubmit={handleUpdateProfile} className="flex-1 flex flex-col space-y-2.5">
                <div className="flex flex-col sm:flex-row gap-2">
                  <div className="flex-1 space-y-1.5">
                    <label className="text-[11px] font-semibold text-dark block">First Name *</label>
                    <input
                      type="text"
                      name="firstName"
                      value={profileFormData.firstName}
                      onChange={handleProfileInputChange}
                      placeholder="Enter your first name"
                      required
                      className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white focus:shadow-sm transition-shadow"
                    />
                  </div>
                  <div className="flex-1 space-y-1.5">
                    <label className="text-[11px] font-semibold text-dark block">Last Name</label>
                    <input
                      type="text"
                      name="lastName"
                      value={profileFormData.lastName}
                      onChange={handleProfileInputChange}
                      placeholder="Enter your last name"
                      className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white focus:shadow-sm transition-shadow"
                    />
                   </div>
                 </div>
                 {profileError && (
                   <div className="p-2 rounded-lg bg-red-50 border border-red-200 animate-fade-in">
                     <p className="text-[11px] text-red-600 font-medium flex items-center gap-1">
                       <span>⚠️</span> {profileError}
                     </p>
                   </div>
                 )}
                 {profileSuccess && (
                   <div className="p-2 rounded-lg bg-green-50 border border-green-200 animate-fade-in">
                     <p className="text-[11px] text-green-600 font-medium flex items-center gap-1">
                       <span>✅</span> Profile updated!
                     </p>
                   </div>
                 )}

                 <div className="flex gap-2 justify-end pt-2 border-t border-pink-100 mt-2">
                   <button
                     type="button"
                     onClick={() => {
                       setProfileFormData({
                         firstName: user?.firstName || '',
                         lastName: user?.lastName || '',
                       })
                       setEditingProfile(false)
                     }}
                     className="px-3 py-1.5 text-xs border-2 border-pink-100 rounded-xl hover:bg-pink-50 hover:border-pink-200 font-medium transition-all duration-200"
                   >
                     Cancel
                   </button>
                   <button
                     type="submit"
                     disabled={profileLoading}
                     className="px-4 py-1.5 text-xs bg-gradient-to-r from-pink-500 to-rose-500 text-white rounded-xl hover:from-pink-600 hover:to-rose-600 font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm hover:shadow"
                   >
                     {profileLoading ? '⏳ Saving...' : '✓ Save'}
                   </button>
                 </div>
               </form>
            ) : (
              <div className="flex-1 flex flex-col justify-center space-y-2">
                <div className="flex flex-col sm:flex-row gap-2">
                  <div className="flex-1 space-y-1.5">
                    <label className="text-[11px] font-semibold text-dark block">First Name</label>
                    <input
                      type="text"
                      value={user?.firstName || ''}
                      disabled
                      className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-50 rounded-xl bg-pink-50/50 text-dark"
                    />
                  </div>
                  <div className="flex-1 space-y-1.5">
                    <label className="text-[11px] font-semibold text-dark block">Last Name</label>
                    <input
                      type="text"
                      value={user?.lastName || ''}
                      disabled
                      className="w-full px-2.5 py-1.5 text-xs border-2 border-pink-50 rounded-xl bg-pink-50/50 text-dark"
                    />
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Column - 60% width */}
        <div className="lg:w-3/5 flex flex-col gap-3 min-h-0">
          {/* Weather City Card */}
          <div className="card p-4 flex-shrink-0 relative overflow-hidden">
            {/* Decorative top border */}
            <div className="absolute top-0 left-4 right-4 h-0.5 bg-gradient-to-r from-transparent via-pink-300 to-transparent"></div>

            <div className="flex items-center gap-1.5 mb-2">
              <span className="text-base">🌤️</span>
              <h2 className="font-serif text-base font-bold text-dark">Weather City</h2>
            </div>
            <p className="text-muted text-[11px] mb-3">
              Set your city for personalized weather-based skincare recommendations.
            </p>
            <form onSubmit={handleUpdateCity} className="space-y-2.5">
              <div className="relative">
                <label className="text-[11px] font-semibold text-dark block mb-1">Your City</label>
                <input
                  type="text"
                  value={city}
                  onChange={handleCityChange}
                  onFocus={() => city.length > 0 && setShowSuggestions(true)}
                  placeholder="Search for a city..."
                  required
                  autoComplete="off"
                  className="w-full px-3 py-2 text-xs border-2 border-pink-100 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-200 focus:border-pink bg-white focus:shadow-sm transition-shadow"
                />

                {/* Autocomplete Dropdown */}
                {showSuggestions && suggestions.length > 0 && (
                  <div className="absolute top-full left-0 right-0 mt-1.5 bg-white border-2 border-pink-200 rounded-xl shadow-lg z-20 max-h-40 overflow-y-auto">
                    {suggestions.map((suggCity) => (
                      <button
                        key={suggCity}
                        type="button"
                        onClick={() => handleSelectCity(suggCity)}
                        className="w-full text-left px-3 py-2 hover:bg-gradient-to-r hover:from-pink-50 hover:to-rose-50 text-xs text-dark transition-all font-medium border-b border-pink-50 last:border-b-0 flex items-center gap-1.5"
                      >
                        <span className="text-pink text-sm">📍</span>
                        {suggCity}
                      </button>
                    ))}
                  </div>
                )}
                {showSuggestions && suggestions.length === 0 && city.length > 0 && (
                  <p className="text-[11px] text-gray-500 mt-1.5 flex items-center gap-1">
                    <span>🔍</span>
                    No cities found matching "{city}"
                  </p>
                )}
              </div>
              <button
                type="submit"
                disabled={cityLoading}
                className="btn-pink w-full py-2 text-xs shadow-sm hover:shadow-md hover:scale-[1.02] transition-all duration-200"
              >
                {cityLoading ? '⏳ Updating...' : 'Update City'}
              </button>
            </form>
            {citySuccess && (
              <div className="mt-2 p-2 rounded-lg bg-green-50 border border-green-200 animate-fade-in">
                <p className="text-[11px] text-green-600 font-medium flex items-center gap-1">
                  <span>✅</span> City updated!
                </p>
              </div>
            )}
            {cityError && (
              <div className="mt-2 p-2 rounded-lg bg-red-50 border border-red-200 animate-fade-in">
                <p className="text-[11px] text-red-600 font-medium flex items-center gap-1">
                  <span>❌</span> {cityError}
                </p>
              </div>
            )}
          </div>

          {/* Settings Container - takes remaining height with internal scroll if needed */}
          <div className="card p-4 space-y-3.5 flex-1 overflow-y-auto">
            {/* Notifications Section */}
            <div className="space-y-2.5">
              <div className="flex items-center gap-1.5">
                <span className="text-base">🔔</span>
                <h3 className="font-serif text-sm font-bold text-dark">Notifications</h3>
              </div>
              <p className="text-[11px] text-muted">Get alerts when products are expiring soon</p>

              {isGoogleConnected ? (
                <div className="bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200 rounded-xl p-3 space-y-2 shadow-sm">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-[11px] font-semibold text-green-800 flex items-center gap-1">
                        <span className="text-green-600">✓</span>
                        Connected to {user?.email}
                      </p>
                      <p className="text-[11px] text-green-700 mt-0.5">Email notifications active</p>
                      </div>
                  </div>

                  {/* Toggle */}
                  <div className="flex items-center justify-between pt-1">
                    <div>
                      <p className="text-[11px] font-semibold text-dark">Receive notifications?</p>
                    </div>
                    <button
                      onClick={() => handleToggleNotifications(!notificationsEnabled)}
                      disabled={notificationLoading}
                      className={`relative w-10 h-5 rounded-full transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed shadow-inner ${
                        notificationsEnabled ? 'bg-gradient-to-r from-green-500 to-emerald-500' : 'bg-gray-300'
                      }`}
                      aria-label="Toggle notifications"
                    >
                      <span
                        className={`absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform duration-300 ${
                          notificationsEnabled ? 'translate-x-5' : 'translate-x-0'
                        }`}
                      />
                    </button>
                  </div>
                </div>
              ) : (
                <div className="bg-gradient-to-br from-yellow-50 to-amber-50 border border-yellow-200 rounded-xl p-3 space-y-2 shadow-sm">
                  <p className="text-[11px] font-semibold text-yellow-800 flex items-center gap-1">
                    <span>⚠️</span>
                    Google Account Required
                  </p>
                  <p className="text-[11px] text-yellow-700">
                    Connect your Google account to enable email notifications for product expirations.
                  </p>
                  <button
                    onClick={() => {
                      window.location.href = '/api/oauth2/authorization/google'
                    }}
                    className="w-full px-2.5 py-1.5 text-xs bg-gradient-to-r from-yellow-500 to-amber-500 text-white rounded-xl hover:from-yellow-600 hover:to-amber-600 font-semibold transition-all duration-200 shadow-sm hover:shadow flex items-center justify-center gap-1.5"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24">
                      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="white"/>
                      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="white"/>
                      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="white"/>
                      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="white"/>
                    </svg>
                    Connect Google
                  </button>
                </div>
              )}

              {notificationError && (
                <div className="p-2 rounded-lg bg-red-50 border border-red-200 animate-fade-in">
                  <p className="text-[11px] text-red-600 font-medium flex items-center gap-1">
                    <span>❌</span> {notificationError}
                  </p>
                </div>
              )}

              {notificationSuccess && (
                <div className="p-2 rounded-lg bg-green-50 border border-green-200 animate-fade-in">
                  <p className="text-[11px] text-green-600 font-medium flex items-center gap-1">
                    <span>✨</span> Settings saved!
                  </p>
                </div>
              )}
            </div>

            {/* Connected Accounts Section */}
            <div className="space-y-2 pt-2.5 border-t border-pink-100">
              <div className="flex items-center gap-1.5">
                <span className="text-base">🔗</span>
                <h3 className="font-serif text-sm font-bold text-dark">Connected Accounts</h3>
              </div>
              <p className="text-[11px] text-muted">Manage your connected social accounts</p>

              {/* Google Account Status */}
              <div className="flex items-center justify-between p-2.5 bg-white rounded-xl border border-pink-100 shadow-sm hover:shadow-md transition-shadow">
                <div className="flex items-center gap-2">
                  <svg width="18" height="18" viewBox="0 0 24 24" className="flex-shrink-0">
                    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                  </svg>
                  <div>
                    <p className="text-sm font-semibold text-dark">Google</p>
                    <p className="text-[11px] text-muted">
                      {isGoogleConnected ? 'Connected' : 'Not connected'}
                    </p>
                  </div>
                </div>
                {isGoogleConnected ? (
                  <span className="px-2 py-0.5 bg-gradient-to-r from-green-100 to-emerald-100 text-green-700 text-[11px] font-semibold rounded-full flex items-center gap-1 whitespace-nowrap">
                    <span className="w-1.5 h-1.5 bg-green-600 rounded-full animate-pulse"></span>
                    Connected
                  </span>
                ) : (
                  <span className="px-2 py-0.5 bg-gray-100 text-gray-700 text-[11px] font-semibold rounded-full flex items-center gap-1 whitespace-nowrap">
                    <span className="w-1.5 h-1.5 bg-gray-400 rounded-full"></span>
                    Not Connected
                  </span>
                )}
              </div>

              {!isGoogleConnected && (
                <div className="bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-3 shadow-sm">
                  <p className="text-[11px] text-blue-800 font-semibold mb-1.5 flex items-center gap-1">
                    <span>💡</span>
                    Connect Your Google Account
                  </p>
                  <p className="text-[11px] text-blue-700 mb-2">
                    Enables sign in with Google and notification features.
                  </p>
                  <button
                    onClick={() => {
                      window.location.href = '/api/oauth2/authorization/google'
                    }}
                    className="w-full px-2.5 py-1.5 text-xs bg-gradient-to-r from-blue-500 to-indigo-500 text-white rounded-xl hover:from-blue-600 hover:to-indigo-600 font-semibold transition-all duration-200 shadow-sm hover:shadow flex items-center justify-center gap-1.5"
                  >
                    <svg width="12" height="12" viewBox="0 0 24 24">
                      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="white"/>
                      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="white"/>
                      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="white"/>
                      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="white"/>
                    </svg>
                    Connect Google
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
 }
