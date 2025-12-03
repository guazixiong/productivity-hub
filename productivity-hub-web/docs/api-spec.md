# API 接口规范文档

## 1. 概述

本文档描述了 Productivity Hub 前端管理系统的所有 API 接口规范。所有接口遵循统一的响应格式，使用 JSON 进行数据交换。

### 1.1 基础信息

- **Base URL**: `/api`
- **请求格式**: JSON
- **响应格式**: JSON
- **认证方式**: Bearer Token（通过 `Authorization` 请求头传递）

### 1.2 统一响应格式

所有接口响应遵循以下格式：

```typescript
interface ApiResponse<T> {
  code: number      // 状态码，0 表示成功，非 0 表示失败
  message: string   // 响应消息
  data: T          // 响应数据，类型由具体接口定义
}
```

### 1.3 状态码说明

| 状态码 | 说明 |
|--------|------|
| 0 | 请求成功 |
| 401 | 未授权（Token 无效或过期） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 2. 认证模块 (Auth)

### 2.1 用户登录

**接口地址**: `POST /api/auth/login`

**请求参数**:

```typescript
interface LoginPayload {
  username: string    // 用户名
  password: string    // 密码
  captcha?: string    // 验证码（可选）
}
```

**请求示例**:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应数据**:

```typescript
interface AuthResponse {
  token: string           // 访问令牌
  refreshToken: string    // 刷新令牌
  user: UserInfo         // 用户信息
}

interface UserInfo {
  id: string             // 用户ID
  name: string           // 用户名称
  roles: string[]        // 用户角色列表
  email?: string         // 邮箱（可选）
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "token": "mock-token-u_admin",
    "refreshToken": "mock-refresh-u_admin",
    "user": {
      "id": "u_admin",
      "name": "超级管理员",
      "roles": ["admin"],
      "email": "admin@productivity-hub.io"
    }
  }
}
```

**错误响应示例**:

```json
{
  "code": 401,
  "message": "账号或密码错误",
  "data": null
}
```

---

### 2.2 重置密码

**接口地址**: `POST /api/auth/reset-password`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**: 无

**响应数据**:

```typescript
interface ResetPasswordResponse {
  success: boolean    // 是否成功
  message: string     // 提示消息
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "密码已重置为默认密码 123456",
  "data": {
    "success": true,
    "message": "密码已重置为默认密码 123456"
  }
}
```

**说明**: 重置密码功能会将当前登录用户的密码重置为默认密码 `123456`。

---

## 3. 全局参数配置模块 (Config)

### 3.1 获取配置列表

**接口地址**: `GET /api/config`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**: 无

**响应数据**:

```typescript
interface ConfigItem {
  id: string          // 配置项ID
  module: string      // 所属模块
  key: string         // 配置键
  value: string       // 配置值
  description: string  // 配置描述
  updatedAt: string   // 更新时间
  updatedBy: string   // 更新人
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": "cfg_1",
      "module": "auth",
      "key": "token.expireDays",
      "value": "7",
      "description": "登录态 Token 过期时间（单位：天）",
      "updatedAt": "2025-11-28 10:00",
      "updatedBy": "系统"
    },
    {
      "id": "cfg_2",
      "module": "message",
      "key": "sendgrid.template",
      "value": "welcome-template-v3",
      "description": "默认 SendGrid 模板",
      "updatedAt": "2025-11-26 14:33",
      "updatedBy": "产品"
    }
  ]
}
```

---

### 3.2 更新配置项

**接口地址**: `PUT /api/config`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**:

```typescript
interface ConfigUpdatePayload {
  id: string           // 配置项ID
  value: string        // 新的配置值
  description?: string // 新的描述（可选）
}
```

**请求示例**:

```json
{
  "id": "cfg_1",
  "value": "30",
  "description": "登录态 Token 过期时间（单位：天）"
}
```

**响应数据**: 返回更新后的配置项（类型同 `ConfigItem`）

**响应示例**:

```json
{
  "code": 0,
  "message": "保存成功",
  "data": {
    "id": "cfg_1",
    "module": "auth",
    "key": "token.expireDays",
    "value": "30",
    "description": "登录态 Token 过期时间（单位：天）",
    "updatedAt": "2025-11-29 15:30:00",
    "updatedBy": "Mock Admin"
  }
}
```

