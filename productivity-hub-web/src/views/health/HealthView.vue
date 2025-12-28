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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ExerciseView from './ExerciseView.vue'
import TrainingPlanView from './TrainingPlanView.vue'
import WaterView from './WaterView.vue'
import WeightView from './WeightView.vue'

const router = useRouter()
const activeTab = ref('exercise')

const handleTabChange = (name: string) => {
  // Tab切换时的处理逻辑（如需要）
  console.log('切换到:', name)
}

onMounted(() => {
  // 页面初始化逻辑
})
</script>

<style scoped>
.health-view {
  padding: 20px;
}
</style>

