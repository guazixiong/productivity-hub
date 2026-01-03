# 通用组件使用说明

## SkeletonLoader - 骨架屏组件

用于显示加载状态的骨架屏组件。

### 基本用法

```vue
<template>
  <SkeletonLoader :loading="loading" :rows="3" variant="card">
    <div>实际内容</div>
  </SkeletonLoader>
</template>

<script setup lang="ts">
import SkeletonLoader from '@/components/common/SkeletonLoader.vue'

const loading = ref(true)
</script>
```

### Props

- `loading`: 是否显示加载状态（默认：true）
- `rows`: 骨架屏行数（默认：3）
- `variant`: 骨架屏变体，可选值：'card' | 'list' | 'table' | 'default'（默认：'default'）
- `rowHeight`: 行高（像素）（默认：20）

## EmptyState - 空状态组件

用于显示空数据状态的组件。

### 基本用法

```vue
<template>
  <EmptyState
    description="暂无数据"
    icon="box"
    size="medium"
    :show-action="true"
    action-text="刷新"
    @action="handleRefresh"
  />
</template>

<script setup lang="ts">
import EmptyState from '@/components/common/EmptyState.vue'

const handleRefresh = () => {
  // 刷新数据
}
</script>
```

### Props

- `description`: 描述文本（默认：'暂无数据'）
- `icon`: 图标类型，可选值：'document' | 'box' | 'search' | 'folder' | 'custom'（默认：'box'）
- `size`: 尺寸，可选值：'small' | 'medium' | 'large'（默认：'medium'）
- `actionText`: 操作按钮文本
- `showAction`: 是否显示操作按钮（默认：false）

### Slots

- `icon`: 自定义图标
- `default`: 自定义描述内容
- `action`: 自定义操作按钮

## VirtualList - 虚拟滚动列表组件

用于优化长列表渲染性能的虚拟滚动组件。

### 基本用法

```vue
<template>
  <VirtualList
    :items="items"
    :item-height="60"
    :container-height="400"
    :buffer-size="5"
  >
    <template #default="{ item, index }">
      <div class="list-item">{{ item.name }}</div>
    </template>
  </VirtualList>
</template>

<script setup lang="ts">
import VirtualList from '@/components/common/VirtualList.vue'

const items = ref([
  { id: 1, name: 'Item 1' },
  { id: 2, name: 'Item 2' },
  // ... 更多数据
])
</script>
```

### Props

- `items`: 数据列表（必需）
- `itemHeight`: 每项高度（像素）（必需）
- `containerHeight`: 容器高度（像素）（默认：400）
- `bufferSize`: 缓冲区大小（默认：5）
- `getItemKey`: 获取唯一键的函数（默认：使用索引）

### 使用场景

适用于渲染大量数据（> 100 条）的列表，可以显著提升性能。