---

## 4. 消息推送模块 (Messages)

### 4.1 发送消息

**接口地址**: `POST /api/messages/send`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**:

```typescript
interface BaseMessagePayload {
  channel: 'sendgrid' | 'dingtalk' | 'dify'  // 推送渠道
  data: Record<string, unknown>               // 渠道相关参数
}
```

**渠道参数说明**:

#### SendGrid 邮件 (`sendgrid`)

```typescript
{
  channel: 'sendgrid',
  data: {
    recipients: string      // 收件人（逗号分隔多个）
    subject: string         // 邮件主题
    content: string         // HTML 内容
    trackOpens?: boolean    // 是否开启打开追踪（可选）
  }
}
```

#### 钉钉 Webhook (`dingtalk`)

```typescript
{
  channel: 'dingtalk',
  data: {
    webhook: string         // Webhook 地址
    msgType: 'text' | 'markdown' | 'link'  // 消息类型
    content: string         // 消息内容
    atMobiles?: string      // @手机号（逗号分隔，可选）
  }
}
```

#### Dify 智能体 (`dify`)

```typescript
{
  channel: 'dify',
  data: {
    agentId: string        // 智能体 ID
    mode: 'sync' | 'async' // 执行模式
    context?: string        // 上下文（JSON 字符串，可选）
    input: string          // 输入内容
  }
}
```

**请求示例** (SendGrid):

```json
{
  "channel": "sendgrid",
  "data": {
    "recipients": "user@example.com,admin@example.com",
    "subject": "欢迎邮件",
    "content": "<h1>欢迎使用 Productivity Hub</h1>",
    "trackOpens": true
  }
}
```

**响应数据**:

```typescript
interface MessageSendResult {
  requestId: string                    // 请求ID
  status: 'success' | 'queued' | 'failed'  // 发送状态
  detail: string                       // 详细信息
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "requestId": "req-123456",
    "status": "success",
    "detail": "消息已送达（Mock）"
  }
}
```

---

### 4.2 获取推送历史

**接口地址**: `GET /api/messages/history`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**: 无

**响应数据**:

```typescript
interface MessageHistoryItem {
  id: string                    // 历史记录ID
  channel: 'sendgrid' | 'dingtalk' | 'dify'  // 推送渠道
  request: Record<string, unknown>  // 请求参数
  status: 'success' | 'failed'  // 发送状态
  response: string               // 响应内容
  createdAt: string             // 创建时间
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": "req-123456",
      "channel": "sendgrid",
      "request": {
        "subject": "欢迎邮件",
        "recipients": "team@company.com"
      },
      "status": "success",
      "response": "SendGrid accepted message",
      "createdAt": "2025-11-29 08:41"
    },
    {
      "id": "req-789012",
      "channel": "dingtalk",
      "request": {
        "msgType": "markdown",
        "title": "巡检报告"
      },
      "status": "failed",
      "response": "签名错误",
      "createdAt": "2025-11-27 16:20"
    }
  ]
}
```

---

## 5. 智能体调用模块 (Agents)

### 5.1 获取智能体列表

**接口地址**: `GET /api/agents`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**: 无

**响应数据**:

```typescript
interface AgentSummary {
  id: string          // 智能体ID
  name: string        // 智能体名称
  description: string // 智能体描述
  version: string     // 版本号
  tags: string[]      // 标签列表
  latencyMs: number   // 平均延迟（毫秒）
  owner: string       // 负责人
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": "agent-cs-bot",
      "name": "客服问答助手",
      "description": "根据 FAQ 与知识库自动回复客户问题",
      "version": "v2.4.1",
      "tags": ["客服", "QA"],
      "latencyMs": 1200,
      "owner": "智能体验组"
    },
    {
      "id": "agent-report",
      "name": "日报生成器",
      "description": "从埋点数据自动生成日报摘要与图表",
      "version": "v1.8.0",
      "tags": ["数据", "自动化"],
      "latencyMs": 2200,
      "owner": "数据中台"
    }
  ]
}
```

---

### 5.2 调用智能体

**接口地址**: `POST /api/agents/invoke`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**:

