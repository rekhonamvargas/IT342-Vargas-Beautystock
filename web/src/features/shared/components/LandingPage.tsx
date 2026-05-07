import { Link } from 'react-router-dom'

const features = [
  {
    icon: '📦',
    title: 'Track Your Collection',
    desc: 'Keep a detailed inventory of all your beauty products with expiry tracking.',
  },
  {
    icon: '🌤',
    title: 'Weather-Based Tips',
    desc: 'Get personalized skincare recommendations based on your local weather.',
  },
  {
    icon: '❤️',
    title: 'Favorites & Alerts',
    desc: 'Mark favorites and receive expiration alerts so nothing goes to waste.',
  },
  {
    icon: '🔒',
    title: 'Secure & Private',
    desc: 'Your data is safely stored with secure authentication and encryption.',
  },
]

export function LandingPage() {
  return (
    <div className="min-h-screen bg-cream flex flex-col">
       {/* Simple Nav */}
       <nav className="flex items-center justify-center py-4 border-b border-cream-200">
         <span className="hello-wave text-pink text-xs mr-1.5">✦</span>
         <span className="hello-title text-[#C88A1A] text-sm">BeautyStock</span>
       </nav>

       {/* Hero */}
       <section className="flex-1 flex flex-col items-center justify-center text-center px-6 py-20 border-b border-cream-200">
         <h1 className="font-serif text-5xl md:text-6xl text-dark max-w-4xl leading-[1.03] tracking-[-0.01em]">
           Your Beauty Collection,<br />
           <span className="hello-title text-pink text-6xl md:text-7xl">Beautifully</span> <span className="hello-title text-[#D8A229] text-6xl md:text-7xl">Organized</span>
         </h1>
        <p className="mt-4 text-muted text-[12px] max-w-xl">
          Track your cosmetics and skincare, get weather-based advice, and
          <br />
          never use an expired product again.
        </p>
        <div className="mt-8 flex gap-4">
          <Link to="/register" className="btn-pink text-sm px-8 py-2 rounded-full">
            Get Started
          </Link>
          <Link to="/login" className="inline-flex items-center justify-center rounded-full border border-cream-200 bg-white px-8 py-2 text-sm font-semibold text-dark hover:bg-cream-50">
            Sign In
          </Link>
        </div>
      </section>

       {/* Features Grid */}
       <section className="max-w-[1080px] mx-auto px-6 py-10">
          <h2 className="text-center hello-title text-3xl text-dark mb-8">Everything You Need</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
           {features.map((f) => (
             <div key={f.title} className="rounded-2xl border border-cream-200 bg-white p-5 text-center min-h-[168px]">
               <div className="mx-auto mb-3 flex h-8 w-8 items-center justify-center rounded-lg bg-pink-50 text-sm">{f.icon}</div>
               <h3 className="font-serif text-dark text-xl leading-tight mb-2">{f.title}</h3>
               <p className="text-muted text-[12px] leading-snug">{f.desc}</p>
             </div>
           ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="text-center py-4 text-[11px] text-muted border-t border-cream-200">
        © {new Date().getFullYear()} BeautyStock. Made with ❤
      </footer>
    </div>
  )
}
