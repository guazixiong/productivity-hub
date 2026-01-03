<template>
  <div class="asset-form">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="资产名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入资产名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="分类" prop="categoryId">
            <el-select v-model="formData.categoryId" placeholder="请选择分类" class="w-100">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="资产价格" prop="price">
            <el-input-number
              v-model="formData.price"
              :min="0.01"
              :step="1"
              :precision="2"
              class="w-100"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="购买日期" prop="purchaseDate">
            <el-date-picker
              v-model="formData.purchaseDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择购买日期"
              class="w-100"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="图片 URL" prop="image">
            <el-input v-model="formData.image" placeholder="请输入图片链接（可选）" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              placeholder="请输入备注（可选）"
              type="textarea"
              :rows="3"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider>保修 & 贬值设置</el-divider>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item label="启用保修" prop="warrantyEnabled">
            <el-switch v-model="formData.warrantyEnabled" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="保修截止日期" prop="warrantyEndDate">
            <el-date-picker
              v-model="formData.warrantyEndDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择保修截止日期"
              class="w-100"
              :disabled="!formData.warrantyEnabled"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="按使用次数贬值" prop="depreciationByUsageCount">
            <el-switch v-model="formData.depreciationByUsageCount" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计使用次数" prop="expectedUsageCount">
            <el-input-number
              v-model="formData.expectedUsageCount"
              :min="1"
              :disabled="!formData.depreciationByUsageCount"
              class="w-100"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="按使用日期贬值" prop="depreciationByUsageDate">
            <el-switch v-model="formData.depreciationByUsageDate" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="使用日期" prop="usageDate">
            <el-date-picker
              v-model="formData.usageDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择使用日期"
              class="w-100"
              :disabled="!formData.depreciationByUsageDate"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="是否在服役" prop="inService">
            <el-switch v-model="formData.inService" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="退役日期" prop="retiredDate">
            <el-date-picker
              v-model="formData.retiredDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择退役日期"
              class="w-100"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type {
  AssetCategory,
  AssetCreateDTO,
  AssetDetail,
  AssetUpdateDTO,
} from '@/types/asset'

const props = defineProps<{
  asset?: AssetDetail | null
  categories: AssetCategory[]
}>()

const emit = defineEmits<{
  submit: [formData: AssetCreateDTO | AssetUpdateDTO]
  cancel: []
}>()

const formRef = ref<FormInstance>()

const formData = reactive<AssetCreateDTO & { id?: string; version?: number }>({
  name: '',
  price: 0,
  image: '',
  remark: '',
  categoryId: '',
  purchaseDate: '',
  warrantyEnabled: false,
  warrantyEndDate: '',
  depreciationByUsageCount: false,
  expectedUsageCount: undefined,
  depreciationByUsageDate: false,
  usageDate: '',
  inService: true,
  retiredDate: '',
})

// 监听 asset prop 变化，填充表单
watch(
  () => props.asset,
  (newAsset) => {
    if (newAsset) {
      formData.name = newAsset.name
      formData.price = newAsset.price
      formData.image = newAsset.image || ''
      formData.remark = newAsset.remark || ''
      formData.categoryId = newAsset.categoryId
      formData.purchaseDate = newAsset.purchaseDate
      formData.warrantyEnabled = newAsset.warrantyEnabled || false
      formData.warrantyEndDate = newAsset.warrantyEndDate || ''
      formData.depreciationByUsageCount = newAsset.depreciationByUsageCount || false
      formData.expectedUsageCount = newAsset.expectedUsageCount
      formData.depreciationByUsageDate = newAsset.depreciationByUsageDate || false
      formData.usageDate = newAsset.usageDate || ''
      formData.inService = newAsset.inService ?? true
      formData.retiredDate = newAsset.retiredDate || ''
      ;(formData as any).id = newAsset.id
      ;(formData as any).version = (newAsset as any).version ?? 0
    } else {
      // 重置表单
      Object.assign(formData, {
        name: '',
        price: 0,
        image: '',
        remark: '',
        categoryId: '',
        purchaseDate: '',
        warrantyEnabled: false,
        warrantyEndDate: '',
        depreciationByUsageCount: false,
        expectedUsageCount: undefined,
        depreciationByUsageDate: false,
        usageDate: '',
        inService: true,
        retiredDate: '',
      })
      delete (formData as any).id
      delete (formData as any).version
    }
  },
  { immediate: true }
)

const rules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购买日期', trigger: 'change' }],
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      if (props.asset) {
        // 编辑模式
        const payload: AssetUpdateDTO = {
          ...(formData as AssetCreateDTO),
          id: (formData as any).id as string,
          version: ((formData as any).version as number) ?? 0,
        }
        emit('submit', payload)
      } else {
        // 创建模式
        const payload: AssetCreateDTO = {
          ...(formData as AssetCreateDTO),
        }
        emit('submit', payload)
      }
    }
  })
}

const handleCancel = () => {
  emit('cancel')
}

// 暴露表单验证方法
defineExpose({
  validate: () => formRef.value?.validate(),
  resetFields: () => formRef.value?.resetFields(),
})
</script>

<style scoped lang="scss">
.asset-form {
  .w-100 {
    width: 100%;
  }
}
</style>