```typescript
interface AgentInvocationPayload {
  agentId: string                    // 智能体ID
  input: Record<string, unknown>      // 输入参数（JSON 对象）
  mode: 'sync' | 'async'             // 执行模式：同步/异步
  context?: string                    // 上下文（可选，JSON 字符串）
}
```

**请求示例**:

```json
{
  "agentId": "agent-cs-bot",
  "input": {
    "prompt": "问题：退款流程？"
  },
  "mode": "sync",
  "context": "{\"sessionId\": \"sess-123\"}"
}
```

**响应数据**:

```typescript
interface AgentInvocationResult {
  taskId: string                      // 任务ID
  status: 'success' | 'running' | 'failed'  // 执行状态
  output: string                       // 输出内容
  startedAt: string                    // 开始时间（ISO 8601）
  finishedAt?: string                  // 结束时间（ISO 8601，可选）
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "taskId": "task-123456",
    "status": "success",
    "output": "智能体响应：处理完成（Mock）",
    "startedAt": "2025-11-29T10:30:00.000Z",
    "finishedAt": "2025-11-29T10:30:01.200Z"
  }
}
```

**错误响应示例** (智能体不存在):

```json
{
  "code": 404,
  "message": "智能体不存在",
  "data": null
}
```

---

### 5.3 获取调用历史

**接口地址**: `GET /api/agents/logs`

**请求头**: 需要携带 `Authorization: Bearer {token}`

**请求参数**: 无

**响应数据**:

```typescript
interface AgentLogEntry {
  id: string                    // 日志ID
  agentId: string              // 智能体ID
  agentName: string            // 智能体名称
  status: 'success' | 'failed' | 'running'  // 执行状态
  duration: number             // 耗时（毫秒）
  createdAt: string            // 创建时间
  input: string                // 输入内容（字符串形式）
  output: string               // 输出内容
}
```

**响应示例**:

```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "id": "log-123456",
      "agentId": "agent-cs-bot",
      "agentName": "客服问答助手",
      "status": "success",
      "duration": 1020,
      "createdAt": "2025-11-28 09:30",
      "input": "问题：退款流程？",
      "output": "已返回标准退款流程"
    }
  ]
}
```

---

## 6. 错误处理

### 6.1 常见错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 0 | 成功 | - |
| 401 | 未授权 | 重新登录获取 Token |
| 404 | 资源不存在 | 检查请求参数 |
| 500 | 服务器错误 | 联系管理员或稍后重试 |

### 6.2 错误响应格式

```json
{
  "code": 401,
  "message": "账号或密码错误",
  "data": null
}
```

---

## 7. 认证与授权

### 7.1 Token 使用

所有需要认证的接口都需要在请求头中携带 Token：

```
Authorization: Bearer {token}
```

### 7.2 Token 刷新

当前版本暂不支持 Token 自动刷新，Token 过期后需要重新登录。

---

## 8. 接口调用示例

### 8.1 JavaScript/TypeScript (Axios)

```typescript
import axios from 'axios'

// 登录
const loginResponse = await axios.post('/api/auth/login', {
  username: 'admin',
  password: 'admin123'
})

const token = loginResponse.data.data.token

// 获取配置列表
const configResponse = await axios.get('/api/config', {
  headers: {
    Authorization: `Bearer ${token}`
  }
})
```

### 8.2 cURL

```bash
# 登录
curl -X POST http://localhost:5173/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 获取配置列表
curl -X GET http://localhost:5173/api/config \
  -H "Authorization: Bearer mock-token-u_admin"
```

---

## 9. 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-11-29 | 初始版本，包含所有基础接口 |

---

## 10. 注意事项

1. **Mock 数据**: 当前阶段所有接口使用 Mock 数据，实际后端实现时需保持接口规范一致。
2. **时间格式**: 时间字段统一使用 ISO 8601 格式或 `YYYY-MM-DD HH:mm` 格式。
3. **分页**: 当前版本暂不支持分页，后续版本将添加分页参数。
4. **文件上传**: 当前版本暂不支持文件上传功能。
5. **WebSocket**: 当前版本暂不支持实时推送，异步任务状态需通过轮询获取。

---

## 11. 联系方式

如有接口相关问题，请联系开发团队。

