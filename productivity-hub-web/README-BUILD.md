# 构建优化说明

## 构建命令

### 标准构建

```bash
npm run build
```

### 构建并分析

```bash
npm run build:analyze
```

构建完成后会自动分析构建产物大小，显示：
- 总大小
- 按文件类型分类的大小
- 最大的 10 个文件

## 构建优化特性

### 1. 代码分割

- **Vue 核心库**：单独打包为 `vue-vendor`
- **Element Plus**：单独打包为 `element-plus-vendor`
- **ECharts**：单独打包为 `echarts-vendor`
- **Axios**：单独打包为 `axios-vendor`
- **Pinia**：单独打包为 `pinia-vendor`
- **其他第三方库**：打包为 `vendor`
- **大型视图组件**：单独打包为 `views`

### 2. Tree Shaking

- 自动移除未使用的代码
- 优化模块副作用
- 优化属性读取副作用

### 3. 压缩优化

- **JavaScript**：使用 esbuild 压缩（生产环境）
- **CSS**：使用 esbuild 压缩（生产环境）
- **移除注释**：生产环境自动移除
- **移除 console 和 debugger**：生产环境自动移除

### 4. 资源优化

- **图片**：小于 4KB 的图片内联为 base64
- **字体**：单独打包到 `assets/fonts/` 目录
- **图片**：单独打包到 `assets/images/` 目录
- **其他资源**：打包到 `assets/` 目录

### 5. 构建性能

- **禁用压缩大小报告**：加快构建速度
- **依赖预构建**：优化常用依赖的预构建
- **Source Map**：生产环境关闭，开发环境可选

## 环境变量

构建时会根据环境变量进行优化：

- `VITE_PROXY_TARGET`：后端服务地址（开发环境）
- `VITE_API_BASE_URL`：API 基础地址

## 构建产物结构

```
dist/
├── assets/
│   ├── images/          # 图片资源
│   ├── fonts/           # 字体资源
│   ├── vue-vendor-*.js  # Vue 核心库
│   ├── element-plus-vendor-*.js  # Element Plus
│   ├── echarts-vendor-*.js  # ECharts
│   ├── axios-vendor-*.js  # Axios
│   ├── pinia-vendor-*.js  # Pinia
│   ├── vendor-*.js      # 其他第三方库
│   ├── views-*.js       # 大型视图组件
│   └── *.css            # 样式文件
├── index.html
└── ...
```

## 性能建议

1. **代码分割**：合理使用路由懒加载，避免一次性加载所有代码
2. **资源优化**：压缩图片，使用 WebP 格式
3. **缓存策略**：利用浏览器缓存，合理设置资源缓存时间
4. **CDN**：生产环境建议使用 CDN 加速静态资源

## 构建分析

运行 `npm run build:analyze` 后，会显示详细的构建分析报告，包括：
- 总构建大小
- 各文件类型的大小分布
- 最大的文件列表

根据分析结果，可以进一步优化构建配置。

