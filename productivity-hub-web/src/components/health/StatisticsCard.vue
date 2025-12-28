<template>
  <el-card class="statistics-card" :style="{ borderTopColor: color }">
    <div class="card-content">
      <div class="card-header">
        <span class="title">{{ title }}</span>
        <div class="header-right">
          <el-icon v-if="icon" :size="20" :color="color">
            <component :is="icon" />
          </el-icon>
        </div>
      </div>
      <div class="card-value">
        <span class="value">{{ value }}</span>
        <span v-if="unit" class="unit">{{ unit }}</span>
        <div v-if="$slots.extra" class="extra-content">
          <slot name="extra"></slot>
        </div>
      </div>
      <div v-if="trend" class="card-trend">
        <el-icon :color="trend.isUp ? '#67C23A' : '#F56C6C'">
          <component :is="trend.isUp ? 'ArrowUp' : 'ArrowDown'" />
        </el-icon>
        <span>{{ Math.abs(trend.value) }}%</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
interface Props {
  title: string
  value: number | string
  unit?: string
  icon?: string
  color?: string
  trend?: {
    value: number
    isUp: boolean
  }
}

const props = withDefaults(defineProps<Props>(), {
  unit: '',
  color: '#409EFF',
})
</script>

<style scoped>
.statistics-card {
  border-top: 3px solid;
}
.card-content {
  padding: 10px 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-value {
  display: flex;
  align-items: baseline;
  gap: 12px;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.extra-content {
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: baseline;
}
.extra-content :deep(.el-tag) {
  font-size: 28px;
  font-weight: bold;
  height: auto;
  padding: 4px 12px;
  line-height: 1.2;
}
.unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}
.card-trend {
  display: flex;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  gap: 4px;
}
</style>

