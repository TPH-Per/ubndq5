/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#0068FF', // Zalo Blue
          dark: '#0054CC',
          light: '#E5F0FF',
        },
        success: '#00B14F',
        warning: '#FFA500',
        danger: '#DC3545',
        gray: {
          50: '#F9FAFB',
          100: '#F5F5F5',
          200: '#E0E0E0',
          300: '#D1D5DB',
          500: '#757575',
          700: '#374151',
          900: '#111827',
        },
        staff: {
          sidebar: '#003366',
          bg: '#F3F4F6',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        card: '0 2px 8px rgba(0,0,0,0.1)',
        'card-hover': '0 4px 12px rgba(0,0,0,0.15)',
      }
    },
  },
  plugins: [],
};
