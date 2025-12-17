import { defineConfig } from 'vite'
import path from 'node:path'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        // Backend Spring Boot service is configured on port 9881
        target: 'http://localhost:9881',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
