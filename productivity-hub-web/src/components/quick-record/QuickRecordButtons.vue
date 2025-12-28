<template>
  <div class="quick-record-buttons">
    <div class="buttons-grid">
      <el-button
        class="record-button"
        type="primary"
        size="large"
        @click="handleTodoClick"
      >
        <div class="button-content">
          <span class="button-icon">📝</span>
          <span class="button-text">待办</span>
        </div>
      </el-button>

      <el-button
        class="record-button"
        type="success"
        size="large"
        @click="handleExerciseClick"
      >
        <div class="button-content">
          <span class="button-icon">🏃</span>
          <span class="button-text">运动</span>
        </div>
      </el-button>

      <el-button
        class="record-button"
        type="info"
        size="large"
        @click="handleWaterClick"
      >
        <div class="button-content">
          <span class="button-icon">💧</span>
          <span class="button-text">饮水</span>
        </div>
      </el-button>

      <el-button
        class="record-button"
        type="warning"
        size="large"
        @click="handleWeightClick"
      >
        <div class="button-content">
          <span class="button-icon">⚖️</span>
          <span class="button-text">体重</span>
        </div>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 快捷记录按钮组组件
 * 
 * 关联需求: REQ-002
 * 关联组件: COMP-REQ-001-01-02
 * 关联测试用例: TC-COMP-REQ-001-01-02-01 ~ TC-COMP-REQ-001-01-02-03
 */

interface Emits {
  (e: 'todo-click'): void
  (e: 'exercise-click'): void
  (e: 'water-click'): void
  (e: 'weight-click'): void
}

const emit = defineEmits<Emits>()

const handleTodoClick = () => {
  emit('todo-click')
}

const handleExerciseClick = () => {
  emit('exercise-click')
}

const handleWaterClick = () => {
  emit('water-click')
}

const handleWeightClick = () => {
  emit('weight-click')
}
</script>

<style scoped lang="scss">
.quick-record-buttons {
  .buttons-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 16px 0;
  }

  .record-button {
    width: 100%;
    min-height: 120px;
    padding: 16px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;

    .button-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .button-icon {
        font-size: 48px;
        line-height: 1;
      }

      .button-text {
        font-size: 16px;
        font-weight: 500;
      }
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .quick-record-buttons {
    .buttons-grid {
      gap: 12px;
      padding: 12px 0;
    }

    .record-button {
      min-height: 150px;

      .button-content {
        .button-icon {
          font-size: 40px;
        }

        .button-text {
          font-size: 14px;
        }
      }
    }
  }
}

@media (min-width: 769px) {
  .quick-record-buttons {
    .buttons-grid {
      max-width: 600px;
      margin: 0 auto;
    }

    .record-button {
      min-height: 120px;
    }
  }
}
</style>

