<template>
  <el-dialog
    :model-value="modelValue"
    :title="plan ? '编辑训练计划' : '新增训练计划'"
    width="600px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="计划名称" prop="planName">
        <el-input
          v-model="form.planName"
          placeholder="请输入计划名称"
          maxlength="128"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="计划类型" prop="planType">
        <el-select v-model="form.planType" placeholder="请选择计划类型" style="width: 100%">
          <el-option
            v-for="type in planTypes"
            :key="type"
            :label="type"
            :value="type"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="form.startDate"
          type="date"
          placeholder="选择开始日期（任意输入两个字段可自动计算第三个）"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          placeholder="选择结束日期（任意输入两个字段可自动计算第三个）"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="目标持续天数" prop="targetDurationDays">
        <el-input-number
          v-model="form.targetDurationDays"
          :min="1"
          placeholder="天数（任意输入两个字段可自动计算第三个）"
          style="width: 100%"
          :controls="true"
        />
        <div class="form-item-tip">提示：开始日期、结束日期、目标天数三者联动，任意输入两个可自动计算第三个</div>
      </el-form-item>
      
      <el-form-item label="每日目标卡路里" prop="targetCaloriesPerDay">
        <el-input-number
          v-model="form.targetCaloriesPerDay"
          :min="0"
          placeholder="可选（单位：千卡）"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="计划描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入计划描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import type { TrainingPlan, TrainingPlanDTO, PlanType } from '@/types/health'

const props = defineProps<{
  modelValue: boolean
  plan?: Partial<TrainingPlan>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [data: TrainingPlanDTO]
}>()

const formRef = ref<FormInstance>()

const planTypes: PlanType[] = ['减脂', '增肌', '塑形', '耐力提升', '康复训练', '其他']

const form = reactive<TrainingPlanDTO>({
  planName: '',
  planType: '其他',
  targetDurationDays: undefined,
  targetCaloriesPerDay: undefined,
  description: '',
  startDate: undefined,
  endDate: undefined,
})

// 用于防止循环触发的标志
const isCalculating = ref(false)

// 计算日期差（天数）
const calculateDays = (start: string, end: string): number => {
  const startDate = new Date(start)
  const endDate = new Date(end)
  const diffTime = endDate.getTime() - startDate.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1 // +1 包含开始和结束日期
  return diffDays > 0 ? diffDays : 0
}

// 根据开始日期和目标天数计算结束日期
const calculateEndDate = (start: string, days: number): string => {
  const startDate = new Date(start)
  startDate.setDate(startDate.getDate() + days - 1) // -1 因为包含开始日期
  const year = startDate.getFullYear()
  const month = String(startDate.getMonth() + 1).padStart(2, '0')
  const day = String(startDate.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 根据结束日期和目标天数计算开始日期
const calculateStartDate = (end: string, days: number): string => {
  const endDate = new Date(end)
  endDate.setDate(endDate.getDate() - days + 1) // +1 因为包含结束日期
  const year = endDate.getFullYear()
  const month = String(endDate.getMonth() + 1).padStart(2, '0')
  const day = String(endDate.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 联动计算逻辑
const handleDateCalculation = () => {
  if (isCalculating.value) return
  
  const hasStartDate = !!form.startDate
  const hasEndDate = !!form.endDate
  const hasTargetDays = form.targetDurationDays !== undefined && form.targetDurationDays !== null && form.targetDurationDays > 0
  
  // 统计已填写的字段数量
  const filledCount = [hasStartDate, hasEndDate, hasTargetDays].filter(Boolean).length
  
  // 只有填写了两个字段时才进行计算
  if (filledCount !== 2) return
  
  // 验证日期有效性
  if (hasStartDate && hasEndDate) {
    const start = new Date(form.startDate!)
    const end = new Date(form.endDate!)
    if (end < start) {
      // 结束日期早于开始日期，不进行计算
      return
    }
  }
  
  isCalculating.value = true
  
  try {
    if (hasStartDate && hasEndDate && !hasTargetDays) {
      // 有开始日期和结束日期，计算目标天数
      const days = calculateDays(form.startDate!, form.endDate!)
      if (days > 0) {
        form.targetDurationDays = days
      }
    } else if (hasStartDate && hasTargetDays && !hasEndDate) {
      // 有开始日期和目标天数，计算结束日期
      form.endDate = calculateEndDate(form.startDate!, form.targetDurationDays!)
    } else if (hasEndDate && hasTargetDays && !hasStartDate) {
      // 有结束日期和目标天数，计算开始日期
      form.startDate = calculateStartDate(form.endDate!, form.targetDurationDays!)
    }
  } catch (error) {
    console.error('日期计算错误:', error)
  } finally {
    // 使用 setTimeout 确保 DOM 更新完成后再重置标志
    setTimeout(() => {
      isCalculating.value = false
    }, 0)
  }
}

// 监听开始日期变化
watch(
  () => form.startDate,
  () => {
    if (!isCalculating.value) {
      handleDateCalculation()
    }
  }
)

// 监听结束日期变化
watch(
  () => form.endDate,
  () => {
    if (!isCalculating.value) {
      handleDateCalculation()
    }
  }
)

// 监听目标天数变化
watch(
  () => form.targetDurationDays,
  () => {
    if (!isCalculating.value) {
      handleDateCalculation()
    }
  }
)

const validateEndDate = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  if (form.startDate && value < form.startDate) {
    callback(new Error('结束日期不能早于开始日期'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { max: 128, message: '计划名称不能超过128个字符', trigger: 'blur' },
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' },
  ],
  endDate: [
    { validator: validateEndDate, trigger: 'change' },
  ],
}

watch(
  () => props.plan,
  (newPlan) => {
    if (newPlan) {
      form.planName = newPlan.planName || ''
      form.planType = newPlan.planType || '其他'
      form.targetDurationDays = newPlan.targetDurationDays
      form.targetCaloriesPerDay = newPlan.targetCaloriesPerDay
      form.description = newPlan.description || ''
      form.startDate = newPlan.startDate
      form.endDate = newPlan.endDate
    } else {
      // 重置表单
      form.planName = ''
      form.planType = '其他'
      form.targetDurationDays = undefined
      form.targetCaloriesPerDay = undefined
      form.description = ''
      form.startDate = undefined
      form.endDate = undefined
    }
    formRef.value?.clearValidate()
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      formRef.value?.clearValidate()
    }
  }
)

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    emit('submit', { ...form })
  } catch (error) {
    ElMessage.warning('请完善表单信息')
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}
</style>

