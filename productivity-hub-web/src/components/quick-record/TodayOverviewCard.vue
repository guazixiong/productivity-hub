<template>
  <el-card class="today-overview-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="title">📊 今日概览</span>
      </div>
    </template>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="skeleton-content">
          <el-skeleton-item variant="text" style="width: 100px; height: 20px; margin-bottom: 16px;" />
          <el-skeleton-item variant="text" style="width: 120px; height: 20px; margin-bottom: 16px;" />
          <el-skeleton-item variant="text" style="width: 80px; height: 20px;" />
        </div>
      </template>
      <template #default>
        <div class="overview-content">
          <div class="overview-item">
            <span class="label">待办:</span>
            <span class="value">{{ todoText }}</span>
          </div>
          <div class="overview-item">
            <span class="label">饮水:</span>
            <span class="value">{{ waterText }}</span>
          </div>
          <div class="overview-item">
            <span class="label">运动:</span>
            <span class="value">{{ exerciseText }}</span>
          </div>
        </div>
      </template>
    </el-skeleton>
  </el-card>
</template>

<script setup lang="ts">
/**
 * 今日概览卡片组件
 */

import { computed } from 'vue'

interface Props {
  todoStats?: {
    completed: number
    total: number
  }
  waterData?: {
    intake: number
    target: number
  }
  exerciseData?: {
    duration: number
  }
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
})

const todoText = computed(() => {
  if (!props.todoStats) {
    return '0/0'
  }
  return `${props.todoStats.completed || 0}/${props.todoStats.total || 0}`
})

const waterText = computed(() => {
  if (!props.waterData) {
    return '0/2000ml'
  }
  const intake = props.waterData.intake || 0
  const target = props.waterData.target || 2000
  return `${intake}/${target}ml`
})

const exerciseText = computed(() => {
  if (!props.exerciseData) {
    return '0分钟'
  }
  const duration = props.exerciseData.duration || 0
  return `${duration}分钟`
})
</script>

<style scoped lang="scss">
.today-overview-card {
  margin-bottom: 16px;
  border-radius: 12px;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }

  .overview-content {
    .overview-item {
      display: flex;
      align-items: baseline; // 文本上下对齐到同一水平线
      margin-bottom: 12px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #666;
        margin-right: 8px;
        min-width: 60px; // 固定宽度，保证「待办」「饮水」对齐
        text-align: right;
      }

      .value {
        color: #333;
        font-weight: 500;
      }
    }
  }

  .skeleton-content {
    padding: 8px 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .today-overview-card {
    margin-bottom: 12px;

    .card-header .title {
      font-size: 14px;
    }

    .overview-content .overview-item {
      font-size: 13px;
      margin-bottom: 10px;
    }
  }
}
</style>

