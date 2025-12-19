# 🚀 Productivity Hub V1.0

一个现代化的一站式生产力工具平台，集成了15+实用工具、行为级TODO管理、智能体管理、宝藏书签、消息中心、低代码生成器等功能模块。基于 Vue 3 + Spring Boot 构建，采用前后端分离架构，支持 WebSocket 实时通信，旨在为开发者和团队提供高效、便捷的工作流程支持。

**核心特性**：
- 🛠️ **丰富的工具集**：JSON/YAML/SQL格式化、时间转换、正则测试、密码生成等15+实用工具
- ✅ **行为级TODO**：模块化任务管理、任务状态流转、时间追踪、数据统计大屏
- 📑 **宝藏书签**：支持标签树、Excel导入导出、批量管理的智能书签系统
- 🤖 **智能体管理**：集成AI智能体，支持调用历史记录和统计分析
- 💬 **消息中心**：多渠道消息推送（钉钉/邮件），WebSocket实时通知
- 🔧 **低代码生成器**：数据库表结构解析，支持自定义模板，一键生成代码
- 🔥 **热点速览**：聚合技术/资讯热点，支持多源标签切换和内容预览
- ⚙️ **系统设置**：全局参数配置、用户管理、定时任务管理

## ✨ 功能特性

### 🏠 首页（工作台）
- **时间助手**：下班/午休倒计时、薪资日提醒、周末加班提示
- **天气信息**：基于地理位置显示实时天气
- **每日运势**：个性化运势与建议
- **热点速览**：技术/资讯热点分组展示，可按标签切换
- **快捷工具**：热门工具 Top8 一键直达（基于使用统计）
- **智能导航**：快速访问常用功能模块

### 🔥 热点速览
- 技术/资讯/生活热点分组浏览（如掘金、InfoQ、GitHub 等来源）
- 按标签懒加载数据，避免一次性全量请求
- 点击条目以弹窗 iframe 预览链接内容，减少跳转打断

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
- **单位换算**：常见物理量单位换算
- **低代码生成器**：
  - 数据库连接配置管理（支持多数据源）
  - 表结构自动解析（字段名、类型、注释等）
  - 代码模板自定义（Entity、Mapper、Service、Controller）
  - 一键生成代码文件或 ZIP 压缩包
  - 支持变量替换（表名、字段、包名等）
- **工具使用统计**：工具使用记录上报与统计（后台 `/api/tools/stats`）

### 📑 宝藏书签
- 标签树 + 子标签分组展示
- 网址分组管理、排序、批量移动/删除
- 关键词搜索与标签筛选
- Excel 导入导出，含模板下载

### 🤖 智能体管理
- 智能体列表展示
- 智能体调用功能
- 调用历史记录（分页查询）

### ✅ 行为级TODO
- **模块化管理**：支持创建多个任务模块，按模块分组管理任务
- **任务状态流转**：待处理 → 进行中 → 已完成，支持暂停/恢复
- **时间追踪**：自动记录任务开始/结束时间，支持暂停时长统计
- **优先级管理**：P0/P1/P2/P3 四级优先级，支持标签分类
- **任务大屏**：可视化数据统计大屏，展示任务分布和完成情况
- **行为事件记录**：完整记录任务状态变更历史

### 💬 消息中心
- 消息发送（支持多种渠道：DingTalk、SendGrid、Resend）
- 消息历史查询
- WebSocket 实时通知（前端小铃铛提醒），可附带跳转链接
- 定时任务：每日技术简报、Cursor 商店库存播报（钉钉）

### ⚙️ 系统设置
- **全局参数配置**：支持用户级和系统级配置管理，应用启动时自动缓存预热
- **用户管理**（管理员）：用户创建、角色分配（admin/ops/user），自动复制默认配置模板
- **定时任务管理**（管理员）：查看和管理系统定时任务执行状态

## 📁 项目结构

