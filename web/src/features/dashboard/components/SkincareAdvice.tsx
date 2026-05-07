import { useEffect, useState, useRef } from 'react'
import { useAuthStore, useWeatherStore } from '@/store/auth'
import { recommendationApi, userApi, authApi } from '@/services/api'

// Helper function to safely extract error messages
const getErrorMessage = (err: unknown): string => {
  if (err && typeof err === 'object' && 'response' in err) {
    const axiosError = err as { response?: { data?: { message?: string; advice?: string } } }
    return axiosError.response?.data?.message || axiosError.response?.data?.advice || 'An error occurred'
  }
  if (err instanceof Error) {
    return err.message
  }
  return 'An unexpected error occurred'
}

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
const youthEssentials = [
  { 
    icon: '🧼', 
    title: 'Gentle Cleanser', 
    desc: 'Use a mild, pH-balanced cleanser twice daily to remove dirt, oil, and sweat without disrupting the skin barrier',
    source: 'https://health.clevelandclinic.org/how-to-wash-your-face'
  },
  { 
    icon: '💦', 
    title: 'Lightweight Moisturizer', 
    desc: 'Apply an oil-free, non-comedogenic moisturizer to keep skin hydrated and balanced throughout the day',
    source: 'https://health.clevelandclinic.org/how-to-choose-the-best-moisturizer-for-your-dry-skin'
  },
  { 
    icon: '☀️', 
    title: 'Broad-Spectrum SPF 30+', 
    desc: 'Apply sunscreen every morning to protect against UVA/UVB damage and prevent early skin concerns',
    source: 'https://www.skincancer.org/skin-cancer-prevention/sun-protection/'
  },
];

const adultEssentials = [
  { 
    icon: '🧪', 
    title: 'Vitamin C Serum', 
    desc: 'Use in the morning to help brighten skin tone and protect against environmental stressors',
    source: 'https://health.clevelandclinic.org/vitamin-c-serum/'
  },
  { 
    icon: '💦', 
    title: 'Hyaluronic Acid', 
    desc: 'Apply on damp skin to attract and retain moisture for a smoother, plumper appearance',
    source: 'https://www.healthline.com/health/beauty-skin-care/hyaluronic-acid'
  },
  { 
    icon: '🌙', 
    title: 'Retinoid Night Treatment', 
    desc: 'Use at night to support cell turnover and improve fine lines and uneven texture over time',
    source: 'https://my.clevelandclinic.org/health/treatments/23293-retinol'
  },
];

