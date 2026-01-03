<template>
  <div class="empty-state" :class="[`empty-${size}`]">
    <div class="empty-content">
      <div class="empty-icon">
        <slot name="icon">
          <el-icon :size="iconSize">
            <component :is="iconComponent" />
          </el-icon>
        </slot>
      </div>
      <div class="empty-description">
        <slot>
          <span>{{ description }}</span>
        </slot>
      </div>
      <div v-if="showAction" class="empty-action">
        <slot name="action">
          <el-button v-if="actionText" type="primary" @click="handleAction">
            {{ actionText }}
          </el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 通用空状态组件
 * 提供统一的空数据展示
 */

import { Document, Box, Search, FolderOpened } from '@element-plus/icons-vue'

interface Props {
  /** 描述文本 */
  description?: string
  /** 图标类型 */
  icon?: 'document' | 'box' | 'search' | 'folder' | 'custom'
  /** 尺寸：small、medium、large */
  size?: 'small' | 'medium' | 'large'
  /** 操作按钮文本 */
  actionText?: string
  /** 是否显示操作按钮 */
  showAction?: boolean
}

interface Emits {
  (e: 'action'): void
}

const props = withDefaults(defineProps<Props>(), {
  description: '暂无数据',
  icon: 'box',
  size: 'medium',
  showAction: false,
})

const emit = defineEmits<Emits>()

const iconMap = {
  document: Document,
  box: Box,
  search: Search,
  folder: FolderOpened,
  custom: null,
}

const iconComponent = computed(() => {
  if (props.icon === 'custom') return null
  return iconMap[props.icon] || Box
})

const iconSize = computed(() => {
  const sizeMap = {
    small: 48,
    medium: 64,
    large: 96,
  }
  return sizeMap[props.size]
})

const handleAction = () => {
  emit('action')
}
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  min-height: 200px;

  .empty-content {
    text-align: center;

    .empty-icon {
      margin-bottom: 16px;
      color: #c0c4cc;
    }

    .empty-description {
      color: #909399;
      font-size: 14px;
      margin-bottom: 20px;
    }

    .empty-action {
      margin-top: 16px;
    }
  }

  &.empty-small {
    padding: 20px;
    min-height: 120px;

    .empty-content {
      .empty-icon {
        margin-bottom: 8px;
      }

      .empty-description {
        font-size: 12px;
        margin-bottom: 12px;
      }
    }
  }

  &.empty-large {
    padding: 60px 20px;
    min-height: 300px;

    .empty-content {
      .empty-icon {
        margin-bottom: 24px;
      }

      .empty-description {
        font-size: 16px;
        margin-bottom: 24px;
      }
    }
  }
}
</style>

