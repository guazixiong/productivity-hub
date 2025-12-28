<template>
  <el-card class="water-progress-card">
    <div class="progress-content">
      <div class="progress-circle">
        <el-progress
          type="dashboard"
          :percentage="Math.min(progress.progressPercent ?? 0, 100)"
          :color="progressColor"
          :width="120"
        >
          <template #default>
            <div class="progress-text">
              <div class="progress-value">{{ (progress.progressPercent ?? 0).toFixed(1) }}%</div>
              <div class="progress-label">完成度</div>
            </div>
          </template>
        </el-progress>
      </div>
      <div class="progress-info">
        <div class="info-item">
          <span class="label">已饮水量：</span>
          <span class="value">{{ progress.totalIntakeMl ?? 0 }}ml</span>
        </div>
        <div class="info-item">
          <span class="label">目标饮水量：</span>
          <span class="value">{{ progress.dailyTargetMl ?? 0 }}ml</span>
        </div>
        <div class="info-item">
          <span class="label">剩余：</span>
          <span class="value" :class="{ 'warning': (progress.remainingMl ?? 0) > 0 }">
            {{ progress.remainingMl ?? 0 }}ml
          </span>
        </div>
        <div class="info-item">
          <span class="label">今日次数：</span>
          <span class="value">{{ progress.intakeCount ?? 0 }}次</span>
        </div>
        <el-tag v-if="progress.isAchieved" type="success" size="large" style="margin-top: 12px;">
          今日目标已完成！
        </el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { WaterProgress } from '@/types/health'

interface Props {
  progress: WaterProgress
}

const props = defineProps<Props>()

const progressColor = computed(() => {
  const percent = props.progress.progressPercent ?? 0
  if (percent >= 100) return '#67C23A'
  if (percent >= 75) return '#409EFF'
  if (percent >= 50) return '#E6A23C'
  return '#F56C6C'
})
</script>

<style scoped>
.water-progress-card {
  margin-bottom: 20px;
}
.progress-content {
  display: flex;
  align-items: center;
  gap: 40px;
}
.progress-circle {
  flex-shrink: 0;
}
.progress-text {
  text-align: center;
}
.progress-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.progress-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.progress-info {
  flex: 1;
}
.info-item {
  margin-bottom: 12px;
  font-size: 14px;
}
.label {
  color: #909399;
}
.value {
  color: #303133;
  font-weight: 500;
  margin-left: 8px;
}
.value.warning {
  color: #E6A23C;
}
</style>

