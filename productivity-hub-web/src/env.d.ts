/// <reference types="vite/client" />

/**
 * 环境变量类型定义
 * 这些变量需要在 .env.development 或 .env.production 文件中定义
 */
interface ImportMetaEnv {
  /**
   * 后端服务的基础地址
   * 用于：
   * - 图片资源 URL 拼接
   * - WebSocket 连接地址（自动转换为 ws:// 或 wss://）
   * - 其他需要完整 URL 的场景
   * 示例: http://127.0.0.1:9881 或 http://117.72.32.111:9881
   */
  readonly VITE_API_BASE_URL?: string

  /**
   * Vite 开发服务器代理目标地址
   * 用于开发环境的 API 请求代理
   * 示例: http://127.0.0.1:9881 或 http://117.72.32.111:9881
   */
  readonly VITE_PROXY_TARGET?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

