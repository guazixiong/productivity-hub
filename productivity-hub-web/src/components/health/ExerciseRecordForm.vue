<template>
  <el-dialog
    :model-value="modelValue"
    :title="record ? '编辑运动记录' : '新增运动记录'"
    width="600px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="运动类型" prop="exerciseType">
        <el-select v-model="form.exerciseType" placeholder="请选择运动类型" style="width: 100%">
          <el-option
            v-for="type in exerciseTypes"
            :key="type"
            :label="type"
            :value="type"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="运动日期" prop="exerciseDate">
        <el-date-picker
          v-model="form.exerciseDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="运动时长(分钟)" prop="durationMinutes">
        <el-input-number
          v-model="form.durationMinutes"
          :min="1"
          :max="1440"
          placeholder="请输入运动时长"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="消耗卡路里" prop="caloriesBurned">
        <el-input-number
          v-model="form.caloriesBurned"
          :min="0"
          placeholder="可选"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="运动距离(公里)" prop="distanceKm">
        <el-input-number
          v-model="form.distanceKm"
          :min="0"
          :precision="2"
          placeholder="可选"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="平均心率" prop="heartRateAvg">
            <el-input-number
              v-model="form.heartRateAvg"
              :min="30"
              :max="250"
              placeholder="可选"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大心率" prop="heartRateMax">
            <el-input-number
              v-model="form.heartRateMax"
              :min="30"
              :max="250"
              placeholder="可选"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
      
      <el-form-item label="训练计划" prop="trainingPlanId">
        <el-select 
          v-model="form.trainingPlanId" 
          placeholder="可选，选择训练计划" 
          clearable 
          style="width: 100%"
          :loading="loadingPlans"
        >
          <el-option
            v-for="plan in localTrainingPlans"
            :key="plan.id"
            :label="plan.planName"
            :value="plan.id"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="动作参考链接">
        <div class="action-url-list">
          <el-tag
            v-for="(url, index) in form.exerciseActionRefUrl"
            :key="index"
            closable
            @close="removeActionUrl(index)"
            style="margin-bottom: 8px; margin-right: 8px;"
          >
            <a :href="url" target="_blank" style="color: inherit;">{{ url }}</a>
          </el-tag>
          <el-input
            v-model="actionUrlInput"
            placeholder="输入链接后按回车添加"
            @keyup.enter="addActionUrl"
          />
        </div>
      </el-form-item>
      
      <el-form-item label="备注" prop="notes">
        <el-input
          v-model="form.notes"
          type="textarea"
          :rows="3"
          placeholder="可选"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { ExerciseRecordDTO, TrainingPlan } from '@/types/health'
import type { FormInstance, FormRules } from 'element-plus'
import { healthApi } from '@/services/healthApi'

interface Props {
  modelValue: boolean
  record?: Partial<ExerciseRecordDTO>
  trainingPlans?: TrainingPlan[] // 保留此prop以保持向后兼容，但不再使用
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: ExerciseRecordDTO): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const actionUrlInput = ref('')
const localTrainingPlans = ref<TrainingPlan[]>([])
const loadingPlans = ref(false)

const form = reactive<ExerciseRecordDTO>({
  exerciseType: props.record?.exerciseType || '',
  exerciseDate: props.record?.exerciseDate || new Date().toISOString().split('T')[0],
  durationMinutes: props.record?.durationMinutes || 0,
  caloriesBurned: props.record?.caloriesBurned,
  distanceKm: props.record?.distanceKm,
  heartRateAvg: props.record?.heartRateAvg,
  heartRateMax: props.record?.heartRateMax,
  trainingPlanId: props.record?.trainingPlanId,
  exerciseActionRefUrl: props.record?.exerciseActionRefUrl || [],
  notes: props.record?.notes || '',
})

// 实时获取训练计划
const loadTrainingPlans = async () => {
  loadingPlans.value = true
  try {
    // 只获取进行中的训练计划
    const data = await healthApi.listTrainingPlans({ status: 'ACTIVE' })
    localTrainingPlans.value = data || []
  } catch (error: any) {
    console.error('加载训练计划失败:', error)
    ElMessage.warning('加载训练计划失败，请稍后重试')
  } finally {
    loadingPlans.value = false
  }
}

// 监听对话框打开，实时获取训练计划
watch(() => props.modelValue, (visible) => {
  if (visible) {
    loadTrainingPlans()
  }
})

// 监听record变化，更新表单
watch(() => props.record, (newRecord) => {
  if (newRecord) {
    Object.assign(form, {
      exerciseType: newRecord.exerciseType || '',
      exerciseDate: newRecord.exerciseDate || new Date().toISOString().split('T')[0],
      durationMinutes: newRecord.durationMinutes || 0,
      caloriesBurned: newRecord.caloriesBurned,
      distanceKm: newRecord.distanceKm,
      heartRateAvg: newRecord.heartRateAvg,
      heartRateMax: newRecord.heartRateMax,
      trainingPlanId: newRecord.trainingPlanId,
      exerciseActionRefUrl: newRecord.exerciseActionRefUrl || [],
      notes: newRecord.notes || '',
    })
  } else {
    // 重置表单
    Object.assign(form, {
      exerciseType: '',
      exerciseDate: new Date().toISOString().split('T')[0],
      durationMinutes: 0,
      caloriesBurned: undefined,
      distanceKm: undefined,
      heartRateAvg: undefined,
      heartRateMax: undefined,
      trainingPlanId: undefined,
      exerciseActionRefUrl: [],
      notes: '',
    })
  }
}, { deep: true })

const exerciseTypes = [
  '跑步', '游泳', '骑行', '力量训练', '瑜伽', '有氧运动', '球类运动', '其他'
]

const addActionUrl = () => {
  if (actionUrlInput.value && !form.exerciseActionRefUrl?.includes(actionUrlInput.value)) {
    const urlPattern = /^https?:\/\/.+/
    if (urlPattern.test(actionUrlInput.value)) {
      form.exerciseActionRefUrl?.push(actionUrlInput.value)
      actionUrlInput.value = ''
    }
  }
}

const removeActionUrl = (index: number) => {
  form.exerciseActionRefUrl?.splice(index, 1)
}

const rules: FormRules = {
  exerciseType: [
    { required: true, message: '请选择运动类型', trigger: 'change' }
  ],
  exerciseDate: [
    { required: true, message: '请选择运动日期', trigger: 'change' }
  ],
  durationMinutes: [
    { required: true, message: '请输入运动时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 1440, message: '运动时长必须在1-1440分钟之间', trigger: 'blur' }
  ],
  caloriesBurned: [
    { type: 'number', min: 0, message: '卡路里不能为负数', trigger: 'blur' }
  ],
  distanceKm: [
    { type: 'number', min: 0, message: '距离不能为负数', trigger: 'blur' }
  ],
  heartRateAvg: [
    { type: 'number', min: 30, max: 250, message: '心率必须在30-250次/分钟之间', trigger: 'blur' }
  ],
  heartRateMax: [
    { type: 'number', min: 30, max: 250, message: '心率必须在30-250次/分钟之间', trigger: 'blur' }
  ],
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('submit', { ...form })
      emit('update:modelValue', false)
    }
  })
}

const handleCancel = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.action-url-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>

