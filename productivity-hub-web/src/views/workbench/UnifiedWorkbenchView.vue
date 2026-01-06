<template>
  <div class="unified-workbench-view">
    <!-- 移动端风格的标签页 -->
    <el-tabs
      v-model="activeTab"
      class="mobile-tabs"
      @tab-change="handleTabChange"
    >
      <!-- 快捷记录标签页 -->
      <el-tab-pane label="快捷记录" name="quick-record">
        <div class="tab-content">
          <div class="section-title">⚡ 快捷记录</div>

          <QuickRecordButtons
            @todo-click="handleTodoClick"
            @exercise-click="handleExerciseClick"
            @water-click="handleWaterClick"
            @weight-click="handleWeightClick"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

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
 * 统一工作台页面（合并快捷记录与统计和工作台）
 * 使用标签页区分两个功能模块
 */

import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import QuickRecordButtons from '@/components/quick-record/QuickRecordButtons.vue'
import TodoRecordDialog from '@/components/quick-record/TodoRecordDialog.vue'
import ExerciseRecordDialog from '@/components/quick-record/ExerciseRecordDialog.vue'
import WaterRecordDialog from '@/components/quick-record/WaterRecordDialog.vue'
import WeightRecordDialog from '@/components/quick-record/WeightRecordDialog.vue'

const route = useRoute()
const router = useRouter()

// 标签页管理
const activeTab = ref<'quick-record'>('quick-record')

// 根据路由参数设置活动标签
watch(
  () => route.path,
  (path) => {
    if (path === '/quick-record') {
      activeTab.value = 'quick-record'
    }
  },
  { immediate: true }
)

const handleTabChange = (tabName: string) => {
  const routeMap: Record<string, string> = {
    'quick-record': '/quick-record',
  }

  const targetPath = routeMap[tabName]
  if (targetPath && route.path !== targetPath) {
    router.push(targetPath)
  }
}

// 快捷记录相关状态

const dialogs = reactive({
  todo: false,
  exercise: false,
  water: false,
  weight: false,
})

// 快捷记录相关方法
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


onMounted(() => {
  // 页面加载时的初始化逻辑
})
</script>

<style scoped lang="scss">
.unified-workbench-view {
  min-height: calc(100vh - 60px);
  background: #f5f7fa;

  /* 移动端风格的标签页（桌面端在顶部） */
  :deep(.mobile-tabs) {
    .el-tabs__header {
      display: none; /* 只有一个标签页时隐藏头部 */
    }

    .el-tabs__nav-wrap {
      &::after {
        display: none;
      }
    }

    .el-tabs__nav {
      width: 100%;
      display: flex;
      justify-content: space-around;
      background: #ffffff;
    }

    .el-tabs__item {
      flex: 1;
      text-align: center;
      padding: 16px 8px;
      font-size: 15px;
      font-weight: 500;
      color: #606266;
      border: none;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      cursor: pointer;
      -webkit-tap-highlight-color: transparent;
      min-width: 0; /* 允许标签页缩小以适应4个标签 */

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 0;
        height: 3px;
        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
        border-radius: 2px 2px 0 0;
        transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      &.is-active {
        color: #667eea;
        font-weight: 600;
        background: rgba(102, 126, 234, 0.05);

        &::after {
          width: 60%;
        }
      }

      &:hover {
        color: #667eea;
        background: rgba(102, 126, 234, 0.03);
      }

      &:active {
        background: rgba(102, 126, 234, 0.1);
      }
    }

    .el-tabs__active-bar {
      display: none;
    }

    .el-tabs__content {
      padding: 0;
    }

    .el-tab-pane {
      padding: 0;
    }
  }

  .tab-content {
    padding: 16px;
    max-width: 100%;
    margin: 0 auto;
    background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
    min-height: calc(100vh - 120px);

    .section-title {
      font-size: 18px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 24px 0 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      letter-spacing: 0.5px;
      
      &::before {
        content: '';
        width: 4px;
        height: 18px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 2px;
      }
    }

    .section {
      margin-bottom: 32px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 16px;
      }
    }

    .add-task-button {
      margin-top: 16px;
      width: 100%;
      height: 48px;
      font-size: 16px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    }

  }
}


// 美化快捷记录按钮
:deep(.quick-record-buttons) {
  .buttons-grid {
    gap: 16px;
    padding: 16px 0;
  }

  .record-button {
    border-radius: 16px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    position: relative;
    overflow: hidden;
    min-height: 120px;

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
        font-size: 32px;
      }

      .button-text {
        font-weight: 600;
        letter-spacing: 0.5px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
        font-size: 14px;
      }
    }

    &:hover .button-icon {
      transform: scale(1.1) rotate(5deg);
    }
  }
}

/* 移动端优化 - 手机风格 */
@media (max-width: 768px) {
  .unified-workbench-view {
    background: #f5f7fa;

    :deep(.mobile-tabs) {
      .el-tabs__header {
        display: none; /* 只有一个标签页时隐藏头部 */
      }

      .el-tabs__nav {
        background: transparent;
      }

      .el-tabs__item {
        padding: 10px 4px;
        font-size: 12px;
        min-height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        gap: 4px;
        min-width: 0; /* 允许标签页缩小以适应4个标签 */

        &::after {
          top: 0;
          bottom: auto;
          height: 3px;
          border-radius: 0 0 2px 2px;
        }

        &.is-active {
          color: #667eea;
          background: rgba(102, 126, 234, 0.08);

          &::after {
            width: 40px;
            height: 3px;
          }
        }
      }
    }

    .tab-content {
      padding: 12px 16px;

      .section-title {
        font-size: 16px;
        margin: 20px 0 12px;
      }

      .section {
        margin-bottom: 24px;

        .section-title {
          font-size: 16px;
          margin-bottom: 12px;
        }
      }

      .add-task-button {
        height: 48px;
        font-size: 16px;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        font-weight: 600;
        -webkit-tap-highlight-color: transparent;
      }

    }

    :deep(.quick-record-buttons) {
      .buttons-grid {
        gap: 12px;
        padding: 12px 0;
        grid-template-columns: repeat(2, 1fr);
      }

      .record-button {
        border-radius: 16px;
        min-height: 120px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        -webkit-tap-highlight-color: transparent;

        &:active {
          transform: scale(0.95);
        }

        .button-content {
          .button-icon {
            font-size: 36px;
          }

          .button-text {
            font-size: 14px;
            font-weight: 600;
          }
        }
      }
    }

    /* 工作台卡片样式优化 */
    :deep(.el-card) {
      border-radius: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      border: none;
      margin-bottom: 16px;
    }

    /* 待办列表移动端优化 */
    :deep(.todo-list) {
      .todo-item {
        border-radius: 12px;
        margin-bottom: 12px;
        padding: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        -webkit-tap-highlight-color: transparent;
      }
    }

    /* 工具网格移动端优化 */
    :deep(.tools-grid) {
      .tool-item {
        border-radius: 16px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        -webkit-tap-highlight-color: transparent;

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }
}

// 大屏幕优化
@media (min-width: 1200px) {
  .unified-workbench-view {
    .tab-content {
      padding: 24px;
      max-width: 1400px;
    }
  }
}
</style>

