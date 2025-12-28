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
          placeholder="选择开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          placeholder="选择结束日期（可选）"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="目标持续天数" prop="targetDurationDays">
        <el-input-number
          v-model="form.targetDurationDays"
          :min="1"
          placeholder="可选"
          style="width: 100%"
        />
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

const rules: FormRules = {
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { max: 128, message: '计划名称不能超过128个字符', trigger: 'blur' },
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' },
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
</style>

