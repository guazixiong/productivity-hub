<template>
  <div class="quick-record-view">
    <div class="section-title">⚡ 快捷记录</div>

    <QuickRecordButtons
      @todo-click="handleTodoClick"
      @exercise-click="handleExerciseClick"
      @water-click="handleWaterClick"
      @weight-click="handleWeightClick"
    />

    <!-- 弹窗组件 -->
    <TodoRecordDialog
      v-model:visible="dialogs.todo"
      @success="handleRecordSuccess"
    />
    <ExerciseRecordDialog
      v-model:visible="dialogs.exercise"
      @success="handleRecordSuccess"
    />
    <WaterRecordDialog
      v-model:visible="dialogs.water"
      @success="handleRecordSuccess"
    />
    <WeightRecordDialog
      v-model:visible="dialogs.weight"
      @success="handleRecordSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 首页-快捷记录页面（融合快捷统计）
 */

import { reactive } from 'vue'
import QuickRecordButtons from '@/components/quick-record/QuickRecordButtons.vue'
import TodoRecordDialog from '@/components/quick-record/TodoRecordDialog.vue'
import ExerciseRecordDialog from '@/components/quick-record/ExerciseRecordDialog.vue'
import WaterRecordDialog from '@/components/quick-record/WaterRecordDialog.vue'
import WeightRecordDialog from '@/components/quick-record/WeightRecordDialog.vue'


const dialogs = reactive({
  todo: false,
  exercise: false,
  water: false,
  weight: false,
})

const handleRecordSuccess = () => {
  // 记录成功后可以在这里添加刷新逻辑
}

const handleTodoClick = () => {
  dialogs.todo = true
}

const handleExerciseClick = () => {
  dialogs.exercise = true
}

const handleWaterClick = () => {
  dialogs.water = true
}

const handleWeightClick = () => {
  dialogs.weight = true
}

</script>

<style scoped lang="scss">
.quick-record-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
  min-height: calc(100vh - 60px);

  .section-title {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 32px 0 20px;
    display: flex;
    align-items: center;
    gap: 8px;
    letter-spacing: 0.5px;
    
    &::before {
      content: '';
      width: 4px;
      height: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 2px;
    }
  }

}

// 美化快捷记录按钮
:deep(.quick-record-buttons) {
  .buttons-grid {
    gap: 20px;
    padding: 20px 0;
  }

  .record-button {
    border-radius: 16px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: left 0.5s;
    }

    &:hover {
      transform: translateY(-6px) scale(1.02);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);

      &::before {
        left: 100%;
      }
    }

    &:active {
      transform: translateY(-2px) scale(0.98);
    }

    .button-content {
      position: relative;
      z-index: 1;

      .button-icon {
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
        transition: transform 0.3s ease;
      }

      .button-text {
        font-weight: 600;
        letter-spacing: 0.5px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
      }
    }

    &:hover .button-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
}

@media (max-width: 768px) {
  .quick-record-view {
    padding: 16px;
    background: #ffffff;

    .section-title {
      font-size: 18px;
      margin: 24px 0 16px;
    }
  }

  :deep(.quick-record-buttons) {
    .buttons-grid {
      gap: 16px;
      padding: 16px 0;
    }

    .record-button {
      border-radius: 12px;
      min-height: 140px;

      .button-content {
        .button-icon {
          font-size: 36px;
        }

        .button-text {
          font-size: 14px;
        }
      }
    }
  }
}

// 大屏幕优化
@media (min-width: 1400px) {
  .quick-record-view {
    padding: 32px;
  }
}
</style>

