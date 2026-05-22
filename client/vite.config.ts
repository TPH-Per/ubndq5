import path from 'path';
import { defineConfig, splitVendorChunkPlugin } from 'vite';
import react from '@vitejs/plugin-react';
import zaloMiniApp from 'zmp-vite-plugin';

export default defineConfig({
  base: '',
  plugins: [
    react(),
    splitVendorChunkPlugin(),
    zaloMiniApp(),
  ],
  build: {
    outDir: 'www',
    emptyOutDir: true,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  optimizeDeps: {
    exclude: ['lucide-react'],
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: process.env.VITE_DEV_BACKEND ?? 'http://127.0.0.1:8081',
        changeOrigin: true,
      },
    },
  },
});
