import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/admin/',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5174,
    host: '0.0.0.0',
    open: true,
    proxy: {
      '/api': {
        target: process.env.VITE_DEV_BACKEND ?? 'http://127.0.0.1:8081',
        changeOrigin: true,
      },
    },
  },
})