const weatherEssentials = {
  hot: {
    youth: [
      { icon: '🧼', title: 'Gel Cleanser', desc: 'Cleanse after sweating to remove excess oil and prevent clogged pores', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '💦', title: 'Oil-Free Moisturizer', desc: 'Use a lightweight formula to hydrate without adding shine or heaviness', source: 'https://www.nhs.uk/conditions/acne/treatment/' },
      { icon: '☀️', title: 'SPF 50+ Sunscreen', desc: 'Apply generously and reapply every 2 hours when exposed to strong sunlight', source: 'https://www.skincancer.org/skin-cancer-prevention/sun-protection/sunscreen/' },
    ],
    adult: [
      { icon: '💦', title: 'Hydrating Serum', desc: 'Layer a light serum under moisturizer to prevent dehydration from heat exposure', source: 'https://www.healthline.com/health/benefits-of-face-serum' },
      { icon: '🧼', title: 'Gentle Foaming Cleanser', desc: 'Remove sweat and buildup while keeping the skin barrier intact', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '☀️', title: 'SPF 50+ Sunscreen', desc: 'Reapply regularly to maintain consistent UV protection outdoors', source: 'https://www.skincancer.org/skin-cancer-prevention/sun-protection/' },
    ],
  },

  humid: {
    youth: [
      { icon: '🫧', title: 'Foaming Cleanser', desc: 'Wash away sweat, oil, and impurities after humid conditions to prevent breakouts', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '🌿', title: 'Gel Moisturizer', desc: 'Use a breathable, fast-absorbing formula that won’t feel heavy on the skin', source: 'https://health.clevelandclinic.org/how-to-choose-the-best-moisturizer-for-your-dry-skin' },
      { icon: '🧻', title: 'Blotting Sheets', desc: 'Control excess oil during the day without removing your skincare products', source: 'https://www.nhs.uk/conditions/acne/' }
    ],
    adult: [
      { icon: '🫧', title: 'Balancing Cleanser', desc: 'Gently cleanse while helping control oil and buildup in humid environments', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '🌿', title: 'Matte Moisturizer', desc: 'Reduce shine while maintaining hydration with a lightweight texture', source: 'https://www.nhs.uk/conditions/acne/treatment/' },
      { icon: '🧪', title: 'Niacinamide Serum', desc: 'Help regulate oil production and improve the look of pores over time', source: 'https://www.healthline.com/nutrition/niacinamide' },
    ],
  },

  dry: {
    youth: [
      { icon: '💦', title: 'Hydrating Cleanser', desc: 'Use a cream-based cleanser to clean skin without stripping natural moisture', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '🧴', title: 'Ceramide Moisturizer', desc: 'Support the skin barrier and reduce dryness, flaking, or tightness', source: 'https://health.clevelandclinic.org/ceramides' },
      { icon: '🌙', title: 'Sleeping Mask', desc: 'Apply at night to lock in hydration and keep skin soft by morning', source: 'https://www.healthline.com/health/beauty-skin-care/overnight-masks-for-glowing-skin' },
    ],
    adult: [
      { icon: '💦', title: 'Hyaluronic Acid Serum', desc: 'Apply before moisturizer to boost hydration and improve moisture retention', source: 'https://www.healthline.com/health/beauty-skin-care/hyaluronic-acid' },
      { icon: '🧴', title: 'Barrier Repair Cream', desc: 'Use a richer cream to strengthen the skin barrier and prevent water loss', source: 'https://health.clevelandclinic.org/treating-dry-skin-on-face' },
      { icon: '🌙', title: 'Overnight Mask', desc: 'Provide deeper hydration and support skin recovery while you sleep', source: 'https://www.healthline.com/health/beauty-skin-care/overnight-masks-for-glowing-skin' },
    ],
  },

  cold: {
    youth: [
      { icon: '🧴', title: 'Cream Cleanser', desc: 'Use a gentle cleanser that prevents dryness and keeps skin comfortable in cold air', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '💦', title: 'Barrier Moisturizer', desc: 'Maintain hydration and protect skin from dryness caused by low temperatures', source: 'https://health.clevelandclinic.org/treating-dry-skin-on-face' },
      { icon: '☀️', title: 'Daily SPF', desc: 'Continue sunscreen use daily, as UV rays are still present even in cold weather', source: 'https://www.skincancer.org/skin-cancer-prevention/sun-protection/' },
    ],
    adult: [
      { icon: '🧴', title: 'Gentle Cream Cleanser', desc: 'Cleanse without stripping essential moisture during colder conditions', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '💦', title: 'Ceramide Cream', desc: 'Reinforce the skin barrier and improve resilience against dryness', source: 'https://health.clevelandclinic.org/ceramides' },
      { icon: '🌙', title: 'Facial Oil', desc: 'Use at night to seal in moisture and improve overall skin comfort', source: 'https://www.healthline.com/health/best-oils-for-your-skin' },
    ],
  },

  rainy: {
    youth: [
      { icon: '🌧️', title: 'Gentle Cleanser', desc: 'Remove humidity-related buildup while keeping skin balanced and fresh', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '💦', title: 'Light Moisturizer', desc: 'Hydrate skin with a lightweight formula that won’t feel sticky in damp weather', source: 'https://health.clevelandclinic.org/how-to-choose-the-best-moisturizer-for-your-dry-skin' },
      { icon: '☀️', title: 'Water-Resistant SPF', desc: 'Apply daily to maintain sun protection even on cloudy or rainy days', source: 'https://www.skincancer.org/skin-cancer-prevention/sun-protection/sunscreen/' },
    ],
    adult: [
      { icon: '🌧️', title: 'Gel Cleanser', desc: 'Cleanse away sweat and environmental impurities effectively', source: 'https://health.clevelandclinic.org/how-to-wash-your-face' },
      { icon: '💦', title: 'Hydrating Lotion', desc: 'Maintain hydration with a breathable and non-heavy texture', source: 'https://www.nhs.uk/conditions/acne/treatment/' },
      { icon: '☀️', title: 'Water-Resistant SPF', desc: 'Ensure consistent UV protection regardless of weather conditions', source: 'https://www.skincancer.org/skin-cancer-prevention/sun-protection/' },
    ],
  },


  default: {
    youth: youthEssentials,
    adult: adultEssentials,
  },
}

