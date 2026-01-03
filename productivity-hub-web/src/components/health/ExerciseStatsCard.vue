<template>
  <el-card class="exercise-stats-card" shadow="hover" style="margin-bottom: 24px;">
    <template #header>
      <span class="card-title">运动数据</span>
    </template>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div style="height: 100px;"></div>
      </template>
      <template #default>
        <div v-if="!data" class="empty-state">
          <el-empty description="暂无运动数据" />
        </div>
        <div v-else class="stats-content">
          <div class="stats-text">
            <span class="label">本周运动</span>
            <span class="value">{{ data.totalCount }}次</span>
            <span class="separator">|</span>
            <span class="value">{{ data.totalDuration }}分钟</span>
          </div>
          <el-progress
            v-if="data.totalDuration > 0"
            :percentage="Math.min(100, (data.totalDuration / 180) * 100)"
            :stroke-width="8"
            color="#4A90E2"
          />
        </div>
      </template>
    </el-skeleton>
  </el-card>
</template>

<script setup lang="ts">
/**
 * 运动数据卡片组件
 */

import type { ExerciseStatistics } from '@/types/health'

interface Props {
  data: ExerciseStatistics | null
  loading?: boolean
}

withDefaults(defineProps<Props>(), {
  loading: false,
})
</script>

<style scoped lang="scss">
.exercise-stats-card {
  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .stats-content {
    .stats-text {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      font-size: 14px;

      .label {
        color: #666;
      }

      .value {
        color: #333;
        font-weight: 500;
      }

      .separator {
        color: #999;
      }
    }
  }

  .empty-state {
    padding: 40px 0;
  }
}
</style>

