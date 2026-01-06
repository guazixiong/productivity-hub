<template>
  <div class="asset-list-view">
    <el-card class="main-card" shadow="hover">
      <template #header>
        <div class="asset-list-header">
          <div class="header-title">
            <el-icon class="title-icon"><Box /></el-icon>
            <span class="title-text">资产列表</span>
            <el-tag v-if="pagination.total > 0" type="info" size="small" class="total-tag">
              共 {{ pagination.total }} 项
            </el-tag>
          </div>
          <div class="asset-list-filters">
            <el-input
              v-model="filterCategoryName"
              placeholder="选择分类"
              readonly
              class="filter-item filter-category-input"
              @click="showFilterCategoryDialog = true"
            >
              <template #suffix>
                <el-icon class="cursor-pointer" @click.stop="showFilterCategoryDialog = true">
                  <ArrowDown />
                </el-icon>
              </template>
            </el-input>
            <el-button
              v-if="filters.categoryId"
              :icon="Close"
              circle
              size="small"
              class="filter-clear-btn"
              @click="handleClearCategoryFilter"
            />
            <el-select
              v-model="filters.status"
              placeholder="选择状态"
              clearable
              class="filter-item"
              @change="handleFilterChange"
            >
              <el-option label="正在服役" value="IN_SERVICE" />
              <el-option label="已退役" value="RETIRED" />
            </el-select>
            <el-button type="primary" :icon="Plus" @click="handleCreate">
              创建资产
            </el-button>
            <el-button
              type="danger"
              :icon="Delete"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="assets"
        stripe
        style="width: 100%"
        class="asset-table"
        :row-class-name="tableRowClassName"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="资产名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="资产分类" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getCategoryDisplayTextSync(row.categoryId) || row.categoryName }}
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购买日期" min-width="120" />
        <el-table-column prop="usageDays" label="已使用天数" min-width="120" align="center">
          <template #default="{ row }">
            {{ calculateUsageDays(row.purchaseDate, row.retiredDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="dailyAveragePrice" label="日均价格" min-width="140">
          <template #default="{ row }">
            <span class="daily-price-text">{{ formatCurrency(row.dailyAveragePrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="资产价格" min-width="130">
          <template #default="{ row }">
            <span class="price-text">{{ formatCurrency(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="additionalFeesTotal" label="附加费用(合计)" min-width="140">
          <template #default="{ row }">
            <span class="additional-fees-text">{{ formatCurrency(row.additionalFeesTotal || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalValue" label="资产总价格" min-width="140">
          <template #default="{ row }">
            <span class="value-text">{{ formatCurrency(row.totalValue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" effect="dark" round>
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="140" align="center">
          <template #default="{ row }">
            <el-dropdown @command="(command) => handleCommand(command, row)" trigger="click">
              <el-button type="primary" link class="action-btn">
                <el-icon><MoreFilled /></el-icon>
                操作
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view" class="dropdown-item">
                    <el-icon class="item-icon"><View /></el-icon>
                    <span>查看详情</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="edit" class="dropdown-item">
                    <el-icon class="item-icon"><Edit /></el-icon>
                    <span>编辑</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided class="dropdown-item danger-item">
                    <el-icon class="item-icon"><Delete /></el-icon>
                    <span>删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="asset-list-pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </el-card>

    <!-- 抽屉：用于创建、编辑和详情 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      :size="drawerSize"
      direction="rtl"
      :before-close="handleDrawerClose"
    >
      <!-- 详情模式 -->
      <div v-if="drawerMode === 'view'" v-loading="detailLoading" class="drawer-content">
        <div v-if="currentAsset" class="detail-wrapper">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-card class="image-card" shadow="hover">
                <div class="asset-image-wrapper" v-if="currentAsset.image">
                  <el-image :src="currentAsset.image" fit="contain" class="asset-image" />
                </div>
                <div v-else class="asset-image-placeholder">
                  <el-icon class="placeholder-icon"><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </el-card>
              <el-card class="info-card mt-16" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><InfoFilled /></el-icon>
                    <span>基本信息</span>
                  </div>
                </template>
                <el-descriptions :column="1" border class="detail-descriptions">
                <el-descriptions-item label="资产名称">
                  {{ currentAsset.name }}
                </el-descriptions-item>
                <el-descriptions-item label="分类">
                  {{ getCategoryDisplayTextSync(currentAsset.categoryId) || currentAsset.categoryName }}
                </el-descriptions-item>
                <el-descriptions-item label="价格">
                  <span class="highlight-price">{{ formatCurrency(currentAsset.price) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusTagType(currentAsset.status)" effect="dark" round>
                    {{ formatStatus(currentAsset.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="购买日期">
                  <el-icon class="inline-icon"><Calendar /></el-icon>
                  {{ currentAsset.purchaseDate }}
                </el-descriptions-item>
                <el-descriptions-item label="退役日期">
                  <el-icon class="inline-icon"><Calendar /></el-icon>
                  {{ currentAsset.retiredDate || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="总价值">
                  <span class="highlight-value">{{ formatCurrency(currentAsset.totalValue) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="日均价格">
                  <span class="highlight-daily">{{ formatCurrency(currentAsset.dailyAveragePrice) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="已使用天数">
                  {{ calculateUsageDays(currentAsset.purchaseDate, currentAsset.retiredDate) }}
                </el-descriptions-item>
                <el-descriptions-item label="是否在服役">
                  {{ currentAsset.inService ? '是' : '否' }}
                </el-descriptions-item>
              </el-descriptions>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="warranty-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Lock /></el-icon>
                    <span>保修信息 & 贬值策略</span>
                  </div>
                </template>
                <el-descriptions :column="1" border class="detail-descriptions">
                  <el-descriptions-item label="启用保修">
                    {{ currentAsset.warrantyEnabled ? '是' : '否' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="保修截止日期">
                    {{ currentAsset.warrantyEndDate || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="按使用次数贬值">
                    {{ currentAsset.depreciationByUsageCount ? '是' : '否' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="预计使用次数">
                    {{ currentAsset.expectedUsageCount || '-' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="按使用日期贬值">
                    {{ currentAsset.depreciationByUsageDate ? '是' : '否' }}
                  </el-descriptions-item>
                  <el-descriptions-item label="使用日期">
                    {{ currentAsset.usageDate || '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>

              <el-card class="mt-16 additional-fees-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Money /></el-icon>
                    <span>附加费用</span>
                    <span v-if="currentAsset.additionalFees && currentAsset.additionalFees.length > 0" class="fee-total">
                      (合计: {{ formatCurrency(calculateAdditionalFeesTotal(currentAsset.additionalFees)) }})
                    </span>
                  </div>
                </template>
                <div v-if="currentAsset.additionalFees && currentAsset.additionalFees.length > 0" class="fees-list">
                  <div v-for="fee in currentAsset.additionalFees" :key="fee.id || fee.name" class="fee-item">
                    <div class="fee-header">
                      <span class="fee-name">{{ fee.name }}</span>
                      <span class="fee-amount">{{ formatCurrency(fee.amount) }}</span>
                    </div>
                    <div class="fee-meta">
                      <span class="fee-date">{{ fee.feeDate }}</span>
                      <span v-if="fee.remark" class="fee-remark">{{ fee.remark }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-fees">
                  <el-icon><Money /></el-icon>
                  <span>暂无附加费用</span>
                </div>
              </el-card>

              <el-card class="mt-16 remark-card" shadow="never">
                <template #header>
                  <div class="card-header">
                    <el-icon><Document /></el-icon>
                    <span>备注</span>
                  </div>
                </template>
                <div class="remark-content">
                  {{ currentAsset.remark || '暂无备注' }}
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <div class="drawer-footer">
          <el-button type="primary" :icon="Edit" @click="handleEditFromDetail">编辑</el-button>
          <el-button :icon="Close" @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>

      <!-- 创建/编辑模式 -->
      <div v-else v-loading="formLoading" class="drawer-content">
        <el-scrollbar class="form-scrollbar">
          <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            label-width="120px"
            class="asset-form"
          >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="资产名称" prop="name">
                <el-input 
                  v-model="form.name" 
                  placeholder="请输入资产名称" 
                  :maxlength="100"
                  show-word-limit
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
              <el-form-item label="图片 URL" prop="image">
                <el-input
                  v-model="form.image"
                  placeholder="请输入图片链接（可选）"
                  :maxlength="500"
                  show-word-limit
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
                  :maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-col>

            <el-col :span="24">
              <el-form-item label="附加费用">
                <div class="additional-fees-form">
                  <div v-for="(fee, index) in formAdditionalFees" :key="index" class="fee-form-item">
                    <el-row :gutter="12">
                      <el-col :span="6">
                        <el-input
                          v-model="fee.name"
                          placeholder="费用名称"
                          :maxlength="50"
                        />
                      </el-col>
                      <el-col :span="5">
                        <el-input-number
                          v-model="fee.amount"
                          :min="0.01"
                          :step="1"
                          :precision="2"
                          placeholder="金额"
                          class="w-100"
                        />
                      </el-col>
                      <el-col :span="5">
                        <el-date-picker
                          v-model="fee.feeDate"
                          type="date"
                          value-format="YYYY-MM-DD"
                          placeholder="费用日期"
                          class="w-100"
                        />
                      </el-col>
                      <el-col :span="6">
                        <el-input
                          v-model="fee.remark"
                          placeholder="备注（可选）"
                          :maxlength="100"
                        />
                      </el-col>
                      <el-col :span="2">
                        <el-button
                          :icon="Remove"
                          circle
                          type="danger"
                          size="small"
                          @click="removeAdditionalFee(index)"
                        />
                      </el-col>
                    </el-row>
                  </div>
                  <el-button
                    type="primary"
                    :icon="Plus"
                    size="small"
                    plain
                    @click="addAdditionalFee"
                    class="add-fee-btn"
                  >
                    添加附加费用
                  </el-button>
                  <div v-if="formAdditionalFees.length > 0" class="fee-total-info">
                    合计: {{ formatCurrency(calculateAdditionalFeesTotal(formAdditionalFees)) }}
                  </div>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider class="form-divider">
            <el-icon><Setting /></el-icon>
            <span>保修 & 贬值设置</span>
          </el-divider>

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
        </el-form>
        </el-scrollbar>
        <div class="drawer-footer">
          <el-button type="primary" :icon="drawerMode === 'edit' ? Check : Plus" @click="handleFormSubmit" :loading="submitting">
            {{ drawerMode === 'edit' ? '保存修改' : '创建资产' }}
          </el-button>
          <el-button :icon="Close" @click="drawerVisible = false">取消</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 分类选择弹窗（表单用） -->
    <CategoryPickerDialog
      v-model="showCategoryDialog"
      :current-category-id="form.categoryId"
      @confirm="handleCategorySelect"
    />

    <!-- 分类选择弹窗（筛选用） -->
    <CategoryPickerDialog
      v-model="showFilterCategoryDialog"
      :current-category-id="filters.categoryId"
      @confirm="handleFilterCategorySelect"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  ArrowDown,
  View,
  Edit,
  Delete,
  Plus,
  Box,
  MoreFilled,
  InfoFilled,
  Picture,
  Calendar,
  Lock,
  Document,
  Close,
  Check,
  Setting,
  Money,
  Remove,
} from '@element-plus/icons-vue'
import { assetApi, assetAdditionalFeeApi } from '@/services/assetApi'
import type {
  AssetCategory,
  AssetCreateDTO,
  AssetDetail,
  AssetListItem,
  AssetStatus,
  AssetUpdateDTO,
  AssetAdditionalFee,
  AssetAdditionalFeeCreateDTO,
  AssetAdditionalFeeUpdateDTO,
} from '@/types/asset'
import { formatCurrency } from '@/utils/format'
import CategoryPickerDialog from '@/components/asset/CategoryPickerDialog.vue'
import { useDevice } from '@/composables/useDevice'
import { useAssetCategory } from '@/composables/useAssetCategory'
import { useRoute, useRouter } from 'vue-router'

const { isMobile } = useDevice()
const { getSelectableCategories, findCategoryName } = useAssetCategory()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const assets = ref<AssetListItem[]>([])
const selectedIds = ref<string[]>([])
const isInitialized = ref(false)

// 抽屉相关状态
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit' | 'view'>('create')
const drawerTitle = computed(() => {
  const titles = {
    create: '创建资产',
    edit: '编辑资产',
    view: '资产详情',
  }
  return titles[drawerMode.value]
})
const drawerSize = computed(() => (isMobile.value ? '100%' : '60%'))

// 详情相关
const detailLoading = ref(false)
const currentAsset = ref<AssetDetail | null>(null)

// 表单相关
const formRef = ref<FormInstance>()
const formLoading = ref(false)
const submitting = ref(false)
const showCategoryDialog = ref(false)
const selectedCategoryName = ref('')
const formAdditionalFees = ref<AssetAdditionalFee[]>([]) // 表单中的附加费用列表

// 筛选相关
const showFilterCategoryDialog = ref(false)
const filterCategoryName = ref('')

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

const formRules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购买日期', trigger: 'change' }],
}

const filters = reactive<{
  categoryId?: string
  status?: AssetStatus
}>({
  categoryId: undefined,
  status: undefined,
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0,
})

const tableRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  if (rowIndex % 2 === 1) {
    return 'highlight-row'
  }
  return ''
}

const getStatusTagType = (status: AssetStatus) => {
  const map: Record<AssetStatus, 'success' | 'warning' | 'info'> = {
    IN_SERVICE: 'success',
    RETIRED: 'warning',
    SOLD: 'info',
  }
  return map[status] || 'info'
}

const formatStatus = (status: AssetStatus) => {
  const map: Record<AssetStatus, string> = {
    IN_SERVICE: '正在服役',
    RETIRED: '已退役',
    SOLD: '已卖出',
  }
  return map[status] || status
}

// 计算已使用天数：当前时间 - 购买日期 + 1（如果已退役，则使用退役日期）
const calculateUsageDays = (purchaseDate: string, retiredDate?: string): number => {
  if (!purchaseDate) return 0
  
  const purchase = new Date(purchaseDate)
  const endDate = retiredDate ? new Date(retiredDate) : new Date()
  
  // 计算天数差：当前时间 - 购入时间 + 1
  const diffTime = endDate.getTime() - purchase.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1
  
  return diffDays
}

// 计算附加费用合计
const calculateAdditionalFeesTotal = (fees?: AssetAdditionalFee[]): number => {
  if (!fees || fees.length === 0) return 0
  return fees.reduce((sum, fee) => sum + (fee.amount || 0), 0)
}

// 添加附加费用项
const addAdditionalFee = () => {
  formAdditionalFees.value.push({
    name: '',
    amount: 0,
    feeDate: '',
    remark: '',
  })
}

// 删除附加费用项
const removeAdditionalFee = (index: number) => {
  formAdditionalFees.value.splice(index, 1)
}

const loadAssets = async () => {
  loading.value = true
  try {
    const res = await assetApi.getAssetPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      categoryId: filters.categoryId,
      status: filters.status,
    })
    assets.value = res?.list || []
    pagination.total = res?.total || 0
    
    // 预加载所有分类名称
    if (assets.value.length > 0) {
      const categoryIds = [...new Set(assets.value.map(a => a.categoryId))]
      for (const categoryId of categoryIds) {
        if (!categoryNameCache.value.has(categoryId)) {
          const name = await getCategoryDisplayName(categoryId)
          categoryNameCache.value.set(categoryId, name)
        }
      }
    }
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.pageNum = 1
  loadAssets()
}

const handleFilterCategorySelect = async (category: AssetCategory) => {
  filters.categoryId = category.id
  // 使用缓存的分类数据查找名称
  const categoryList = await getSelectableCategories()
  filterCategoryName.value = findCategoryName(category.id, categoryList)
  handleFilterChange()
}

const handleClearCategoryFilter = () => {
  filters.categoryId = undefined
  filterCategoryName.value = ''
  handleFilterChange()
}

const getCategoryDisplayName = async (categoryId: string): Promise<string> => {
  if (!categoryId) return ''
  try {
    const categoryList = await getSelectableCategories()
    return findCategoryName(categoryId, categoryList)
  } catch (error) {
    return ''
  }
}

const handlePageChange = (page: number) => {
  pagination.pageNum = page
  loadAssets()
}

const handlePageSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadAssets()
}

const handleSelectionChange = (rows: AssetListItem[]) => {
  selectedIds.value = rows.map((row) => row.id)
}


// 获取分类显示文本（用于表格显示）
const categoryNameCache = ref<Map<string, string>>(new Map())
const getCategoryDisplayText = async (categoryId: string): Promise<string> => {
  if (!categoryId) return ''
  if (categoryNameCache.value.has(categoryId)) {
    return categoryNameCache.value.get(categoryId) || ''
  }
  const name = await getCategoryDisplayName(categoryId)
  categoryNameCache.value.set(categoryId, name)
  return name
}

// 同步版本，用于模板中直接调用
const getCategoryDisplayTextSync = (categoryId: string): string => {
  return categoryNameCache.value.get(categoryId) || ''
}

// 加载分类名称（用于显示）
const loadCategoryName = async (categoryId: string) => {
  if (!categoryId) {
    selectedCategoryName.value = ''
    return
  }
  try {
    const categoryList = await getSelectableCategories()
    selectedCategoryName.value = findCategoryName(categoryId, categoryList)
  } catch (error) {
    selectedCategoryName.value = ''
  }
}

// 处理分类选择
const handleCategorySelect = async (category: AssetCategory) => {
  form.categoryId = category.id
  await loadCategoryName(category.id)
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
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
    additionalFees: 0,
  })
  delete (form as any).id
  delete (form as any).version
  selectedCategoryName.value = ''
  formAdditionalFees.value = []
  formRef.value?.resetFields()
}

// 填充表单数据
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

  // 加载附加费用列表
  if (detail.id) {
    try {
      const fees = await assetAdditionalFeeApi.getFeesByAssetId(detail.id)
      formAdditionalFees.value = fees.map(fee => ({
        id: fee.id,
        name: fee.name,
        amount: fee.amount,
        feeDate: fee.feeDate,
        remark: fee.remark || '',
      }))
    } catch (error) {
      formAdditionalFees.value = []
    }
  } else {
    formAdditionalFees.value = []
  }

  if (detail.categoryId) {
    await loadCategoryName(detail.categoryId)
  }
}

// 处理下拉菜单命令
const handleCommand = (command: string, row: AssetListItem) => {
  if (command === 'view') {
    handleView(row)
  } else if (command === 'edit') {
    handleEdit(row)
  } else if (command === 'delete') {
    handleDelete(row)
  }
}

// 打开创建抽屉
const handleCreate = () => {
  drawerMode.value = 'create'
  resetForm()
  drawerVisible.value = true
}

// 打开查看抽屉
const handleView = async (row: AssetListItem) => {
  drawerMode.value = 'view'
  detailLoading.value = true
  drawerVisible.value = true
  try {
    const res = await assetApi.getAssetById(row.id)
    currentAsset.value = res || null
    // 加载分类名称到缓存
    if (res?.categoryId && !categoryNameCache.value.has(res.categoryId)) {
      const name = await getCategoryDisplayName(res.categoryId)
      categoryNameCache.value.set(res.categoryId, name)
    }
  } catch (error) {
    ElMessage.error('加载资产详情失败')
    drawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

// 从详情页打开编辑
const handleEditFromDetail = async () => {
  if (!currentAsset.value) return
  drawerMode.value = 'edit'
  await fillFormFromDetail(currentAsset.value)
  currentAsset.value = null
}

// 打开编辑抽屉
const handleEdit = async (row: AssetListItem) => {
  drawerMode.value = 'edit'
  formLoading.value = true
  drawerVisible.value = true
  try {
    const res = await assetApi.getAssetById(row.id)
    if (res) {
      await fillFormFromDetail(res)
    }
  } catch (error) {
    ElMessage.error('加载资产详情失败')
    drawerVisible.value = false
  } finally {
    formLoading.value = false
  }
}

// 删除资产
const handleDelete = async (row: AssetListItem) => {
  await ElMessageBox.confirm('确定要删除该资产吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await assetApi.deleteAsset(row.id)
  ElMessage.success('删除成功')
  loadAssets()
}

// 提交表单
const handleFormSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      let assetId: string
      if (drawerMode.value === 'edit') {
        const payload: AssetUpdateDTO = {
          ...(form as AssetCreateDTO),
          id: (form as any).id as string,
          version: ((form as any).version as number) ?? 0,
        }
        const result = await assetApi.updateAsset(payload)
        assetId = result.id
        ElMessage.success('更新成功')
      } else {
        const payload: AssetCreateDTO = {
          ...(form as AssetCreateDTO),
        }
        const result = await assetApi.createAsset(payload)
        assetId = result.id
        ElMessage.success('创建成功')
      }

      // 保存附加费用
      if (assetId) {
        try {
          // 获取现有的附加费用列表
          const existingFees = await assetAdditionalFeeApi.getFeesByAssetId(assetId)
          const existingFeeIds = new Set(existingFees.map(f => f.id).filter(Boolean))
          const formFeeIds = new Set(formAdditionalFees.value.map(f => f.id).filter(Boolean))

          // 删除已移除的费用
          for (const existingFee of existingFees) {
            if (existingFee.id && !formFeeIds.has(existingFee.id)) {
              await assetAdditionalFeeApi.deleteFee(existingFee.id)
            }
          }

          // 创建或更新费用
          for (const fee of formAdditionalFees.value) {
            // 过滤掉空数据
            if (!fee.name || !fee.amount || !fee.feeDate) {
              continue
            }

            if (fee.id && existingFeeIds.has(fee.id)) {
              // 更新现有费用
              await assetAdditionalFeeApi.updateFee({
                id: fee.id,
                name: fee.name,
                amount: fee.amount,
                feeDate: fee.feeDate,
                remark: fee.remark || '',
              })
            } else {
              // 创建新费用
              await assetAdditionalFeeApi.createFee({
                assetId,
                name: fee.name,
                amount: fee.amount,
                feeDate: fee.feeDate,
                remark: fee.remark || '',
              })
            }
          }
        } catch (error) {
          ElMessage.warning('资产保存成功，但附加费用保存失败')
        }
      }

      drawerVisible.value = false
      loadAssets()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 抽屉关闭前处理
const handleDrawerClose = (done: () => void) => {
  if (submitting.value) {
    return
  }
  resetForm()
  currentAsset.value = null
  done()
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  await ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 个资产吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
  await assetApi.batchDeleteAssets(selectedIds.value)
  ElMessage.success('批量删除成功')
  selectedIds.value = []
  loadAssets()
}

onMounted(async () => {
  await loadAssets()
  // 如果有筛选分类，加载分类名称
  if (filters.categoryId) {
    filterCategoryName.value = await getCategoryDisplayName(filters.categoryId)
  }
  
  // 检查是否从心愿单跳转过来，如果是则自动打开创建表单
  const fromWishlist = route.query.fromWishlist === 'true'
  if (fromWishlist) {
    handleCreate()
  }
  
  isInitialized.value = true
})

// 当页面被激活时（从其他页面返回时）刷新列表
onActivated(() => {
  // 只在已经初始化后才刷新，避免首次加载时重复请求
  if (isInitialized.value) {
    loadAssets()
  }
})
</script>

<style scoped lang="scss">
.asset-list-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.main-card {
  border-radius: 12px;
  overflow: hidden;
}

.asset-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .title-icon {
    font-size: 24px;
  }
  
  .title-text {
    font-size: 20px;
    font-weight: 600;
  }
}

.asset-list-filters {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-item {
  width: 140px;
}

.filter-category-input {
  cursor: pointer;
  
  :deep(.el-input__inner) {
    cursor: pointer;
  }
}

.filter-clear-btn {
  margin-left: 8px;
}

.asset-table {
  margin-top: 16px;
  border-radius: 8px;
  overflow: hidden;
  
  :deep(.el-table__header) {
    th {
      background: #f8f9fa;
      color: #333;
      font-weight: 600;
      border-bottom: 2px solid #e9ecef;
    }
  }
  
  :deep(.el-table__row) {
    transition: all 0.3s ease;
    
    &:hover {
      background: #f0f4ff !important;
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
  }
  
  :deep(.el-table__body tr.highlight-row) {
    background: #f0f4ff;
  }
}

.price-text {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.value-text {
  font-weight: 600;
  color: #67c23a;
  font-size: 14px;
}

.daily-price-text {
  font-weight: 500;
  color: #909399;
  font-size: 14px;
}

.additional-fees-text {
  font-weight: 500;
  color: #e6a23c;
  font-size: 14px;
}

.action-btn {
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
  
  &:hover {
    background: #ecf5ff;
    transform: scale(1.05);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  
  .item-icon {
    font-size: 16px;
  }
  
  &.danger-item {
    color: #f56c6c;
    
    &:hover {
      background: #fef0f0;
    }
  }
  
  &:hover {
    background: #f5f7fa;
  }
}

.asset-list-pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
  
  :deep(.el-pagination) {
    .el-pager li {
      border-radius: 6px;
      margin: 0 4px;
    }
  }
}

.drawer-content {
  padding: 0;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.detail-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  
  .mt-16 {
    margin-top: 16px;
  }
  
  .image-card {
    border-radius: 12px;
    overflow: hidden;
    
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
  
  .asset-image-wrapper {
    width: 100%;
    padding: 12px;
    border-radius: 8px;
    background: #f8f9fa;
    text-align: center;
    min-height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .asset-image {
      max-width: 100%;
      max-height: 300px;
      border-radius: 8px;
    }
  }
  
  .asset-image-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    color: #909399;
    background: #fafafa;
    gap: 12px;
    
    .placeholder-icon {
      font-size: 48px;
      color: #c0c4cc;
    }
  }
  
  .info-card,
  .warranty-card,
  .remark-card,
  .additional-fees-card {
    border-radius: 12px;
    border: 1px solid #e4e7ed;
    
    :deep(.el-card__header) {
      background: #f8f9fa;
      border-bottom: 2px solid #e4e7ed;
      padding: 16px 20px;
    }
    
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
  
  .additional-fees-card {
    .fee-total {
      margin-left: 8px;
      font-size: 14px;
      color: #e6a23c;
      font-weight: 600;
    }
    
    .fees-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .fee-item {
      padding: 12px;
      background: #fafafa;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background: #f0f4ff;
        border-color: #409eff;
        transform: translateX(4px);
      }
      
      .fee-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .fee-name {
          font-weight: 600;
          color: #303133;
          font-size: 15px;
        }
        
        .fee-amount {
          font-weight: 700;
          color: #e6a23c;
          font-size: 16px;
        }
      }
      
      .fee-meta {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: #909399;
        
        .fee-date {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        
        .fee-remark {
          color: #606266;
          font-style: italic;
        }
      }
    }
    
    .empty-fees {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
      color: #909399;
      gap: 12px;
      
      .el-icon {
        font-size: 48px;
        color: #c0c4cc;
      }
    }
  }
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #303133;
    
    .el-icon {
      font-size: 18px;
      color: #409eff;
    }
  }
  
  .detail-descriptions {
    :deep(.el-descriptions__label) {
      font-weight: 600;
      color: #606266;
      background: #f5f7fa;
    }
    
    :deep(.el-descriptions__content) {
      color: #303133;
    }
  }
  
  .highlight-price {
    font-weight: 700;
    color: #409eff;
    font-size: 16px;
  }
  
  .highlight-value {
    font-weight: 700;
    color: #67c23a;
    font-size: 16px;
  }
  
  .highlight-daily {
    font-weight: 600;
    color: #909399;
    font-size: 14px;
  }
  
  .highlight-additional {
    font-weight: 600;
    color: #e6a23c;
    font-size: 14px;
  }
  
  .inline-icon {
    margin-right: 6px;
    color: #909399;
  }
  
  .remark-content {
    white-space: pre-wrap;
    min-height: 60px;
    line-height: 1.8;
    color: #606266;
    padding: 12px;
    background: #fafafa;
    border-radius: 6px;
    border: 1px solid #e4e7ed;
  }
}

.form-scrollbar {
  flex: 1;
  padding: 24px;
  
  :deep(.el-scrollbar__wrap) {
    padding-right: 8px;
  }
}

.asset-form {
  .w-100 {
    width: 100%;
  }
  
  .cursor-pointer {
    cursor: pointer;
    transition: color 0.3s;
    
    &:hover {
      color: #409eff;
    }
  }
  
  :deep(.el-form-item) {
    margin-bottom: 22px;
    
    .el-form-item__label {
    font-weight: 500;
    color: #606266;
      word-break: break-word;
      white-space: normal;
      line-height: 1.5;
      padding-bottom: 4px;
    }
    
    .el-form-item__content {
      .el-input,
      .el-input-number,
      .el-date-picker,
      .el-textarea {
        width: 100%;
      }
      
      .el-input__wrapper {
    border-radius: 6px;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
  }
  
      .el-textarea__inner {
    border-radius: 6px;
    transition: all 0.3s;
        resize: vertical;
        min-height: 80px;
      }
      
      .el-input-number {
        width: 100%;
        
        .el-input__wrapper {
          width: 100%;
        }
      }
    }
  }
  
  :deep(.el-form-item__error) {
    padding-top: 4px;
    font-size: 12px;
  }
  
  .additional-fees-form {
    .fee-form-item {
      margin-bottom: 12px;
      padding: 12px;
      background: #fafafa;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      
      &:hover {
        border-color: #409eff;
      }
    }
    
    .add-fee-btn {
      margin-top: 8px;
    }
    
    .fee-total-info {
      margin-top: 12px;
      padding: 8px 12px;
      background: #f0f4ff;
      border-radius: 6px;
      font-weight: 600;
      color: #409eff;
      text-align: right;
    }
  }
}

.form-divider {
  margin: 32px 0 24px;
  
  :deep(.el-divider__text) {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #303133;
    background: white;
    padding: 0 16px;
    
    .el-icon {
      color: #409eff;
    }
  }
}

.drawer-footer {
  position: sticky;
  bottom: 0;
  padding: 20px 24px;
  background: white;
  border-top: 1px solid #e4e7ed;
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
  z-index: 10;
  
  .el-button {
    padding: 10px 24px;
    border-radius: 6px;
    font-weight: 500;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .asset-list-view {
    padding: 12px;
  }
  
  .asset-list-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .asset-list-filters {
    width: 100%;
    
    .filter-item,
    .el-button {
      flex: 1;
      min-width: auto;
    }
  }
  
  .detail-wrapper {
    padding: 16px;
    
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .drawer-footer {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}
</style>


