<template>
  <el-dialog
    v-model="dialogVisible"
    title="记录饮水量"
    :width="isMobile ? '90%' : '500px'"
    @close="handleClose"
  >
    <div class="water-record-content">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="饮水量" prop="volumeMl">
          <el-input-number
            v-model="form.volumeMl"
            :min="1"
            :max="10000"
            placeholder="请输入饮水量(ml)"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="快捷饮水量">
          <div class="quick-buttons">
            <el-button
              v-for="volume in quickVolumes"
              :key="volume"
              type="primary"
              size="large"
              @click="handleQuickSelect(volume)"
              :loading="submitting"
            >
              {{ volume }}ml
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="饮水类型" prop="waterType">
          <el-select
            v-model="form.waterType"
            placeholder="请选择饮水类型"
            style="width: 100%"
          >
            <el-option
              v-for="type in waterTypes"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注（可选）"
            maxlength="200"
            show-word-limit
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
 */

import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { healthApi } from '@/services/healthApi'
import type { WaterIntakeDTO, WaterType } from '@/types/health'

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

const waterTypes: WaterType[] = ['白开水', '矿泉水', '纯净水', '茶水', '咖啡', '果汁', '运动饮料', '其他']

const form = reactive<{
  volumeMl: number
  waterType: WaterType
  notes?: string
}>({
  volumeMl: 0,
  waterType: '白开水',
  notes: '',
})

const rules: FormRules = {
  volumeMl: [
    { required: true, message: '请输入饮水量', trigger: 'blur' },
    { type: 'number', min: 1, message: '饮水量必须大于0', trigger: 'blur' },
  ],
  waterType: [
    { required: true, message: '请选择饮水类型', trigger: 'change' },
  ],
}

const submitWaterIntake = async (volumeMl: number, waterType?: WaterType, notes?: string) => {
  try {
    submitting.value = true

    const dto: WaterIntakeDTO = {
      waterType: waterType || form.waterType || '白开水',
      volumeMl,
      notes: notes || form.notes || undefined,
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

// 选择快捷饮水量：仅填充到表单中，需点击“确认”按钮才会真正保存
const handleQuickSelect = (volume: number) => {
  form.volumeMl = volume
}

const handleCustomSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    if (form.volumeMl <= 0) {
      ElMessage.error('请输入有效的饮水量')
      return
    }
    await submitWaterIntake(form.volumeMl, form.waterType, form.notes)
  } catch (error: any) {
    // 验证失败,不处理
  }
}

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
  formRef.value?.resetFields()
  form.volumeMl = 0
  form.waterType = '白开水'
  form.notes = ''
}

// 弹窗打开时，自动加载用户最近一次饮水记录并填充表单
watch(
  () => dialogVisible.value,
  async (visible) => {
    if (!visible) return

    try {
      const latest = await healthApi.getLatestWaterIntake()
      if (latest) {
        form.volumeMl = latest.volumeMl
        form.waterType = latest.waterType
        form.notes = latest.notes || ''
      } else {
        // 如果没有历史记录，保持默认值
        form.volumeMl = 0
        form.waterType = '白开水'
        form.notes = ''
      }
    } catch (error) {
      // 忽略预填充失败，不影响用户手动输入
    }
  },
  { immediate: false }
)
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

