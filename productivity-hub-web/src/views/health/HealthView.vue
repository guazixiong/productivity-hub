<template>
  <div class="health-view">
    <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
      <el-tab-pane label="运动记录" name="exercise">
        <ExerciseView />
      </el-tab-pane>
      <el-tab-pane label="运动计划" name="training-plan">
        <TrainingPlanView />
      </el-tab-pane>
      <el-tab-pane label="饮水记录" name="water">
        <WaterView />
      </el-tab-pane>
      <el-tab-pane label="体重记录" name="weight">
        <WeightView />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * 健康管理页面组件
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDevice } from '@/composables/useDevice'
import ExerciseView from './ExerciseView.vue'
import TrainingPlanView from './TrainingPlanView.vue'
import WaterView from './WaterView.vue'
import WeightView from './WeightView.vue'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const router = useRouter()
const activeTab = ref('exercise')

const handleTabChange = (name: string) => {
  // Tab切换时的处理逻辑（如需要）
}

onMounted(() => {
  // 页面初始化逻辑
})
</script>

<style scoped>
.health-view {
  padding: 20px;
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .health-view {
    padding: 0;
  }

  .health-view :deep(.el-tabs) {
    border-radius: 0;
  }

  .health-view :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 12px;
    background: #ffffff;
    border-bottom: 1px solid #e4e7ed;
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .health-view :deep(.el-tabs__nav-wrap) {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .health-view :deep(.el-tabs__nav-scroll) {
    overflow-x: auto;
  }

  .health-view :deep(.el-tabs__item) {
    padding: 12px 16px;
    font-size: 13px;
    white-space: nowrap;
  }

  .health-view :deep(.el-tabs__content) {
    padding: 12px;
  }
}
</style>

