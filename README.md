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
- 💰 **资产管理**：个人资产分类管理、心愿单管理、统计分析

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

### 💰 资产管理
- **资产分类管理**：支持多级分类，自定义分类图标和排序
- **资产信息管理**：记录资产名称、价格、购买日期、使用状态等
- **心愿单管理**：管理待购买物品，支持价格追踪和购买提醒
- **统计分析**：资产价值统计、分类分布、购买趋势分析
- **数据管理**：支持数据导入导出、备份恢复

### ⚙️ 系统设置
- **全局参数配置**：支持用户级和系统级配置管理，应用启动时自动缓存预热
- **用户管理**（管理员）：用户创建、角色分配（admin/ops/user），自动复制默认配置模板
- **定时任务管理**（管理员）：查看和管理系统定时任务执行状态

## 🛠️ 技术栈

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.24 | 前端框架 |
| TypeScript | 5.9.3 | 类型系统 |
| Vite | 7.2.4 | 构建工具 |
| Vue Router | 4.4.5 | 路由管理 |
| Pinia | 2.2.8 | 状态管理 |
| Axios | 1.7.9 | HTTP客户端 |
| Element Plus | 2.9.4 | UI组件库 |
| ECharts | 5.5.0 | 图表库 |
| XLSX | 0.18.5 | Excel处理 |
| Marked | 12.0.2 | Markdown解析 |
| YAML | 2.8.2 | YAML解析 |

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.2.0.RELEASE | 后端框架 |
| Java | 8 | 编程语言 |
| MyBatis | 2.2.0 | ORM框架 |
| MySQL | 5.7+ | 数据库 |
| Redis | 2.7.1 | 缓存 |
| Maven | 3.6+ | 构建工具 |
| JWT | 0.9.1 | 认证 |
| Swagger | 2.9.2 | API文档 |
| WebSocket | 2.4.5 | 实时通信 |
| Hutool | 5.7.18 | Java工具库 |
| Druid | 1.1.9 | 数据库连接池 |

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
CREATE DATABASE productivity_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本（按顺序执行）：
```bash
cd productivity-hub-server/script/sql/init
```

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

**API 文档**：
- Swagger UI：`http://localhost:9881/swagger-ui.html`
- 可用于查看和测试所有 API 接口

**默认管理员账号**：
- 用户名：`admin`
- 密码：请查看 `auth_user_init.sql` 中的初始化数据

### 前端启动

1. 进入前端目录：
```bash
cd productivity-hub-web
```

2. 安装依赖：
```bash
npm install
```

3. 配置环境变量（推荐）：
```bash
# 开发环境（自动创建 .env.development）
npm run env:dev

# 生产环境（自动创建 .env.production）
npm run env:prod

# 检查环境配置
npm run env:check
```

环境变量说明：
- `VITE_API_BASE_URL`：后端服务地址（用于图片资源、WebSocket 连接等）
- `VITE_PROXY_TARGET`：Vite 代理目标地址（开发环境使用）

默认开发环境配置：
```env
VITE_API_BASE_URL=http://127.0.0.1:9881
VITE_PROXY_TARGET=http://127.0.0.1:9881
```

详细配置说明请参考 [ENV_CONFIG.md](productivity-hub-web/ENV_CONFIG.md)

4. 启动开发服务器：
```bash
npm run dev
```

前端应用将在 `http://localhost:5173` 启动（默认 Vite 端口）。

5. 构建生产版本：
```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

## 🛠️ 工具列表

| 工具名称 | 路径 | 描述 | 使用场景 |
|---------|------|------|---------|
| AI成长蓝图 | `/tools/blueprint` | AI架构师成长路线可视化 | 职业规划、技能提升路线图 |
| Git 工具箱速查 | `/tools/git-toolbox` | 提交规范与常用命令 | 快速查阅 Git 命令和规范 |
| 今天吃什么 | `/tools/food` | 美食转盘决策工具 | 解决选择困难症 |
| JSON格式化 | `/tools/json` | 格式化、验证、压缩JSON数据 | API 调试、数据格式化 |
| 时间转换 | `/tools/time` | 时间戳与日期时间相互转换 | 时间戳转换、时区计算 |
| 屏幕时钟 | `/tools/clock` | 全屏数码时钟 | 会议、直播、倒计时显示 |
| 密码生成器 | `/tools/password` | 自定义字符集密码生成 | 生成强密码、随机字符串 |
| Cron表达式 | `/tools/cron` | Cron表达式解析和预览 | 定时任务配置、表达式验证 |
| 正则表达式 | `/tools/regex` | 正则表达式测试工具 | 正则编写、匹配测试 |
| YAML格式检查 | `/tools/yaml` | YAML格式校验 | 配置文件校验、格式检查 |
| 工作日计算 | `/tools/workday` | 工作日和节假日计算 | 工作日统计、假期计算 |
| Cursor商店库存 | `/tools/cursor-shop` | Cursor商店商品查询 | 查询 Cursor 商店商品库存 |
| 图片 Base64 编码 | `/tools/image-base64` | 图片转 Base64 字符串 | 图片内联、数据 URI 生成 |
| SQL 格式化 | `/tools/sql-formatter` | SQL 格式化/压缩与关键字大小写切换 | SQL 美化、代码规范 |
| 单位换算 | `/tools/unit-converter` | 常用物理量单位换算 | 单位转换、数值计算 |
| 低代码生成器 | `/code-generator` | 数据库解析、模板管理、代码一键生成 | 快速生成 CRUD 代码 |
| 行为级TODO | `/todo` | 模块化任务管理、时间追踪、数据统计 | 任务管理、时间追踪 |
| TODO任务大屏 | `/todo/dashboard` | 任务数据可视化大屏 | 任务统计、数据展示 |
| 资产管理 | `/asset` | 资产分类管理、心愿单管理、统计分析 | 个人资产管理、消费统计 |

## 📦 部署说明

### 前端部署

1. **构建生产版本**：
```bash
cd productivity-hub-web

