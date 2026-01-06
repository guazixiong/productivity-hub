<template>
  <div class="asset-form-view">
    <el-page-header @back="handleBack">
      <template #content>
        <span>{{ isEdit ? '编辑资产' : '创建资产' }}</span>
      </template>
    </el-page-header>

    <el-card class="mt-16" v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="资产名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入资产名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0.01"
                :step="1"
                :precision="2"
                class="w-100"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-input
                v-model="selectedCategoryName"
                placeholder="请选择分类"
                readonly
                class="w-100"
                @click="showCategoryDialog = true"
              >
                <template #suffix>
                  <el-icon class="cursor-pointer" @click="showCategoryDialog = true">
                    <ArrowDown />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买日期" prop="purchaseDate">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择购买日期"
                class="w-100"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="图片 URL" prop="image">
              <el-input
                v-model="form.image"
                placeholder="请输入图片链接（可选）"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
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
              <el-switch v-model="form.warrantyEnabled" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保修截止日期" prop="warrantyEndDate">
              <el-date-picker
                v-model="form.warrantyEndDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择保修截止日期"
                class="w-100"
                :disabled="!form.warrantyEnabled"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="按使用次数贬值" prop="depreciationByUsageCount">
              <el-switch v-model="form.depreciationByUsageCount" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计使用次数" prop="expectedUsageCount">
              <el-input-number
                v-model="form.expectedUsageCount"
                :min="1"
                :disabled="!form.depreciationByUsageCount"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="按使用日期贬值" prop="depreciationByUsageDate">
              <el-switch v-model="form.depreciationByUsageDate" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用日期" prop="usageDate">
              <el-date-picker
                v-model="form.usageDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择使用日期"
                class="w-100"
                :disabled="!form.depreciationByUsageDate"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="是否在服役" prop="inService">
              <el-switch v-model="form.inService" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退役日期" prop="retiredDate">
              <el-date-picker
                v-model="form.retiredDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择退役日期"
                class="w-100"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分类选择弹窗 -->
    <CategoryPickerDialog
      v-model="showCategoryDialog"
      :current-category-id="form.categoryId"
      @confirm="handleCategorySelect"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import type {
  AssetCategory,
  AssetCreateDTO,
  AssetDetail,
  AssetUpdateDTO,
} from '@/types/asset'
import { assetApi } from '@/services/assetApi'
import CategoryPickerDialog from '@/components/asset/CategoryPickerDialog.vue'
import { useTabsStore } from '@/stores/tabs'
import { useAssetCategory } from '@/composables/useAssetCategory'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()
const { getSelectableCategories, findCategoryName } = useAssetCategory()

const formRef = ref<FormInstance>()
const loading = ref(false)
const showCategoryDialog = ref(false)
const selectedCategoryName = ref('')

const form = reactive<AssetCreateDTO & { id?: string; version?: number }>({
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

const isEdit = computed(() => !!route.params.id)

const rules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购买日期', trigger: 'change' }],
}

// 加载分类名称（用于显示）
const loadCategoryName = async (categoryId: string) => {
  if (!categoryId) {
    selectedCategoryName.value = ''
    return
  }
  try {
    const categories = await getSelectableCategories()
    selectedCategoryName.value = findCategoryName(categoryId, categories)
  } catch (error) {
    selectedCategoryName.value = ''
  }
}

// 处理分类选择
const handleCategorySelect = async (category: AssetCategory) => {
  form.categoryId = category.id
  // 加载分类名称（会查找父分类信息）
  await loadCategoryName(category.id)
}

const fillFormFromDetail = async (detail: AssetDetail) => {
  form.name = detail.name
  form.price = detail.price
  form.image = detail.image || ''
  form.remark = detail.remark || ''
  form.categoryId = detail.categoryId
  form.purchaseDate = detail.purchaseDate
  form.warrantyEnabled = detail.warrantyEnabled || false
  form.warrantyEndDate = detail.warrantyEndDate || ''
  form.depreciationByUsageCount = detail.depreciationByUsageCount || false
  form.expectedUsageCount = detail.expectedUsageCount
  form.depreciationByUsageDate = detail.depreciationByUsageDate || false
  form.usageDate = detail.usageDate || ''
  form.inService = detail.inService ?? true
  form.retiredDate = detail.retiredDate || ''
  ;(form as any).id = detail.id
  ;(form as any).version = (detail as any).version ?? 0
  
  // 加载分类名称
  if (detail.categoryId) {
    await loadCategoryName(detail.categoryId)
  }
}

const loadDetailIfEdit = async () => {
  if (!isEdit.value) return
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    const res = await assetApi.getAssetById(id)
    if (res) {
      fillFormFromDetail(res)
    }
  } catch (error) {
    ElMessage.error('加载资产详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (isEdit.value) {
        const payload: AssetUpdateDTO = {
          ...(form as AssetCreateDTO),
          id: (form as any).id as string,
          version: ((form as any).version as number) ?? 0,
        }
        await assetApi.updateAsset(payload)
        ElMessage.success('更新成功')
        // 刷新资产列表页
        tabsStore.refreshTab('/assets')
      } else {
        const payload: AssetCreateDTO = {
          ...(form as AssetCreateDTO),
        }
        await assetApi.createAsset(payload)
        ElMessage.success('创建成功')
        // 刷新资产列表页
        tabsStore.refreshTab('/assets')
        // 关闭当前创建表单的标签页
        tabsStore.removeTab(route.fullPath)
      }
      router.push('/assets')
    } finally {
      loading.value = false
    }
  })
}

const handleBack = () => {
  router.back()
}

onMounted(async () => {
  await loadDetailIfEdit()
})
</script>

<style scoped>
.asset-form-view {
  padding: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.w-100 {
  width: 100%;
}

.cursor-pointer {
  cursor: pointer;
}
</style>