```
productivity-hub/
├── productivity-hub-web/          # 前端项目
│   ├── src/
│   │   ├── views/                  # 页面视图
│   │   │   ├── home/               # 首页
│   │   │   ├── tools/              # 工具箱
│   │   │   ├── bookmark/           # 宝藏书签
│   │   │   ├── agents/             # 智能体管理
│   │   │   ├── messages/           # 消息中心
│   │   │   ├── todo/               # 行为级TODO
│   │   │   ├── config/             # 配置管理
│   │   │   ├── settings/           # 系统设置
│   │   │   ├── hot-sections/       # 热点速览
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
    ├── base-service-basic/         # 基础业务服务模块
    │   ├── src/main/java/com/pbad/
    │   │   ├── agents/             # 智能体模块
    │   │   ├── auth/               # 认证模块
    │   │   ├── bookmark/           # 宝藏书签模块
    │   │   ├── config/             # 配置模块
    │   │   ├── home/               # 首页模块
    │   │   ├── messages/           # 消息模块
    │   │   ├── schedule/           # 定时任务模块
    │   │   ├── tools/              # 工具模块
    │   │   └── codegenerator/      # 低代码生成器模块
    │   └── src/main/resources/
    │       ├── application.yml     # 应用配置
    │       └── mapper/             # MyBatis Mapper
    │
    ├── base-service-thirdparty-api/ # 第三方API服务模块
    │   └── src/main/java/com/pbad/thirdparty/
    │       ├── dingtalk/           # 钉钉集成
    │       ├── resend/             # Resend邮件服务
    │       └── sendgrid/           # SendGrid邮件服务
    │
    ├── base-common/                # 公共模块
    │   ├── base-common-core/       # 核心公共模块
    │   ├── base-common-web/        # Web 公共模块
    │   ├── base-common-websocket/  # WebSocket 公共模块
    │   └── base-common-id-generator/ # ID 生成器模块
    │
    └── script/                     # 脚本目录
        └── sql/init/               # SQL 初始化脚本
            ├── business_demo.sql   # 业务表初始化（书签、代码生成器等）
            ├── id_generator.sql    # ID生成器表初始化
            ├── auth_user_init.sql  # 用户认证数据初始化
            ├── bookmark_init_data.sql # 书签示例数据
            └── todo_init.sql       # TODO模块表初始化
```

## 🚀 快速开始

### 环境要求

- **Node.js** >= 16.x
- **Java** 8
- **MySQL** 5.7+
- **Redis** (可选，用于缓存)
- **Maven** 3.6+

### 后端启动

1. 进入后端目录：
```bash
cd productivity-hub-server
```

2. 修改配置文件 `base-service-basic/src/main/resources/application.yml`：
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
cd base-service-basic
mvn spring-boot:run
```

后端服务将在 `http://localhost:9881` 启动。

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
| 行为级TODO | `/todo` | 模块化任务管理、时间追踪、数据统计 |
| TODO任务大屏 | `/todo/dashboard` | 任务数据可视化大屏 |

## 🌟 Star History

如果这个项目对你有帮助，欢迎给个 Star ⭐️

---

**注意**：
- 本项目仍在持续开发中，部分功能可能尚未完善
- 建议在生产环境使用前进行充分测试
- 欢迎提交 Issue 和 Pull Request 帮助改进项目

---

**更新日志**：详见下方 [版本更新](#版本更新) 章节

## 版本更新

### v2.0（规划 & TODO）

#### 🔒 安全与性能优化

- [ ] **安全相关配置**
  - [ ] 接口安全加固
  - [ ] 数据加密与脱敏
- [ ] **性能优化**
  - [ ] 接口幂等性处理
  - [ ] 数据批量处理优化
  - [ ] 缓存策略优化

#### 🎯 核心功能规划

- [ ] **首页大屏（v2.0）**
  - [ ] 支持选中后自动处理流程
- [ ] **AI 相关能力（v2.0）**
  - [ ] Dify 智能体模块
  - [ ] Prompt 模块
  - [ ] 知识库模块（多语义向量 / 图谱化 / 知识管理 / AI 搜索）
  - [ ] AI 生图
  - [ ] AI 员工：自动化完成所有相关任务
  - [ ] AI 统计图表 SQLBot 功能模仿与迁移
- [ ] **自动化流程（v2.0）**
  - [ ] 日常汇报自动化（年 / 月 / 日 / 季度）
  - [ ] TODO 任务自动流转（到点立即触发）

#### 📚 项目知识库

- [ ] 城市生命线
- [ ] 南阳 DeepSeek
- [ ] 投标项目

#### 🤖 Dify 智能体

- [ ] 薪资计算
- [ ] 文档数据清洗与存储
- [ ] 日报、周报智能体
- [ ] 每日热点信息推送
- [ ] OCR 工具
- [ ] PPT 制作助手

#### 🎨 其他项目

- [ ] 归物业务迁移
- [ ] 食光业务迁移