# 标准构建
npm run build
```

2. **配置生产环境变量**：
```bash
# 创建生产环境配置文件
npm run env:prod

# 编辑 .env.production，修改后端服务地址
VITE_API_BASE_URL=http://your-production-server:9881
```

### 后端部署

1. **打包应用**：
```bash
cd productivity-hub-server
mvn clean package -DskipTests
```

2. **运行 JAR 包**：
```bash
# 开发环境
java -jar base-service-basic/target/base-service-basic-1.0-SNAPSHOT.jar

# 生产环境（指定 profile）
java -jar -Dspring.profiles.active=prod base-service-basic/target/base-service-basic-1.0-SNAPSHOT.jar

# 后台运行（Linux）
nohup java -jar -Dspring.profiles.active=prod base-service-basic/target/base-service-basic-1.0-SNAPSHOT.jar > app.log 2>&1 &
```

### 性能优化建议

#### 前端优化
- **代码分割**：已配置路由懒加载，大型组件自动分割
- **资源压缩**：生产构建自动压缩 JS/CSS
- **CDN 加速**：建议将静态资源部署到 CDN
- **缓存策略**：合理设置 HTTP 缓存头
- **图片优化**：使用 WebP 格式，压缩图片大小

#### 后端优化
- **数据库连接池**：已配置 Druid 连接池，可根据实际情况调整
- **Redis 缓存**：启用 Redis 缓存热点数据
- **接口限流**：已配置限流器，防止接口被滥用
- **SQL 优化**：使用索引，避免全表扫描
- **日志级别**：生产环境建议设置为 INFO 或 WARN

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 前端：遵循 Vue 3 官方风格指南，使用 ESLint 和 Prettier
- 后端：遵循 Java 编码规范，使用统一的代码格式化配置
- 提交信息：使用清晰的提交信息，遵循 Conventional Commits 规范

## ❓ 常见问题

### Q: 前端启动后无法连接后端？
**A:** 

1. 检查环境变量 `VITE_API_BASE_URL` 是否正确配置
2. 确认后端服务已启动（访问 `http://localhost:9881/actuator/health` 检查）
3. 检查浏览器控制台是否有 CORS 错误
4. 查看 `vite.config.ts` 中的代理配置是否正确

### Q: 数据库连接失败？
**A:**
1. 确认数据库已创建：`CREATE DATABASE productivity_hub;`
2. 检查 `application.yml` 中的数据库配置（URL、用户名、密码）
3. 确认数据库用户有相应权限
4. 检查数据库服务是否启动
5. 查看后端启动日志中的错误信息

### Q: Redis 连接失败？
**A:** 
- Redis 是可选的，如果不需要缓存功能，可以在配置中移除 Redis 相关配置
- 如果需要使用 Redis，确认 Redis 服务已启动，检查连接配置（host、port、password）

### Q: 如何修改默认端口？
**A:**
- **前端**：修改 `vite.config.ts` 中的 `server.port`，或使用 `npm run dev -- --port 3000`
- **后端**：修改 `application.yml` 中的 `server.port`，或使用 `--server.port=8080` 启动参数

### Q: WebSocket 连接失败？
**A:**
1. 确认后端服务已启动
2. 检查环境变量 `VITE_API_BASE_URL` 是否正确（WebSocket 地址基于此生成）
3. 检查防火墙是否阻止了 WebSocket 连接
4. 查看浏览器控制台的 WebSocket 连接错误信息

### Q: 构建产物过大？
**A:**
1. 运行 `npm run build:analyze` 分析构建产物
2. 检查是否有未使用的依赖
3. 确认代码分割配置正确
4. 考虑使用 CDN 加载大型第三方库（如 ECharts、Element Plus）

### Q: 接口请求被限流？
**A:**
1. 检查 `application.yml` 中的限流配置
2. 确认是否触发了限流阈值（用户级、接口级、IP级）
3. 可以临时调整限流配置或禁用限流（`rate-limit.enabled=false`）

### Q: 如何查看 API 文档？
**A:**
- 启动后端服务后，访问 `http://localhost:9881/swagger-ui.html`
- 可以在 Swagger UI 中直接测试接口

### Q: 生产环境如何配置？
**A:**
1. 修改 `application.yml` 中的 `spring.profiles.active=prod`
2. 创建 `application-prod.yml` 配置生产环境特定配置
3. 前端使用 `npm run env:prod` 创建生产环境变量文件
4. 修改生产环境的后端服务地址

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

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

#### 🤖 Dify 智能体

- [ ] 薪资计算
- [ ] 文档数据清洗与存储
- [ ] 日报、周报智能体
- [ ] 每日热点信息推送
- [ ] OCR 工具
- [ ] PPT 制作助手

#### 🎨 其他项目

- [ ] 食光业务迁移
