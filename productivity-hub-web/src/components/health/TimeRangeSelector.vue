<template>
  <el-select
    :model-value="value"
    @update:model-value="handleChange"
    style="width: 150px"
  >
    <el-option label="今日" value="today" />
    <el-option label="本周" value="week" />
    <el-option label="本月" value="month" />
    <el-option label="自定义" value="custom" />
  </el-select>

  <el-dialog
    v-model="customDialogVisible"
    title="选择日期范围"
    width="400px"
  >
    <el-date-picker
      v-model="customRange"
      type="daterange"
      range-separator="至"
      start-placeholder="开始日期"
      end-placeholder="结束日期"
      format="YYYY-MM-DD"
      value-format="YYYY-MM-DD"
      style="width: 100%"
    />
    <template #footer>
      <el-button @click="customDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleCustomConfirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 时间筛选器组件
 * 
 * 关联需求: REQ-008
 * 关联组件: COMP-REQ-002-01-01
 */

import { ref } from 'vue'

interface Props {
  value: 'today' | 'week' | 'month' | 'custom'
}

interface Emits {
  (e: 'change', value: string, startDate?: string, endDate?: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const customDialogVisible = ref(false)
const customRange = ref<[string, string] | null>(null)

const handleChange = (value: string) => {
  if (value === 'custom') {
    customDialogVisible.value = true
    // 重置选择,等待用户确认自定义日期范围
    emit('change', props.value)
  } else {
    emit('change', value)
  }
}

const handleCustomConfirm = () => {
  if (customRange.value && customRange.value.length === 2) {
    emit('change', 'custom', customRange.value[0], customRange.value[1])
    customDialogVisible.value = false
  }
}
</script>

<style scoped lang="scss">
// 样式已在Element Plus组件中处理
</style>

