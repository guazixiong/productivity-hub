# 环境配置快速参考

## 快速开始

1. **创建开发环境配置**：
   ```bash
   npm run env:dev
   ```

2. **创建生产环境配置**：
   ```bash
   npm run env:prod
   ```

3. **修改配置**：
   - 编辑 `.env.development`（开发环境）
   - 编辑 `.env.production`（生产环境）
   - 根据实际后端服务地址修改 `VITE_PROXY_TARGET` 和 `VITE_API_BASE_URL`

## 配置说明

- `VITE_PROXY_TARGET`: Vite 开发服务器代理目标（开发环境使用）
- `VITE_API_BASE_URL`: 后端服务基础地址（用于图片资源等）

## 详细文档

查看 [ENV_CONFIG.md](./ENV_CONFIG.md) 获取完整配置说明。

