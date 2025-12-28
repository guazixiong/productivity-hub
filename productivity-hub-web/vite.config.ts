import { defineConfig, loadEnv } from 'vite'
import path from 'node:path'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  // 获取后端服务地址，默认为本地开发地址
  const apiBaseUrl = env.VITE_PROXY_TARGET || env.VITE_API_BASE_URL || 'http://127.0.0.1:9881'
  
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
    build: {
      // 确保构建输出一致
      rollupOptions: {
        output: {
          // 确保 chunk 文件名格式一致
          chunkFileNames: 'assets/[name]-[hash].js',
          entryFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash].[ext]',
        },
      },
      // 确保所有资源都被正确包含
      assetsInlineLimit: 4096,
    },
    server: {
      proxy: {
        '/api': {
          // 使用环境变量配置后端服务地址
          target: apiBaseUrl,
          changeOrigin: true,
          secure: false,
        },
        '/uploads': {
          // 代理静态资源请求到后端服务器
          target: apiBaseUrl,
          changeOrigin: true,
          secure: false,
        },
        '/socketServer': {
          // 代理 WebSocket 连接到后端服务器
          target: apiBaseUrl,
          ws: true,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
