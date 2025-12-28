<template>
  <el-dialog
    v-model="dialogVisible"
    title="运动记录"
    :width="isMobile ? '90%' : '500px'"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="运动类型" prop="exerciseType">
        <div class="exercise-type-buttons">
          <el-button
            v-for="type in exerciseTypes"
            :key="type"
            :type="form.exerciseType === type ? 'primary' : 'default'"
            @click="form.exerciseType = type"
          >
            {{ type }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="时长(分钟)" prop="durationMinutes">
        <el-input-number
          v-model="form.durationMinutes"
          :min="1"
          :max="1440"
          placeholder="请输入运动时长"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="消耗卡路里">
        <el-input
          :model-value="calculatedCalories"
          readonly
          placeholder="自动计算"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 新增运动记录弹窗组件
 * 
 * 关联需求: REQ-004
 * 关联组件: COMP-REQ-001-01-04
 * 关联接口: API-REQ-002-02
 * 关联测试用例: TC-COMP-REQ-001-01-04-01, TC-COMP-REQ-001-01-04-02
 */

import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { healthApi } from '@/services/healthApi'
import type { ExerciseRecordDTO, ExerciseType } from '@/types/health'
import { calculateCalories } from '@/utils/caloriesCalculator'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'close'): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
})

const isMobile = computed(() => window.innerWidth < 768)

const formRef = ref<FormInstance>()
const submitting = ref(false)

const exerciseTypes: ExerciseType[] = [
  '跑步', '游泳', '骑行', '力量训练', '瑜伽', '有氧运动', '球类运动', '其他'
]

const form = reactive<ExerciseRecordDTO>({
  exerciseType: '跑步',
  durationMinutes: 30,
  caloriesBurned: 0,
})

const calculatedCalories = computed(() => {
  if (!form.exerciseType || !form.durationMinutes) {
    return 0
  }
  const calories = calculateCalories(form.exerciseType, form.durationMinutes)
  form.caloriesBurned = calories
  return calories
})

const rules: FormRules = {
  exerciseType: [
    { required: true, message: '请选择运动类型', trigger: 'change' },
  ],
  durationMinutes: [
    { required: true, message: '请输入运动时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '运动时长必须大于0', trigger: 'blur' },
  ],
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
  formRef.value?.resetFields()
  Object.assign(form, {
    exerciseType: '跑步',
    durationMinutes: 30,
    caloriesBurned: 0,
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const dto: ExerciseRecordDTO = {
      exerciseType: form.exerciseType,
      durationMinutes: form.durationMinutes,
      caloriesBurned: calculatedCalories.value,
    }

    await healthApi.createExerciseRecord(dto)
    ElMessage.success('记录成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    form.durationMinutes = 30
    form.exerciseType = '跑步'
  }
})
</script>

<style scoped lang="scss">
.exercise-type-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  width: 100%;
}

@media (min-width: 769px) {
  .exercise-type-buttons {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>

