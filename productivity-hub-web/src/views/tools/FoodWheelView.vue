<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Food, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const foodOptions = ref<string[]>(['火锅', '烧烤', '日料', '西餐', '中餐', '快餐', '小吃', '甜品'])
const spinning = ref(false)
const selectedFood = ref('')
const newFoodOption = ref('')

const addFoodOption = () => {
  const food = newFoodOption.value.trim()
  if (!food) {
    ElMessage.warning('请输入美食名称')
    return
  }
  if (foodOptions.value.includes(food)) {
    ElMessage.warning('该美食已存在')
    return
  }
  foodOptions.value.push(food)
  newFoodOption.value = ''
  ElMessage.success('添加成功')
}

const removeFoodOption = (index: number) => {
  foodOptions.value.splice(index, 1)
  ElMessage.success('删除成功')
}

const spinFoodWheel = () => {
  if (foodOptions.value.length === 0) {
    ElMessage.warning('请先添加美食选项')
    return
  }
  if (spinning.value) return
  
  spinning.value = true
  selectedFood.value = ''
  
  const duration = 2000
  const startTime = Date.now()
  const interval = setInterval(() => {
    const randomIndex = Math.floor(Math.random() * foodOptions.value.length)
    selectedFood.value = foodOptions.value[randomIndex]
    
    if (Date.now() - startTime >= duration) {
      clearInterval(interval)
      spinning.value = false
      const finalIndex = Math.floor(Math.random() * foodOptions.value.length)
      selectedFood.value = foodOptions.value[finalIndex]
      ElMessage.success(`今天吃：${selectedFood.value}`)
    }
  }, 50)
}
</script>

<template>
  <div class="food-wheel-container">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="router.push('/tools')">返回工具箱</el-button>
    </div>
    <div class="food-wheel-content">
      <div class="food-wheel-section">
        <h3>美食选项</h3>
        <div class="food-options">
          <el-tag
            v-for="(food, index) in foodOptions"
            :key="index"
            closable
            @close="removeFoodOption(index)"
            class="food-tag"
          >
            {{ food }}
          </el-tag>
        </div>
        <div class="food-input-group">
          <el-input
            v-model="newFoodOption"
            placeholder="输入美食名称"
            @keyup.enter="addFoodOption"
          />
          <el-button type="primary" @click="addFoodOption">添加</el-button>
        </div>
      </div>
      <div class="food-wheel-section">
        <h3>转盘抽选</h3>
        <div class="wheel-container">
          <div class="wheel-result" :class="{ spinning: spinning }">
            <div v-if="selectedFood" class="selected-food">{{ selectedFood }}</div>
            <div v-else class="wheel-placeholder">点击开始抽选</div>
          </div>
          <el-button
            type="primary"
            size="large"
            :loading="spinning"
            @click="spinFoodWheel"
            class="spin-button"
          >
            {{ spinning ? '抽选中...' : '开始抽选' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 8px;
}

.food-wheel-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.food-wheel-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.food-wheel-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.food-wheel-section h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
  font-weight: 600;
}

.food-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.food-tag {
  font-size: 14px;
  padding: 8px 12px;
}

.food-input-group {
  display: flex;
  gap: 8px;
}

.wheel-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.wheel-result {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  border: 4px solid var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--brand-gradient);
  color: white;
  font-size: 24px;
  font-weight: 700;
  transition: transform 0.1s ease;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
}

.wheel-result.spinning {
  animation: spin 0.1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.selected-food {
  text-align: center;
}

.wheel-placeholder {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.spin-button {
  min-width: 120px;
}

@media (max-width: 768px) {
  .food-wheel-content {
    grid-template-columns: 1fr;
  }
}
</style>