const getWeatherMode = (weather: { temperature?: number | null; humidity?: number | null; condition?: string | null }) => {
  const condition = weather.condition?.toLowerCase() || ''

  if (condition.includes('rain') || condition.includes('drizzle') || condition.includes('storm')) {
    return 'rainy'
  }

  if (weather.temperature != null && weather.temperature >= 31) {
    return 'hot'
  }

  if (weather.temperature != null && weather.temperature <= 12) {
    return 'cold'
  }

  if (weather.humidity != null && weather.humidity >= 75) {
    return 'humid'
  }

  if (weather.humidity != null && weather.humidity <= 35) {
    return 'dry'
  }

  return 'default'
}

const getEssentialsForWeather = (
  weather: { temperature?: number | null; humidity?: number | null; condition?: string | null } | null,
  isYouth: boolean,
) => {
  if (!weather) {
    return isYouth ? youthEssentials : adultEssentials
  }

  const mode = getWeatherMode(weather)
  const byRole = weatherEssentials[mode as keyof typeof weatherEssentials]
  return isYouth ? byRole.youth : byRole.adult
}

export function SkincareAdvice() {
  const { user, setUser } = useAuthStore()
  const { weather, setWeather, setError } = useWeatherStore()
  const [loading, setLoading] = useState(false)
  const [editing, setEditing] = useState(false)
  const [tempCity, setTempCity] = useState(user?.city || '')
  const [saving, setSaving] = useState(false)
  const [updateError, setUpdateError] = useState<string | null>(null)
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const suggestionsRef = useRef<HTMLDivElement>(null)

   const isYouth = user?.role === 'ROLE_YOUTH'
   const essentials = getEssentialsForWeather(weather, isYouth)
   const weatherMode = weather ? getWeatherMode(weather) : 'default'

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

   useEffect(() => {
    if (user?.city) {
      loadWeather()
    }
  }, [user?.city, isYouth])

  useEffect(() => {
    // Close suggestions when clicking outside
    const handleClickOutside = (e: MouseEvent) => {
      if (suggestionsRef.current && !suggestionsRef.current.contains(e.target as Node)) {
        setShowSuggestions(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const loadWeather = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = isYouth
        ? await recommendationApi.getYouthWeather()
        : await recommendationApi.getAdultWeather()
      setWeather(res.data)
    } catch (err: unknown) {
      const errorMsg = getErrorMessage(err) || 'Failed to load weather data'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  const handleCityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value
    setTempCity(value)
    
    if (value.trim().length === 0) {
      setSuggestions([])
      setShowSuggestions(false)
    } else {
      const filtered = POPULAR_CITIES.filter(city =>
        city.toLowerCase().startsWith(value.toLowerCase())
      ).slice(0, 8)
      setSuggestions(filtered)
      setShowSuggestions(true)
    }
  }

  const handleSelectCity = (city: string) => {
    setTempCity(city)
    setShowSuggestions(false)
  }

  const handleSaveCity = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!tempCity.trim()) return

    try {
      setSaving(true)
      setUpdateError(null)
      await userApi.updateLocation(tempCity.trim())
      
      // Refresh user data
      const res = await authApi.getMe()
      setUser(res.data)
      
      // Refresh weather
      setWeather(null)
      setEditing(false)
      await loadWeather()
    } catch (err: unknown) {
      setUpdateError(getErrorMessage(err) || 'Failed to update city')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-5 w-full">
       {/* Simple Header - no background box */}
       <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="hello-title text-pink">
              <span className="hello-wave-wrap">{renderWavingText('Skincare Advice')}</span>
            </h1>
           <p className="text-muted text-[10px] uppercase tracking-wider mb-1 font-normal font-serif mt-3">
  Personalized tips based on your local weather and skin profile
</p>
          </div>
        </div>

      {/* Weather Advice Card */}
      {editing ? (
        <form onSubmit={handleSaveCity} className="rounded-2xl border-2 border-pink-200 bg-white p-6 shadow-sm">
          <div className="space-y-4">
            <div>
              <label className="text-sm font-semibold text-dark block mb-2">Your City</label>
              <div className="relative" ref={suggestionsRef}>
                <input
                  type="text"
                  value={tempCity}
                  onChange={handleCityChange}
                  onFocus={() => tempCity.length > 0 && setShowSuggestions(true)}
                  placeholder="Search for a city..."
                  className="w-full px-4 py-2 border-2 border-pink-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-pink focus:border-transparent text-sm"
                  autoComplete="off"
                  required
                />

                {/* Autocomplete Suggestions Dropdown */}
                {showSuggestions && suggestions.length > 0 && (
                  <div className="absolute top-full left-0 right-0 mt-2 bg-white border-2 border-pink-200 rounded-lg shadow-lg z-10 max-h-48 overflow-y-auto">
                    {suggestions.map((city) => (
                      <button
                        key={city}
                        type="button"
                        onClick={() => handleSelectCity(city)}
                        className="w-full text-left px-4 py-2 hover:bg-pink-50 text-sm text-dark transition-colors font-medium border-b border-pink-100 last:border-b-0"
                      >
                        📍 {city}
                      </button>
                    ))}
                  </div>
                )}
              </div>
              {showSuggestions && suggestions.length === 0 && tempCity.length > 0 && (
                <p className="text-xs text-gray-500 mt-2">No cities found matching "{tempCity}"</p>
              )}
            </div>
            {updateError && <p className="text-xs text-red-600 bg-red-50 p-2 rounded">{updateError}</p>}
            <div className="flex gap-3 justify-end pt-2">
              <button
                type="button"
                onClick={() => setEditing(false)}
                className="px-4 py-2 text-sm border-2 border-gray-300 rounded-lg hover:bg-gray-50 font-medium transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-6 py-2 text-sm bg-pink text-white rounded-lg hover:bg-pink-700 font-semibold disabled:opacity-50 transition-colors"
              >
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </form>
      ) : loading ? (
        <div className="rounded-2xl border-2 border-pink-200 bg-gradient-to-br from-pink-50 to-rose-50 text-center py-12">
          <div className="w-8 h-8 border-2 border-pink border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-muted text-sm font-medium">Loading weather data...</p>
        </div>
      ) : weather ? (
        <div className="rounded-2xl border-2 border-pink-300 bg-gradient-to-br from-pink-50 to-rose-50 p-6 shadow-sm">
          <div className="flex items-start justify-between gap-4 mb-5">
            <div className="flex-1">
              <h3 className="font-semibold text-base text-dark flex items-center gap-2 mb-3">
                <span className="text-2xl">
                  {weatherMode === 'hot' && '🔥'}
                  {weatherMode === 'humid' && '💧'}
                  {weatherMode === 'dry' && '🏜️'}
                  {weatherMode === 'cold' && '❄️'}
                  {weatherMode === 'rainy' && '☔'}
                  {weatherMode === 'default' && (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
    <path d="M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z" />
  </svg>
)}
                </span>
                {isYouth ? 'Youth' : 'Adult'} Weather Analysis
              </h3>
              <div className="flex flex-wrap gap-4 text-xs text-muted font-medium">
                <span className="flex items-center gap-1.5">🌡️ <strong>{weather.temperature}°C</strong></span>
                <span className="flex items-center gap-1.5">💧 <strong>{weather.humidity}%</strong> Humidity</span>
                <span className="flex items-center gap-1.5">📍 <strong>{weather.city}</strong></span>
                {weather.condition && <span className="flex items-center gap-1.5">🌤️ {weather.condition}</span>}
              </div>
            </div>
            <button
              onClick={() => {
                setEditing(true)
                setTempCity(user?.city || '')
              }}
              className="text-xs text-pink hover:text-pink-700 font-semibold whitespace-nowrap hover:underline transition-colors"
            >
              Change City
            </button>
          </div>
           <div className="rounded-lg border-2 border-pink-200 bg-white px-4 py-3">
             <p className="text-[12px] text-pink-900 leading-relaxed font-medium">{weather.advice}</p>
           </div>
        </div>
      ) : (
        <div className="rounded-2xl border-2 border-pink-200 bg-gradient-to-br from-white to-pink-50 p-8 text-center">
          <div className="text-5xl mb-4">📍</div>
          <h3 className="font-serif text-2xl font-bold text-dark mb-2">No Location Set</h3>
          <p className="text-muted text-sm mb-6 max-w-xs mx-auto">
            Add your city to get personalized weather-based skincare tips tailored just for you
          </p>
          <button
            onClick={() => {
              setEditing(true)
              setTempCity('')
            }}
            className="px-6 py-2 bg-pink text-white rounded-lg hover:bg-pink-700 text-sm font-semibold transition-colors"
          >
            + Add Your City
          </button>
        </div>
      )}

      {/* Essentials */}
      <div className="space-y-4">
        <div>
          <h2 className="text-[24px] font-bold text-pink mb-1">
            {isYouth ? '🌱 Youth' : '🌸 Adult'} Skincare Essentials
          </h2>
          
          {/* Weather-based dynamic label */}
          {weather ? (
            <div className="flex items-center gap-2 mb-4 p-3 rounded-lg bg-gradient-to-r from-pink-50 to-rose-50 border border-pink-100">
                <span className="text-xl flex items-center justify-center w-6 h-6">
                  {weatherMode === 'hot' && '🔥'}
                  {weatherMode === 'humid' && '💧'}
                  {weatherMode === 'dry' && '🏜️'}
                  {weatherMode === 'cold' && '❄️'}
                  {weatherMode === 'rainy' && '☔'}
                  {weatherMode === 'default' && (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z" />
                    </svg>
                  )}
                </span>
              <div className="flex-1">
                <p className="text-[24px] font-bold text-pink capitalize font-serif italic">
                  {weatherMode === 'hot' && 'Hot Weather'}
                  {weatherMode === 'humid' && 'Humid Weather'}
                  {weatherMode === 'dry' && 'Dry Conditions'}
                  {weatherMode === 'cold' && 'Cold Weather'}
                  {weatherMode === 'rainy' && 'Rainy Weather'}
                  {weatherMode === 'default' && 'Standard'}
                  {' Essentials'}
                </p>
                <p className="text-xs text-muted">
                  Optimized for {weather.temperature}°C and {weather.humidity}% humidity
                </p>
              </div>
            </div>
          ) : (
            <p className="text-xs text-muted mb-4 font-medium">
              {isYouth ? '🌱 Youth' : '🌸 Adult'} skincare essentials – Set your city to get weather-specific recommendations
            </p>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {essentials.map((item) => (
            <div
              key={item.title}
              className="rounded-2xl border-2 border-pink-100 bg-gradient-to-br from-white via-pink-50 to-rose-50 p-5 hover:shadow-lg hover:border-pink-200 transition-all duration-300 flex flex-col h-full relative overflow-hidden group"
            >
              {/* Subtle animated background gradient on hover */}
              <div className="absolute inset-0 bg-gradient-to-br from-pink-100/0 to-rose-100/0 group-hover:from-pink-100/10 group-hover:to-rose-100/10 transition-all duration-300 pointer-events-none" />
              
              {/* Content */}
              <div className="relative z-10 flex flex-col flex-grow">
                <div className="text-4xl mb-3 transform group-hover:scale-110 transition-transform duration-300">{item.icon}</div>
                <h3 className="font-semibold text-dark text-sm mb-2 flex-shrink-0">{item.title}</h3>
                <p className="text-muted text-xs leading-relaxed mb-4">{item.desc}</p>

                {item.source && (
                  <p className="mt-auto mb-3">
                    <a
                      href={item.source}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-block text-muted text-xs uppercase tracking-wider font-normal font-serif italic text-pink-600 transition-all duration-300 hover:text-pink-400 hover:drop-shadow-[0_0_6px_rgba(236,72,153,0.8)]"
                    >
                      📖 Discover More
                    </a>
                  </p>
                )}
                {/* Weather match indicator */}
                {weather && (
                  <div className="pt-3 border-t border-pink-100">
                    <span className="text-[11px] text-pink-700 font-semibold leading-snug">
                      
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* Additional tips based on weather */}
        {weather && (
          <div className="mt-6 p-4 rounded-xl border-2 border-yellow-200 bg-gradient-to-r from-yellow-50 to-amber-50">
            <p className="text-xs text-amber-900 leading-relaxed">
              <span className="font-semibold">💡 Weather Tip: </span>
              {weatherMode === 'hot' && 'In hot weather, reapply SPF every 2 hours and keep your products in a cool place to prevent formula breakdown.'}
              {weatherMode === 'humid' && 'Humidity can trap bacteria. Use a gentle exfoliating cleanser 2-3 times weekly and avoid heavy occlusive products.'}
              {weatherMode === 'dry' && 'Dry air strips moisture from skin. Layer hydrating serums and consider adding a facial oil to seal in moisture at night.'}
              {weatherMode === 'cold' && 'Cold weather damages your skin barrier. Switch to creamier formulas and avoid very hot water when cleansing.'}
              {weatherMode === 'rainy' && 'Increase antioxidant protection with vitamin C serum to defend against environmental pollutants and dampness-induced congestion.'}
              {weatherMode === 'default' && 'Stick with a gentle routine and wear sunscreen daily to keep skin balanced in normal conditions.'}
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
