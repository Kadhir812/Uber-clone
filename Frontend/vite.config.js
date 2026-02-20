import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // Listen on all network interfaces for mobile access
    port: 3000,
    proxy: {
      '/api/rides': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/drivers': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/matching': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      }
    }
  }
})
