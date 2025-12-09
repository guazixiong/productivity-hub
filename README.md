# 🚀 Productivity Hub

一个现代化的生产力工具集，集成了多种实用工具、智能体管理和消息中心等功能，旨在提升日常工作效率。

## 📋 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [开发指南](#开发指南)
- [API 文档](#api-文档)
- [工具列表](#工具列表)

## ✨ 功能特性

### 🏠 首页
- **时间助手**：下班/午休倒计时、薪资日提醒、周末加班提示
- **天气信息**：基于地理位置显示实时天气
- **每日运势**：个性化运势与建议
- **热点速览**：技术/资讯热点分组展示，可按标签切换
- **快捷工具**：热门工具 Top5 一键直达

### 🛠️ 工具箱
提供多种实用工具，包括：
- **AI成长蓝图**：AI架构师成长路线可视化
- **Git 工具箱速查**：提交规范与常用命令速览
- **今天吃什么**：美食转盘决策工具
- **JSON格式化**：格式化、验证、压缩JSON数据
- **时间转换**：时间戳与日期时间相互转换
- **屏幕时钟**：全屏数码时钟，会议/直播/倒计时必备
- **密码生成器**：自定义字符集，一键生成强密码
- **Cron表达式**：解析表达式并预览未来执行时间
- **正则表达式**：实时匹配与替换，支持多模式
- **YAML格式检查**：校验YAML并提示错误位置
- **工作日计算**：计算工作日、节假日判断
- **Cursor商店库存**：Cursor商店商品库存查询
- **图片 Base64 编码**：图片转 Base64 字符串
- **SQL 格式化**：格式化/压缩 SQL，关键字大小写切换
- **单位换算**：长度、面积、体积、时间、角度、速度、温度、压力、能量、功率等换算
- **低代码生成器**：多数据源表解析、模板管理，一键生成代码/ZIP

### 🤖 智能体管理
- 智能体列表展示
- 智能体调用功能
- 调用历史记录（分页查询）

### 💬 消息中心
- 消息发送（支持多种渠道：DingTalk、SendGrid、Resend）
- 消息历史查询
- 消息管理

### ⚙️ 配置管理
- 全局参数配置
- 配置项增删改查

### 🔐 认证授权
- 用户登录
- 密码重置
- JWT Token 认证

## 🛠️ 技术栈

### 前端 (productivity-hub-web)
- **框架**：Vue 3 + TypeScript
- **构建工具**：Vite
- **UI 组件库**：Element Plus
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP 客户端**：Axios
- **其他**：
  - Marked (Markdown 解析)
  - YAML (YAML 解析)
  - Cron Parser (Cron 表达式解析)
  - Cronstrue (Cron 表达式转自然语言)
  - Day.js (日期处理)

### 后端 (productivity-hub-server)
- **框架**：Spring Boot 2.2.0
- **语言**：Java 8
- **数据库**：MySQL 5.7
- **缓存**：Redis
- **ORM**：MyBatis 2.2.0
- **其他**：
  - Druid (数据库连接池)
  - SendGrid (邮件服务)
  - Resend (邮件服务)
  - Apache HttpClient (HTTP 客户端)
  - 雪花算法 (分布式ID生成)

## 📁 项目结构

```
productivity-hub/
├── productivity-hub-web/          # 前端项目
│   ├── src/
│   │   ├── views/                  # 页面视图
│   │   │   ├── home/               # 首页
│   │   │   ├── tools/              # 工具箱
│   │   │   ├── agents/             # 智能体管理
│   │   │   ├── messages/           # 消息中心
│   │   │   ├── config/             # 配置管理
│   │   │   └── auth/               # 认证相关
│   │   ├── components/             # 组件
│   │   ├── layouts/                # 布局
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── services/               # API 服务
│   │   ├── router/                 # 路由配置
│   │   └── types/                  # TypeScript 类型定义
│   └── package.json
│
└── productivity-hub-server/        # 后端项目
    ├── base-business-demo/         # 业务模块
    │   ├── src/main/java/com/pbad/
    │   │   ├── agents/             # 智能体模块
    │   │   ├── auth/                # 认证模块
    │   │   ├── config/              # 配置模块
    │   │   ├── home/                # 首页模块
    │   │   ├── messages/            # 消息模块
    │   │   ├── schedule/            # 定时任务模块
    │   │   ├── tools/               # 工具模块
    │   │   └── wechat/              # 微信模块
    │   └── src/main/resources/
    │       ├── application.yml      # 应用配置
    │       └── mapper/              # MyBatis Mapper
    │
    └── base-common/                 # 公共模块
        ├── base-common-core/        # 核心公共模块
        ├── base-common-web/         # Web 公共模块
        ├── base-common-websocket/   # WebSocket 公共模块
        └── base-common-id-generator/ # ID 生成器模块
```

## 🚀 快速开始

### 环境要求

- **Node.js** >= 16.x
- **Java** 8
- **MySQL** 5.7+
- **Redis** (可选，用于缓存)
- **Maven** 3.6+

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE productivity_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
# 执行 SQL 初始化脚本
mysql -u root -p productivity_hub < productivity-hub-server/script/sql/init/business_demo.sql
mysql -u root -p productivity_hub < productivity-hub-server/script/sql/init/id_generator.sql
```

### 后端启动

1. 进入后端目录：
```bash
cd productivity-hub-server
```

2. 修改配置文件 `base-business-demo/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/productivity_hub?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true
    username: your_username
    password: your_password
  redis:
    host: 127.0.0.1
    port: 6379
    password: your_redis_password  # 如果有密码
```

3. 编译并启动：
```bash
mvn clean install
cd base-business-demo
mvn spring-boot:run
```

后端服务将在 `http://localhost:9888` 启动。

### 前端启动

1. 进入前端目录：
```bash
cd productivity-hub-web
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端应用将在 `http://localhost:5173` 启动（默认 Vite 端口）。

4. 构建生产版本：
```bash
npm run build
```

## ⚙️ 配置说明

### 后端配置

主要配置文件：`productivity-hub-server/base-business-demo/src/main/resources/application.yml`

#### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/productivity_hub?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true
    username: root
    password: root
```

#### Redis 配置
```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password:  # 可选
```

#### 无需鉴权的接口路径
```yaml
common:
  web:
    exclude-paths: /api/auth/login,/api/schedule/**
```

### 前端配置

前端 API 基础 URL 配置在 `productivity-hub-web/src/services/http.ts` 中，默认指向 `http://localhost:9888`。

## 💻 开发指南

### 后端开发

1. **添加新的 Controller**：
   - 在 `base-business-demo/src/main/java/com/pbad/` 下创建对应的模块目录
   - 创建 Controller、Service、Mapper 等文件
   - 遵循现有的代码结构和命名规范

2. **数据库操作**：
   - 使用 MyBatis Mapper XML 文件定义 SQL
   - Mapper XML 文件位于 `src/main/resources/mapper/`
   - 实体类使用 `PO` (Persistent Object) 后缀

3. **API 响应格式**：
   - 统一使用 `ApiResponse<T>` 包装响应
   - 成功：`ApiResponse.ok(data)`
   - 失败：`ApiResponse.fail(message)`

### 前端开发

1. **添加新页面**：
   - 在 `src/views/` 下创建对应的视图组件
   - 在 `src/router/index.ts` 中添加路由配置
   - 如需状态管理，在 `src/stores/` 下创建对应的 store

2. **API 调用**：
   - 在 `src/services/api.ts` 中定义 API 方法
   - 使用 `request<T>()` 函数发送请求
   - 类型定义在 `src/types/` 目录下

3. **添加新工具**：
   - 在 `src/data/tools.ts` 中添加工具元数据
   - 在 `src/views/tools/` 下创建工具视图组件
   - 在路由中添加对应的路由配置

## 📚 API 文档

### 认证相关

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/reset-password` - 重置密码

### 首页相关

- `GET /api/home/weather` - 获取天气信息
- `GET /api/home/hot-sections` - 获取热点数据
- `GET /api/home/hot-sections/names` - 获取热点标签列表
- `GET /api/home/hot-sections/section` - 获取指定标签热点

### 工具相关

- `GET /api/tools/stats` - 获取工具使用统计
- `POST /api/tools/track` - 记录工具使用
- `GET /api/tools/cursor-shop/commodities` - 获取商品库存

### 智能体相关

- `GET /api/agents` - 获取智能体列表
- `POST /api/agents/invoke` - 调用智能体
- `GET /api/agents/logs` - 获取调用历史（分页）

### 消息相关

- `POST /api/messages/send` - 发送消息
- `GET /api/messages/history` - 获取消息历史

### 配置相关

- `GET /api/config` - 获取配置列表
- `PUT /api/config` - 更新配置项
- `POST /api/config` - 创建配置项
- `DELETE /api/config/:id` - 删除配置项

### Cursor商店相关

- `GET /api/tools/cursor-shop/commodities` - 获取商品库存

### 低代码生成相关

- `GET /api/code-generator/templates` - 查询模板列表
- `POST /api/code-generator/templates` - 新增/更新模板
- `DELETE /api/code-generator/templates/{id}` - 删除模板
- `GET /api/code-generator/database-configs` - 查询数据库配置列表
- `POST /api/code-generator/database-configs` - 新增/更新数据库配置
- `DELETE /api/code-generator/database-configs/{id}` - 删除数据库配置
- `POST /api/code-generator/parse-table` - 解析表结构
- `POST /api/code-generator/generate` - 生成代码（返回文件列表）
- `POST /api/code-generator/generate/zip` - 生成代码压缩包

## 🛠️ 工具列表

| 工具名称 | 路径 | 描述 |
|---------|------|------|
| AI成长蓝图 | `/tools/blueprint` | AI架构师成长路线可视化 |
| Git 工具箱速查 | `/tools/git-toolbox` | 提交规范与常用命令 |
| 今天吃什么 | `/tools/food` | 美食转盘决策工具 |
| JSON格式化 | `/tools/json` | 格式化、验证、压缩JSON数据 |
| 时间转换 | `/tools/time` | 时间戳与日期时间相互转换 |
| 屏幕时钟 | `/tools/clock` | 全屏数码时钟 |
| 密码生成器 | `/tools/password` | 自定义字符集密码生成 |
| Cron表达式 | `/tools/cron` | Cron表达式解析和预览 |
| 正则表达式 | `/tools/regex` | 正则表达式测试工具 |
| YAML格式检查 | `/tools/yaml` | YAML格式校验 |
| 工作日计算 | `/tools/workday` | 工作日和节假日计算 |
| Cursor商店库存 | `/tools/cursor-shop` | Cursor商店商品查询 |
| 图片 Base64 编码 | `/tools/image-base64` | 图片转 Base64 字符串 |
| SQL 格式化 | `/tools/sql-formatter` | SQL 格式化/压缩与关键字大小写切换 |
| 单位换算 | `/tools/unit-converter` | 常用物理量单位换算 |
| 低代码生成器 | `/code-generator` | 数据库解析、模板管理、代码一键生成 |

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题或建议，请通过 Issue 反馈。

---

**注意**：本项目仍在持续开发中，部分功能可能尚未完善。如有问题，欢迎反馈。
