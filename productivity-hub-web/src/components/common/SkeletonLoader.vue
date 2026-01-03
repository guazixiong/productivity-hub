<template>
  <div class="skeleton-loader" :class="[`skeleton-${variant}`]">
    <el-skeleton :loading="loading" animated :rows="rows">
      <template #template>
        <slot name="skeleton">
          <div v-for="i in rows" :key="i" class="skeleton-row" :style="rowStyle"></div>
        </slot>
      </template>
      <template #default>
        <slot></slot>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup lang="ts">
/**
 * 通用骨架屏组件
 * 提供统一的加载状态展示
 */

interface Props {
  /** 是否显示加载状态 */
  loading?: boolean
  /** 骨架屏行数 */
  rows?: number
  /** 骨架屏变体：card、list、table */
  variant?: 'card' | 'list' | 'table' | 'default'
  /** 行高（像素） */
  rowHeight?: number
}

const props = withDefaults(defineProps<Props>(), {
  loading: true,
  rows: 3,
  variant: 'default',
  rowHeight: 20,
})

const rowStyle = computed(() => ({
  height: `${props.rowHeight}px`,
  marginBottom: '12px',
}))
</script>

<style scoped lang="scss">
.skeleton-loader {
  width: 100%;

  .skeleton-row {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    border-radius: 4px;
  }

  &.skeleton-card {
    .skeleton-row {
      &:first-child {
        height: 24px;
        width: 60%;
      }
      &:nth-child(2) {
        height: 16px;
        width: 80%;
      }
      &:nth-child(3) {
        height: 16px;
        width: 40%;
      }
    }
  }

  &.skeleton-list {
    .skeleton-row {
      height: 60px;
      display: flex;
      align-items: center;
      gap: 12px;

      &::before {
        content: '';
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #f0f0f0;
        flex-shrink: 0;
      }
    }
  }

  &.skeleton-table {
    .skeleton-row {
      height: 48px;
      display: flex;
      gap: 16px;

      &::before,
      &::after {
        content: '';
        flex: 1;
        height: 100%;
        background: #f0f0f0;
        border-radius: 4px;
      }
    }
  }
}
</style>

