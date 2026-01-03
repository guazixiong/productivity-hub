# 工具函数使用说明

## 性能优化工具 (performance.ts)

### 防抖函数

```typescript
import { debounce } from '@/utils/performance'

const handleSearch = debounce((keyword: string) => {
  // 搜索逻辑
}, 300)

// 使用
handleSearch('keyword')
```

### 节流函数

```typescript
import { throttle } from '@/utils/performance'

const handleScroll = throttle(() => {
  // 滚动处理逻辑
}, 300)

// 使用
window.addEventListener('scroll', handleScroll)
```

### 图片懒加载

```typescript
import { lazyLoadImage } from '@/utils/performance'

const img = document.querySelector('img')
if (img) {
  lazyLoadImage(img, 'https://example.com/image.jpg')
}
```

## 请求优化工具 (requestOptimizer.ts)

### 请求缓存

```typescript
import { request } from '@/services/http'

// 启用缓存（默认缓存 5 分钟）
const data = await request({
  url: '/api/data',
  method: 'GET',
  cache: true,
  cacheTTL: 5 * 60 * 1000, // 可选：自定义缓存时间
})
```

### 请求取消

```typescript
import { request } from '@/services/http'
import { requestCancelManager } from '@/utils/requestOptimizer'

// 发送请求
const data = await request({
  url: '/api/data',
  method: 'GET',
  requestId: 'fetch-data', // 设置请求 ID
})

// 取消请求
requestCancelManager.cancel('fetch-data')
```

### 请求重试

```typescript
import { request } from '@/services/http'

// 启用重试（默认重试 3 次）
const data = await request({
  url: '/api/data',
  method: 'GET',
  retry: true,
})

// 自定义重试配置
const data = await request({
  url: '/api/data',
  method: 'GET',
  retry: {
    retries: 5,
    retryDelay: 1000,
    exponentialBackoff: true,
    retryCondition: (error) => {
      // 自定义重试条件
      return error.response?.status >= 500
    },
  },
})
```

## 用户体验工具 (userExperience.ts)

### 成功反馈

```typescript
import { showSuccessFeedback } from '@/utils/userExperience'

// Toast 提示（默认）
showSuccessFeedback('操作成功')

// 通知提示
showSuccessFeedback('操作成功', 'notification')

// 自定义选项
showSuccessFeedback('操作成功', 'toast', {
  duration: 3000,
  showClose: true,
})
```

### 错误反馈

```typescript
import { showErrorFeedback } from '@/utils/userExperience'

showErrorFeedback('操作失败')
```

### 警告反馈

```typescript
import { showWarningFeedback } from '@/utils/userExperience'

showWarningFeedback('请注意')
```

### 信息反馈

```typescript
import { showInfoFeedback } from '@/utils/userExperience'

showInfoFeedback('提示信息')
```

### 按钮点击反馈

```typescript
import { addButtonClickFeedback } from '@/utils/userExperience'

const button = document.querySelector('.my-button')
if (button) {
  addButtonClickFeedback(button as HTMLElement)
}
```

### 表单验证反馈

```typescript
import {
  addFormFieldFeedback,
  removeFormFieldFeedback,
} from '@/utils/userExperience'

// 添加错误反馈
const input = document.querySelector('.my-input')
if (input) {
  addFormFieldFeedback(input as HTMLElement, 'error')
}

// 移除反馈
removeFormFieldFeedback(input as HTMLElement)
```

### 操作成功后的反馈（带页面跳转）

```typescript
import { handleSuccessWithRedirect } from '@/utils/userExperience'
import { useRouter } from 'vue-router'

const router = useRouter()

handleSuccessWithRedirect('创建成功', '/list', router)
```

### 操作成功后的反馈（带状态更新）

```typescript
import { handleSuccessWithRefresh } from '@/utils/userExperience'

const refreshData = async () => {
  // 刷新数据逻辑
}

handleSuccessWithRefresh('更新成功', refreshData)
```

