<template>
  <div class="asset-settings-view">
    <el-card>
      <template #header>
        <span>资产设置</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="200px"
        style="max-width: 700px"
      >
        <el-form-item label="退役资产不计入总额" prop="excludeRetired">
          <el-switch v-model="form.excludeRetired" />
          <div class="form-item-desc">
            开启后，已退役的资产将不计入总资产统计
          </div>
        </el-form-item>

        <el-form-item label="总资产算二手盈利" prop="calculateProfit">
          <el-switch v-model="form.calculateProfit" />
          <div class="form-item-desc">
            开启后，总资产统计将包含二手盈利金额
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存设置
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { assetSettingsApi } from '@/services/assetSettingsApi'
import type { AssetSettings, AssetSettingsUpdateDTO } from '@/types/assetSettings'

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive<AssetSettingsUpdateDTO>({
  excludeRetired: false,
  calculateProfit: false,
})

const originalForm = reactive<AssetSettingsUpdateDTO>({
  excludeRetired: false,
  calculateProfit: false,
})

const rules: FormRules = {
  excludeRetired: [
    { required: true, message: '请选择是否排除退役资产', trigger: 'change' },
  ],
  calculateProfit: [
    { required: true, message: '请选择是否计算二手盈利', trigger: 'change' },
  ],
}

const loadSettings = async () => {
  loading.value = true
  try {
    const res = await assetSettingsApi.getAssetSettings()
    const settings = res.data
    if (settings) {
      form.excludeRetired = settings.excludeRetired ?? false
      form.calculateProfit = settings.calculateProfit ?? false
      originalForm.excludeRetired = settings.excludeRetired ?? false
      originalForm.calculateProfit = settings.calculateProfit ?? false
    }
  } catch (error) {
    ElMessage.error('加载设置失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      saveSettings()
    }
  })
}

const saveSettings = async () => {
  saving.value = true
  try {
    const res = await assetSettingsApi.updateAssetSettings({
      excludeRetired: form.excludeRetired,
      calculateProfit: form.calculateProfit,
    })
    if (res.data) {
      originalForm.excludeRetired = res.data.excludeRetired
      originalForm.calculateProfit = res.data.calculateProfit
    }
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  form.excludeRetired = originalForm.excludeRetired
  form.calculateProfit = originalForm.calculateProfit
  ElMessage.info('已重置为上次保存的值')
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.asset-settings-view {
  padding: 16px;
}

.form-item-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>

