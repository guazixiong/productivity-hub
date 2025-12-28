<template>
  <el-dialog
    v-model="dialogVisible"
    title="体重记录"
    :width="isMobile ? '90%' : '500px'"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="体重(kg)" prop="weightKg">
        <el-input-number
          v-model="form.weightKg"
          :min="20"
          :max="300"
          :precision="2"
          placeholder="请输入体重"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="BMI">
        <el-input
          :model-value="bmiText"
          readonly
          placeholder="自动计算"
        />
        <div v-if="bmiStatus" class="bmi-status">
          <el-tag :type="getBMIStatusTagType(bmiStatus)">
            {{ bmiStatus }}
          </el-tag>
        </div>
        <div v-if="!bodyInfo" class="bmi-hint">
          <el-text type="warning" size="small">请先设置身体信息</el-text>
        </div>
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
 * 新增体重记录弹窗组件
 * 
 * 关联需求: REQ-006
 * 关联组件: COMP-REQ-001-01-06
 * 关联接口: API-REQ-002-04, API-REQ-002-05, API-REQ-002-06
 * 关联测试用例: TC-COMP-REQ-001-01-06-01 ~ TC-COMP-REQ-001-01-06-03
 */

import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { healthApi } from '@/services/healthApi'
import type { WeightRecordDTO, UserBodyInfo } from '@/types/health'
import { calculateBMI, getBMIStatus, type BMIStatus } from '@/utils/bmiCalculator'

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
const loading = ref(false)
const bodyInfo = ref<UserBodyInfo | null>(null)
const latestWeight = ref<number | null>(null)

const form = reactive<WeightRecordDTO>({
  weightKg: 0,
})

const bmi = computed(() => {
  if (!form.weightKg || !bodyInfo.value?.heightCm) {
    return null
  }
  return calculateBMI(form.weightKg, bodyInfo.value.heightCm)
})

const bmiText = computed(() => {
  if (bmi.value === null) {
    return '-'
  }
  return bmi.value.toFixed(2)
})

const bmiStatus = computed<BMIStatus | null>(() => {
  if (bmi.value === null) {
    return null
  }
  return getBMIStatus(bmi.value)
})

const getBMIStatusTagType = (status: BMIStatus) => {
  const map: Record<BMIStatus, string> = {
    偏瘦: 'info',
    正常: 'success',
    偏胖: 'warning',
    肥胖: 'danger',
  }
  return map[status] || 'info'
}

const rules: FormRules = {
  weightKg: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 20, max: 300, message: '体重范围: 20-300kg', trigger: 'blur' },
  ],
}

const loadData = async () => {
  try {
    loading.value = true

    // 加载最新体重记录
    const weightRecords = await healthApi.listWeightRecords({ pageNum: 1, pageSize: 1 })
    if (weightRecords.items && weightRecords.items.length > 0) {
      latestWeight.value = weightRecords.items[0].weightKg
      form.weightKg = latestWeight.value
    }

    // 加载身体信息
    bodyInfo.value = await healthApi.getBodyInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
  formRef.value?.resetFields()
  form.weightKg = latestWeight.value || 0
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const dto: WeightRecordDTO = {
      weightKg: form.weightKg,
    }

    await healthApi.createWeightRecord(dto)
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
    loadData()
  }
})

onMounted(() => {
  if (props.visible) {
    loadData()
  }
})
</script>

<style scoped lang="scss">
.bmi-status {
  margin-top: 8px;
}

.bmi-hint {
  margin-top: 8px;
}
</style>

