import { defineConfig, loadEnv } from 'vite'
import path from 'node:path'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  // 获取后端服务地址，默认为本地开发地址
  const apiBaseUrl = env.VITE_PROXY_TARGET || env.VITE_API_BASE_URL || 'http://127.0.0.1:9881'
  
  // 是否为生产环境
  const isProduction = mode === 'production'
  
  return {
    // 设置基础路径为相对路径，确保在 APK 中能正确加载资源
    base: './',
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
    // 优化依赖预构建
    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'axios',
        'element-plus',
        'echarts',
        'vue-echarts',
        'cronstrue', // 包含 cronstrue 以确保正确预构建
        'core-js/stable/promise',
        'core-js/stable/object/assign',
        'core-js/stable/array/from',
        'core-js/stable/array/includes',
        'core-js/stable/string/includes',
        'whatwg-fetch',
      ],
      exclude: ['cron-parser'], // 排除 cron-parser 从预构建，使用动态导入以避免兼容性问题
    },
    build: {
      // 输出目录
      outDir: 'dist',
      // 确保构建输出一致
      commonjsOptions: {
        // 确保 CommonJS 模块被正确转换
        transformMixedEsModules: true,
        // 包含需要转换的模块
        include: [/node_modules/],
      },
      rollupOptions: {
        output: {
          // 确保 chunk 文件名格式一致
          format: 'es',
          interop: 'compat', // 使用 compat 模式更好地处理 CommonJS/ESM 互操作
          chunkFileNames: 'assets/[name]-[hash].js',
          entryFileNames: 'assets/[name]-[hash].js',
          // 确保动态导入的模块被正确处理
          inlineDynamicImports: false,
          preserveModules: false,
          // 确保模块加载顺序正确，避免初始化顺序问题
          // 使用更保守的代码生成策略
          generatedCode: {
            constBindings: false, // 不使用 const，避免 TDZ (Temporal Dead Zone) 问题
            objectShorthand: false, // 不使用对象简写，避免压缩问题
          },
          assetFileNames: (assetInfo) => {
            const info = assetInfo.name?.split('.') || []
            const ext = info[info.length - 1]
            if (/\.(png|jpe?g|svg|gif|tiff|bmp|ico)$/i.test(assetInfo.name || '')) {
              return `assets/images/[name]-[hash].${ext}`
            }
            if (/\.(woff2?|eot|ttf|otf)$/i.test(assetInfo.name || '')) {
              return `assets/fonts/[name]-[hash].${ext}`
            }
            return `assets/[name]-[hash].${ext}`
          },
          // 代码分割策略 - 优化 chunk 大小
          manualChunks: (id) => {
            // 第三方库单独打包
            if (id.includes('node_modules')) {
              // Vue 核心库必须最先加载（router, vue）
              // 确保 Vue 在 Element Plus 之前加载，避免初始化顺序问题
              if (id.includes('vue') && !id.includes('vue-router') && !id.includes('vue-echarts') && !id.includes('@vue')) {
                return 'vue-vendor'
              }
              if (id.includes('vue-router')) {
                return 'vue-router-vendor'
              }
              // Element Plus（大型 UI 库，单独打包，但依赖 Vue）
              // 注意：Element Plus 依赖 Vue，所以 Vue 必须在之前加载
              if (id.includes('element-plus')) {
                return 'element-plus-vendor'
              }
              // ECharts（大型图表库，单独打包）
              if (id.includes('echarts') || id.includes('vue-echarts')) {
                return 'echarts-vendor'
              }
              // Excel 处理库（xlsx，按需加载）
              if (id.includes('xlsx')) {
                return 'xlsx-vendor'
              }
              // Markdown 和 YAML 解析器
              if (id.includes('marked')) {
                return 'marked-vendor'
              }
              if (id.includes('yaml')) {
                return 'yaml-vendor'
              }
              // Cron 相关 - 确保 cronstrue 被正确打包
              // 注意：cron-parser 使用动态导入，不在这里处理，避免兼容性问题
              if (id.includes('cronstrue')) {
                return 'cronstrue-vendor'
              }
              // Axios（HTTP 客户端）
              if (id.includes('axios')) {
                return 'axios-vendor'
              }
              // Pinia（状态管理）
              if (id.includes('pinia')) {
                return 'pinia-vendor'
              }
              // Core-js（polyfills，可能较大）
              if (id.includes('core-js')) {
                return 'core-js-vendor'
              }
              // 其他第三方库
              return 'vendor'
            }
            
            // 按功能模块分割视图
            if (id.includes('/views/')) {
              // AI 相关视图
              if (id.includes('/views/ai/')) {
                return 'views-ai'
              }
              // 资产相关视图
              if (id.includes('/views/asset/')) {
                return 'views-asset'
              }
              // 健康相关视图
              if (id.includes('/views/health/')) {
                return 'views-health'
              }
              // 工具相关视图
              if (id.includes('/views/tools/')) {
                return 'views-tools'
              }
              // 消息相关视图
              if (id.includes('/views/messages/')) {
                return 'views-messages'
              }
              // 书签相关视图
              if (id.includes('/views/bookmark/')) {
                return 'views-bookmark'
              }
              // 其他视图
              return 'views-common'
            }
            
            // 大型组件单独分割
            if (id.includes('/components/')) {
              // 健康相关组件（可能包含图表）
              if (id.includes('/components/health/')) {
                return 'components-health'
              }
              // 资产相关组件
              if (id.includes('/components/asset/')) {
                return 'components-asset'
              }
            }
          },
        },
        // Tree Shaking 优化
        // 使用更保守的配置，避免过度优化导致的初始化顺序问题
        treeshake: {
          moduleSideEffects: 'no-external', // 只对 node_modules 进行 tree shaking
          propertyReadSideEffects: true, // 保留属性读取的副作用，避免初始化顺序问题
          tryCatchDeoptimization: false,
          // 不使用 preset，手动配置更安全
        },
      },
      // 确保所有资源都被正确包含
      assetsInlineLimit: 4096,
      // 启用 CSS 代码拆分
      cssCodeSplit: true,
      // 生成 source map（生产环境可关闭，开发环境可选）
      sourcemap: !isProduction,
      // 压缩配置（使用 esbuild，Vite 默认，更快）
      // 注意：esbuild 压缩可能导致变量初始化顺序问题，使用更保守的配置
      minify: isProduction ? 'esbuild' : false,
      // esbuild 压缩选项
      esbuild: {
        drop: isProduction ? ['console', 'debugger'] : [],
        legalComments: 'none', // 移除注释
        // 保持变量名，避免过度压缩导致的初始化顺序问题
        keepNames: true, // 保持函数和类名，有助于调试和避免压缩问题
      },
      // CSS 压缩配置
      cssMinify: isProduction ? 'esbuild' : false,
      // 启用 chunk 大小警告阈值（KB）
      chunkSizeWarningLimit: 1000,
      // 构建性能优化
      reportCompressedSize: false, // 禁用压缩大小报告以加快构建速度
      // 目标浏览器（通过 browserslist 配置）
      target: 'es2015',
    },
    server: {
      proxy: {
        '/api': {
          // 使用环境变量配置后端服务地址
          target: apiBaseUrl,
          changeOrigin: true,
          secure: false,
        },
        '/acl': {
          // 代理 ACL 相关请求到后端服务器
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
