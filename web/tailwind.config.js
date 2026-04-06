/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        pink: {
          DEFAULT: '#C94B6E',
          50: '#FAE8ED',
          100: '#F5D1DD',
          200: '#EAA3BC',
          300: '#DF759B',
          400: '#D44779',
          500: '#C94B6E',
          600: '#B13B5F',
          700: '#8F2E4C',
        },
        dark: '#1A1008',
        muted: '#8A7A72',
        gold: {
          DEFAULT: '#D4921E',
          50: '#FEF7E8',
          100: '#FCECC8',
          200: '#F9D994',
          300: '#F4C35D',
          400: '#E9AB33',
          500: '#D4921E',
          600: '#B07618',
          700: '#8A5B14',
        },
        orange: '#F59E0B',
        red: '#DC2626',
        // Soft pastel feminine palette
        rose: {
          50: '#fdf2f8',
          100: '#fce7f3',
          200: '#fbcfe8',
          300: '#f8b4d4',
          400: '#f472b6',
          500: '#ec4899',
          600: '#db2777',
          700: '#be185d',
          800: '#9d174d',
          900: '#831843',
        },
        cream: {
          DEFAULT: '#F2EDE8',
          50: '#fffbf0',
          100: '#fef3c7',
          200: '#fde68a',
          300: '#fcd34d',
        },
        sage: {
          50: '#f7fbf5',
          100: '#ecf9ee',
          200: '#d3f0dc',
          300: '#a8e6c1',
          400: '#7ed9b8',
          500: '#52ccaa',
          600: '#34a088',
          700: '#1e7861',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      spacing: {
        '18': '4.5rem',
        '22': '5.5rem',
      },
    },
  },
  plugins: [],
}
