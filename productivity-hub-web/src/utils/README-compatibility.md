# 兼容性工具使用说明

## 浏览器兼容性检测

### 检测浏览器信息

```typescript
import { getBrowserInfo } from '@/utils/compatibility'

const browser = getBrowserInfo()
console.log(browser.name) // 'chrome', 'firefox', 'safari', 'edge'
console.log(browser.version) // '80', '75', etc.
console.log(browser.isMobile) // true/false
console.log(browser.isIOS) // true/false
console.log(browser.isAndroid) // true/false
```

### 检测特性支持

```typescript
import { isFeatureSupported } from '@/utils/compatibility'

if (isFeatureSupported('Promise')) {
  // 使用 Promise
}

if (isFeatureSupported('fetch')) {
  // 使用 fetch API
}

if (isFeatureSupported('localStorage')) {
  // 使用 localStorage
}
```

### 检查浏览器兼容性

```typescript
import { checkBrowserCompatibility } from '@/utils/compatibility'

const isCompatible = checkBrowserCompatibility()
if (!isCompatible) {
  // 显示升级提示
}
```

### 加载 Polyfill（如果需要）

```typescript
import { loadPolyfills } from '@/utils/compatibility'

// 在应用启动前加载
await loadPolyfills()
```

## 浏览器支持范围

根据 `.browserslistrc` 配置，项目支持：

- **现代浏览器**：Chrome 80+, Firefox 75+, Safari 13+, Edge 80+
- **移动端**：iOS 12+, Android 6+
- **不支持**：IE 11 及以下版本

## 兼容性样式

项目提供了兼容性样式类，位于 `src/styles/compatibility.scss`：

- `.flex` - Flexbox 布局（带浏览器前缀）
- `.custom-scrollbar` - 自定义滚动条样式
- `.touch-action` - 触摸操作优化
- `.backdrop-blur` - 毛玻璃效果（带浏览器前缀）

## 注意事项

1. **Polyfill 依赖**：如果需要使用 Polyfill，需要安装相应的包：
   ```bash
   npm install core-js whatwg-fetch
   ```

2. **构建目标**：Vite 配置中设置了 `target: 'es2015'`，确保生成的代码兼容 ES2015+ 浏览器。

3. **移动端适配**：项目已针对移动端进行了优化，包括 Android WebView 和 iOS Safari 的特殊处理。

