import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: process.env.BACKEND_ORIGIN ?? 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api/v1'),
      },
      '/auth': {
        target: process.env.AUTH_ORIGIN ?? 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/auth/, '/api/v1/auth'),
      },
    },
  },
})
