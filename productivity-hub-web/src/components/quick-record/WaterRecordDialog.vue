<template>
  <el-dialog
    v-model="dialogVisible"
    title="记录饮水量"
    :width="isMobile ? '90%' : '400px'"
    @close="handleClose"
  >
    <div class="water-record-content">
      <div class="quick-buttons">
        <el-button
          v-for="volume in quickVolumes"
          :key="volume"
          type="primary"
          size="large"
          @click="handleQuickSubmit(volume)"
          :loading="submitting"
        >
          {{ volume }}ml
        </el-button>
      </div>

      <el-divider>或自定义</el-divider>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
      >
        <el-form-item prop="volumeMl">
          <el-input-number
            v-model="form.volumeMl"
            :min="1"
            :max="10000"
            placeholder="请输入饮水量(ml)"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleCustomSubmit">
        确认
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * 新增饮水记录弹窗组件
 * 
 * 关联需求: REQ-005
 * 关联组件: COMP-REQ-001-01-05
 * 关联接口: API-REQ-002-03
 * 关联测试用例: TC-COMP-REQ-001-01-05-01, TC-COMP-REQ-001-01-05-02
 */

import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { healthApi } from '@/services/healthApi'
import type { WaterIntakeDTO } from '@/types/health'

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

const quickVolumes = [200, 400, 500]

const form = reactive<{ volumeMl: number }>({
  volumeMl: 0,
})

const rules: FormRules = {
  volumeMl: [
    { required: true, message: '请输入饮水量', trigger: 'blur' },
    { type: 'number', min: 1, message: '饮水量必须大于0', trigger: 'blur' },
  ],
}

const submitWaterIntake = async (volumeMl: number) => {
  try {
    submitting.value = true

    const dto: WaterIntakeDTO = {
      waterType: '白开水',
      volumeMl,
    }

    await healthApi.createWaterIntake(dto)
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

const handleQuickSubmit = (volume: number) => {
  submitWaterIntake(volume)
}

const handleCustomSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.volumeMl <= 0) {
      ElMessage.error('请输入有效的饮水量')
      return
    }
    await submitWaterIntake(form.volumeMl)
  } catch (error: any) {
    // 验证失败,不处理
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
  formRef.value?.resetFields()
  form.volumeMl = 0
}
</script>

<style scoped lang="scss">
.water-record-content {
  .quick-buttons {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }
}
</style>

