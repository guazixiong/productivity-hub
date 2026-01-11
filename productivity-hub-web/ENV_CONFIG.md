# 环境变量配置说明

本项目使用环境变量来管理不同环境的后端服务地址配置，方便开发和部署。

## 配置文件

项目需要以下环境变量配置文件：

- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置
- `.env.example` - 配置示例文件（可提交到 Git）

## 配置变量说明

### VITE_API_BASE_URL
后端服务的基础地址，用于：
- **生产环境中的所有 API 请求**（`http.ts` - 生产环境必须配置）
- 图片资源 URL 拼接（`imageUtils.ts`）
- WebSocket 连接地址（自动转换为 ws:// 或 wss://，`notifications.ts`）
- 其他需要完整 URL 的场景

**重要**：
1. **生产环境必须配置此变量**：在生产环境中，所有 API 请求（包括 `/api` 和 `/acl` 路径）都会使用此地址，因为生产环境没有 Vite 代理服务器。
2. 如果前端和后端服务运行在不同的端口，必须配置此变量以确保 API 请求和 WebSocket 连接到正确的服务器地址。
3. 如果未配置，生产环境会使用默认值 `http://127.0.0.1:9881`，这通常会导致 API 请求失败。

### VITE_PROXY_TARGET
Vite 开发服务器的代理目标地址，用于：
- `/api` 路径的 API 请求代理
- `/uploads` 路径的静态资源代理
- `/socketServer` 路径的 WebSocket 连接代理

## 配置示例

### 开发环境 (.env.development)
```env
# 开发环境配置
# 后端服务地址（开发环境）
VITE_API_BASE_URL=http://127.0.0.1:9881

# Vite 代理目标地址（开发环境使用）
VITE_PROXY_TARGET=http://127.0.0.1:9881
```

### 生产环境 (.env.production)
```env
# 生产环境配置
# 后端服务地址（生产环境）
# 请根据实际部署情况修改此地址
VITE_API_BASE_URL=http://117.72.32.111:9881

# Vite 代理目标地址（生产环境通常不需要代理，但保留此配置）
VITE_PROXY_TARGET=http://117.72.32.111:9881
```

## 使用方法

### 方法一：使用脚本快速创建（推荐）

项目提供了便捷的脚本命令来快速创建环境变量文件：

```bash
# 创建开发环境配置文件
npm run env:dev

# 创建生产环境配置文件
npm run env:prod
```

脚本会自动创建对应的 `.env.development` 或 `.env.production` 文件，并填充默认配置值。

### 方法二：手动创建

1. **开发环境**：
   - 创建 `.env.development` 文件
   - 复制上述开发环境配置内容
   - 根据实际情况修改后端地址

2. **生产环境**：
   - 创建 `.env.production` 文件
   - 复制上述生产环境配置内容
   - 根据实际部署情况修改后端地址

3. **首次使用**：
   - 可以复制 `.env.example` 文件并重命名为对应的环境文件
   - 然后根据实际情况修改配置值

## 注意事项

1. `.env.development` 和 `.env.production` 文件不应提交到 Git（已在 `.gitignore` 中排除）
2. `.env.example` 文件可以提交到 Git，作为配置模板
3. 修改环境变量后，需要重启 Vite 开发服务器才能生效
4. 生产环境构建时，Vite 会根据 `--mode` 参数自动加载对应的环境变量文件

## 构建命令

- 开发环境：`npm run dev`（自动加载 `.env.development`）
- 生产构建：`npm run build`（自动加载 `.env.production`）

