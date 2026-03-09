/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
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
