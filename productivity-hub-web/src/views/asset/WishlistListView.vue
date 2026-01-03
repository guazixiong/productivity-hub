<template>
  <div class="wishlist-list-view">
    <el-card class="main-card" shadow="hover">
      <template #header>
        <div class="wishlist-list-header">
          <div class="header-title">
            <el-icon class="title-icon"><Star /></el-icon>
            <span class="title-text">心愿单列表</span>
          </div>
          <div class="wishlist-list-filters">
            <el-select
              v-model="filters.achieved"
              placeholder="选择状态"
              clearable
              class="filter-item"
              @change="handleFilterChange"
            >
              <el-option label="未实现" :value="false" />
              <el-option label="已实现" :value="true" />
            </el-select>
            <el-button
              type="primary"
              :icon="Plus"
              class="header-action-btn"
              @click="handleCreate"
            >
              创建心愿单
            </el-button>
            <el-button
              type="danger"
              :icon="Delete"
              :disabled="selectedIds.length === 0"
              class="header-action-btn"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="wishlists"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="心愿名称" min-width="220">
          <template #default="{ row }">
            <span class="name-text">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" min-width="130">
          <template #default="{ row }">
            <span class="price-text">{{ formatCurrency(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="achieved" label="心愿状态" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.achieved ? 'success' : 'info'"
              size="default"
              effect="plain"
            >
              {{ row.achieved ? '已实现' : '未实现' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170">
          <template #default="{ row }">
            <span class="time-cell">
              {{ formatDate(row.createdAt, 'YYYY-MM-DD HH:mm:ss') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="achievedAt" label="实现时间" min-width="170">
          <template #default="{ row }">
            <span class="time-cell">
              {{ formatDate(row.achievedAt, 'YYYY-MM-DD HH:mm:ss') || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="间隔时间" min-width="120" align="center">
          <template #default="{ row }">
            <span class="time-cell">
              {{ calculateIntervalDays(row.createdAt, row.achievedAt) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.achieved && !row.syncedToAsset"
                type="success"
                link
                size="small"
                :icon="Box"
                @click="handleSyncToAsset(row)"
              >
                同步至资产
              </el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
                <el-button type="primary" link size="small" class="action-dropdown-btn">
                  <el-icon><MoreFilled /></el-icon>
                  操作
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">
                      <el-icon class="dropdown-icon"><View /></el-icon>
                      <span>查看</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="edit">
                      <el-icon class="dropdown-icon"><Edit /></el-icon>
                      <span>编辑</span>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!row.achieved" command="achieve">
                      <el-icon class="dropdown-icon"><Check /></el-icon>
                      <span>实现</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon class="dropdown-icon"><Delete /></el-icon>
                      <span style="color: var(--el-color-danger)">删除</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
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
        <div v-if="currentWishlist" class="detail-wrapper">
          <div class="detail-header">
            <div class="detail-title">
              <el-icon class="detail-icon"><Star /></el-icon>
              <span class="detail-name">{{ currentWishlist.name }}</span>
            </div>
            <el-tag
              :type="currentWishlist.achieved ? 'success' : 'info'"
              size="default"
              effect="plain"
              class="status-tag"
            >
              {{ currentWishlist.achieved ? '已实现' : '未实现' }}
            </el-tag>
          </div>

          <div class="detail-price">
            <span class="price-label">价格</span>
            <span class="price-value">{{ formatCurrency(currentWishlist.price) }}</span>
          </div>

          <el-descriptions :column="1" border class="detail-descriptions">
            <el-descriptions-item label="状态">
              <el-tag :type="currentWishlist.achieved ? 'success' : 'info'" effect="plain">
                {{ currentWishlist.achieved ? '已实现' : '未实现' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="实现时间">
              {{ formatDate(currentWishlist.achievedAt, 'YYYY-MM-DD HH:mm:ss') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDate(currentWishlist.createdAt, 'YYYY-MM-DD HH:mm:ss') || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="链接">
              <el-link
                v-if="currentWishlist.link"
                :href="currentWishlist.link"
                target="_blank"
                type="primary"
                :icon="Link"
                :underline="false"
                class="detail-link"
              >
                {{ currentWishlist.link }}
              </el-link>
              <span v-else class="empty-text">-</span>
            </el-descriptions-item>
            <el-descriptions-item label="备注">
              <div class="remark-content">
                {{ currentWishlist.remark || '无' }}
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="drawer-footer">
          <el-button
            type="primary"
            :icon="Edit"
            class="footer-btn"
            @click="handleEditFromDetail"
          >
            编辑
          </el-button>
          <el-button class="footer-btn" @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>

      <!-- 创建/编辑模式 -->
      <div v-else v-loading="formLoading" class="drawer-content">
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-width="100px"
          class="wishlist-form"
        >
          <el-form-item label="心愿名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入心愿名称"
              maxlength="100"
              show-word-limit
              clearable
            />
          </el-form-item>

          <el-form-item label="价格" prop="price">
            <el-input-number
              v-model="form.price"
              :min="0.01"
              :step="1"
              :precision="2"
              class="w-100"
              placeholder="请输入价格"
              controls-position="right"
            />
          </el-form-item>

          <el-form-item label="链接" prop="link">
            <el-input
              v-model="form.link"
              placeholder="请输入心愿链接（可选）"
              maxlength="500"
              show-word-limit
              clearable
            >
              <template #prefix>
                <el-icon><Link /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="form.remark"
              placeholder="请输入备注（可选）"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              resize="none"
            />
          </el-form-item>

          <el-form-item v-if="drawerMode === 'edit' && form.achieved" label="实现时间" prop="achievedAt">
            <el-date-picker
              v-model="form.achievedAt"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择实现时间"
              class="w-100"
            />
          </el-form-item>
        </el-form>
        <div class="drawer-footer">
          <el-button
            type="primary"
            class="footer-btn"
            @click="handleFormSubmit"
            :loading="submitting"
          >
            {{ drawerMode === 'edit' ? '更新' : '创建' }}
          </el-button>
          <el-button class="footer-btn" @click="drawerVisible = false">取消</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 资产创建抽屉：用于从心愿单同步到资产 -->
    <el-drawer
      v-model="assetDrawerVisible"
      title="创建资产（从心愿单同步）"
      :size="assetDrawerSize"
      direction="rtl"
      :before-close="handleAssetDrawerClose"
    >
      <div v-loading="assetFormLoading" class="drawer-content">
        <el-scrollbar class="form-scrollbar">
          <el-form
            ref="assetFormRef"
            :model="assetForm"
            :rules="assetFormRules"
            label-width="120px"
            class="asset-form"
          >
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="资产名称" prop="name">
                  <el-input 
                    v-model="assetForm.name" 
                    placeholder="请输入资产名称" 
                    :maxlength="100"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="分类" prop="categoryId">
                  <el-input
                    v-model="selectedAssetCategoryName"
                    placeholder="请选择分类"
                    readonly
                    class="w-100"
                    @click="showAssetCategoryDialog = true"
                  >
                    <template #suffix>
                      <el-icon class="cursor-pointer" @click="showAssetCategoryDialog = true">
                        <ArrowDown />
                      </el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="购买日期" prop="purchaseDate">
                  <el-date-picker
                    v-model="assetForm.purchaseDate"
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
                    v-model="assetForm.price"
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
                    v-model="assetForm.image"
                    placeholder="请输入图片链接（可选）"
                    :maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="备注" prop="remark">
                  <el-input
                    v-model="assetForm.remark"
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
                    <div v-for="(fee, index) in assetFormAdditionalFees" :key="index" class="fee-form-item">
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
                            @click="removeAssetAdditionalFee(index)"
                          />
                        </el-col>
                      </el-row>
                    </div>
                    <el-button
                      type="primary"
                      :icon="Plus"
                      size="small"
                      plain
                      @click="addAssetAdditionalFee"
                      class="add-fee-btn"
                    >
                      添加附加费用
                    </el-button>
                    <div v-if="assetFormAdditionalFees.length > 0" class="fee-total-info">
                      合计: {{ formatCurrency(calculateAssetAdditionalFeesTotal(assetFormAdditionalFees)) }}
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
                  <el-switch v-model="assetForm.warrantyEnabled" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="保修截止日期" prop="warrantyEndDate">
                  <el-date-picker
                    v-model="assetForm.warrantyEndDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="请选择保修截止日期"
                    class="w-100"
                    :disabled="!assetForm.warrantyEnabled"
                  />
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="按使用次数贬值" prop="depreciationByUsageCount">
                  <el-switch v-model="assetForm.depreciationByUsageCount" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="预计使用次数" prop="expectedUsageCount">
                  <el-input-number
                    v-model="assetForm.expectedUsageCount"
                    :min="1"
                    :disabled="!assetForm.depreciationByUsageCount"
                  />
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="按使用日期贬值" prop="depreciationByUsageDate">
                  <el-switch v-model="assetForm.depreciationByUsageDate" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="使用日期" prop="usageDate">
                  <el-date-picker
                    v-model="assetForm.usageDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    placeholder="请选择使用日期"
                    class="w-100"
                    :disabled="!assetForm.depreciationByUsageDate"
                  />
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="是否在服役" prop="inService">
                  <el-switch v-model="assetForm.inService" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="退役日期" prop="retiredDate">
                  <el-date-picker
                    v-model="assetForm.retiredDate"
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
          <el-button type="primary" :icon="Plus" @click="handleAssetFormSubmit" :loading="assetSubmitting">
            创建资产
          </el-button>
          <el-button :icon="Close" @click="assetDrawerVisible = false">取消</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 分类选择弹窗（资产创建用） -->
    <CategoryPickerDialog
      v-model="showAssetCategoryDialog"
      :current-category-id="assetForm.categoryId"
      @confirm="handleAssetCategorySelect"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { View, Edit, Check, Delete, Plus, Star, Link, MoreFilled, Box, ArrowDown, Close, Setting, Remove } from '@element-plus/icons-vue'
import { wishlistApi } from '@/services/wishlistApi'
import { assetApi, assetAdditionalFeeApi } from '@/services/assetApi'
import type { Wishlist, WishlistCreateDTO, WishlistUpdateDTO } from '@/types/wishlist'
import type { AssetCreateDTO, AssetAdditionalFee, AssetAdditionalFeeCreateDTO } from '@/types/asset'
import { formatCurrency, formatDate } from '@/utils/format'
import { useRouter } from 'vue-router'
import CategoryPickerDialog from '@/components/asset/CategoryPickerDialog.vue'
import { useDevice } from '@/composables/useDevice'

const router = useRouter()
const { isMobile } = useDevice()
const loading = ref(false)
const wishlists = ref<Wishlist[]>([])
const selectedIds = ref<string[]>([])
const isInitialized = ref(false)

const filters = reactive<{
  achieved?: boolean
}>({
  achieved: undefined,
})


// 抽屉相关状态
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit' | 'view'>('create')
const drawerTitle = ref('')
const drawerSize = ref('600px')
const currentWishlist = ref<Wishlist | null>(null)
const detailLoading = ref(false)
const formLoading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<WishlistCreateDTO & { id?: string; achieved?: boolean; achievedAt?: string }>({
  name: '',
  price: 0,
  link: '',
  remark: '',
  achieved: false,
  achievedAt: '',
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入心愿名称', trigger: 'blur' },
    { min: 1, max: 100, message: '心愿名称长度必须在1-100个字符之间', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' },
  ],
  link: [
    { max: 500, message: '链接长度不能超过500个字符', trigger: 'blur' },
  ],
  remark: [
    { max: 500, message: '备注长度不能超过500个字符', trigger: 'blur' },
  ],
}

const loadWishlists = async () => {
  loading.value = true
  try {
    const res = await wishlistApi.getWishlistList({
      achieved: filters.achieved,
    })
    wishlists.value = res || []
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  loadWishlists()
}

const handleSelectionChange = (rows: Wishlist[]) => {
  selectedIds.value = rows.map((row) => row.id)
}

const handleCreate = () => {
  drawerMode.value = 'create'
  drawerTitle.value = '创建心愿单'
  drawerSize.value = '600px'
  resetForm()
  drawerVisible.value = true
}

const handleCommand = (command: string, row: Wishlist) => {
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'edit':
      handleEdit(row)
      break
    case 'achieve':
      handleAchieve(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleView = async (row: Wishlist) => {
  drawerMode.value = 'view'
  drawerTitle.value = '心愿单详情'
  drawerSize.value = '600px'
  detailLoading.value = true
  drawerVisible.value = true
  
  try {
    const data = await wishlistApi.getWishlistById(row.id)
    currentWishlist.value = data
  } catch (error) {
    ElMessage.error('加载心愿单详情失败')
    drawerVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const handleEdit = async (row: Wishlist) => {
  drawerMode.value = 'edit'
  drawerTitle.value = '编辑心愿单'
  drawerSize.value = '600px'
  formLoading.value = true
  drawerVisible.value = true
  
  try {
    const data = await wishlistApi.getWishlistById(row.id)
    if (data) {
      form.id = data.id
      form.name = data.name
      form.price = data.price
      form.link = data.link || ''
      form.remark = data.remark || ''
      form.achieved = data.achieved || false
      // 如果已实现且有实现时间，格式化日期时间
      if (data.achieved && data.achievedAt) {
        const date = new Date(data.achievedAt)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        form.achievedAt = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      } else {
        form.achievedAt = ''
      }
    }
  } catch (error) {
    ElMessage.error('加载心愿单失败')
    drawerVisible.value = false
  } finally {
    formLoading.value = false
  }
}

const handleEditFromDetail = async () => {
  if (!currentWishlist.value) return
  await handleEdit(currentWishlist.value)
}

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.price = 0
  form.link = ''
  form.remark = ''
  form.achieved = false
  form.achievedAt = ''
  formRef.value?.clearValidate()
}

const handleDrawerClose = (done: () => void) => {
  resetForm()
  currentWishlist.value = null
  done()
}

const handleFormSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (drawerMode.value === 'edit') {
        const updateData: WishlistUpdateDTO = {
          id: form.id!,
          name: form.name,
          price: form.price,
          link: form.link || undefined,
          remark: form.remark || undefined,
          achieved: form.achieved,
          achievedAt: form.achievedAt || undefined,
        }
        await wishlistApi.updateWishlist(updateData)
        ElMessage.success('更新成功')
        
        // 如果当前正在查看详情，且是同一个心愿单，重新加载详情数据
        if (currentWishlist.value && currentWishlist.value.id === form.id) {
          try {
            const updatedData = await wishlistApi.getWishlistById(form.id!)
            currentWishlist.value = updatedData
            // 如果是从详情页面进入编辑的，切换回详情模式
            drawerMode.value = 'view'
            drawerTitle.value = '心愿单详情'
            return // 不关闭抽屉，保持在详情页面
          } catch (error) {
            console.error('重新加载详情失败:', error)
          }
        }
      } else {
        const createData: WishlistCreateDTO = {
          name: form.name,
          price: form.price,
          link: form.link || undefined,
          remark: form.remark || undefined,
        }
        await wishlistApi.createWishlist(createData)
        ElMessage.success('创建成功')
      }
      drawerVisible.value = false
      loadWishlists()
    } catch (error) {
      ElMessage.error(drawerMode.value === 'edit' ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleAchieve = async (row: Wishlist) => {
  await ElMessageBox.confirm('确定要将该心愿单标记为已实现吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  })
  await wishlistApi.updateWishlist({
    id: row.id,
    name: row.name,
    price: row.price,
    link: row.link,
    remark: row.remark,
    achieved: true,
  })
  ElMessage.success('标记成功')
  loadWishlists()
}

const handleDelete = async (row: Wishlist) => {
  await ElMessageBox.confirm('确定要删除该心愿单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await wishlistApi.deleteWishlist(row.id)
  ElMessage.success('删除成功')
  loadWishlists()
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) return
  await ElMessageBox.confirm(
    `确定要删除选中的 ${selectedIds.value.length} 个心愿单吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )
  await Promise.all(selectedIds.value.map((id) => wishlistApi.deleteWishlist(id)))
  ElMessage.success('批量删除成功')
  selectedIds.value = []
  loadWishlists()
}

// 计算间隔时间（天）
const calculateIntervalDays = (createdAt?: string, achievedAt?: string): string => {
  if (!createdAt || !achievedAt) {
    return '-'
  }
  const created = new Date(createdAt)
  const achieved = new Date(achievedAt)
  if (isNaN(created.getTime()) || isNaN(achieved.getTime())) {
    return '-'
  }
  const diffTime = achieved.getTime() - created.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  return `${diffDays} 天`
}

// 资产创建抽屉相关状态
const assetDrawerVisible = ref(false)
const assetDrawerSize = computed(() => (isMobile.value ? '100%' : '60%'))
const assetFormLoading = ref(false)
const assetSubmitting = ref(false)
const assetFormRef = ref<FormInstance>()
const showAssetCategoryDialog = ref(false)
const selectedAssetCategoryName = ref('')
const currentSyncWishlistId = ref<string | null>(null)
const assetFormAdditionalFees = ref<AssetAdditionalFee[]>([])

const assetForm = reactive<AssetCreateDTO & { additionalFees?: number }>({
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

const assetFormRules: FormRules = {
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  purchaseDate: [{ required: true, message: '请选择购买日期', trigger: 'change' }],
}

// 计算附加费用合计
const calculateAssetAdditionalFeesTotal = (fees: AssetAdditionalFee[]): number => {
  if (!fees || fees.length === 0) return 0
  return fees.reduce((sum, fee) => sum + (fee.amount || 0), 0)
}

// 添加附加费用项
const addAssetAdditionalFee = () => {
  assetFormAdditionalFees.value.push({
    name: '',
    amount: 0,
    feeDate: '',
    remark: '',
  })
}

// 删除附加费用项
const removeAssetAdditionalFee = (index: number) => {
  assetFormAdditionalFees.value.splice(index, 1)
}

// 处理分类选择
const handleAssetCategorySelect = (category: { id: string; name: string } | null) => {
  if (category) {
    assetForm.categoryId = category.id
    selectedAssetCategoryName.value = category.name
  } else {
    assetForm.categoryId = ''
    selectedAssetCategoryName.value = ''
  }
  showAssetCategoryDialog.value = false
}

// 重置资产表单
const resetAssetForm = () => {
  Object.assign(assetForm, {
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
  selectedAssetCategoryName.value = ''
  assetFormAdditionalFees.value = []
  currentSyncWishlistId.value = null
  assetFormRef.value?.resetFields()
}

// 处理资产抽屉关闭
const handleAssetDrawerClose = (done: () => void) => {
  resetAssetForm()
  done()
}

// 同步至资产
const handleSyncToAsset = async (row: Wishlist) => {
  if (row.syncedToAsset) {
    ElMessage.warning('该心愿已同步至资产，不能重复同步')
    return
  }
  
  // 打开资产创建抽屉并同步字段
  resetAssetForm()
  currentSyncWishlistId.value = row.id
  
  // 同步心愿单字段到资产表单
  assetForm.name = row.name
  assetForm.price = row.price
  if (row.remark) {
    assetForm.remark = row.remark
  }
  
  // 设置购买日期为今天
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  assetForm.purchaseDate = `${year}-${month}-${day}`
  
  assetDrawerVisible.value = true
}

// 提交资产创建表单
const handleAssetFormSubmit = async () => {
  if (!assetFormRef.value) return

  await assetFormRef.value.validate(async (valid) => {
    if (!valid) return
    assetSubmitting.value = true
    try {
      const payload: AssetCreateDTO = {
        ...assetForm,
      }
      const result = await assetApi.createAsset(payload)
      
      // 保存附加费用
      if (result.id && assetFormAdditionalFees.value.length > 0) {
        const feePromises = assetFormAdditionalFees.value
          .filter(fee => fee.name && fee.amount > 0)
          .map(fee => {
            const feePayload: AssetAdditionalFeeCreateDTO = {
              assetId: result.id,
              name: fee.name,
              amount: fee.amount,
              feeDate: fee.feeDate || assetForm.purchaseDate,
              remark: fee.remark,
            }
            return assetAdditionalFeeApi.createAssetAdditionalFee(feePayload)
          })
        await Promise.all(feePromises)
      }
      
      ElMessage.success('创建成功')
      
      // 如果是从心愿单同步过来的，更新心愿单的同步状态
      if (currentSyncWishlistId.value) {
        try {
          // 获取心愿单信息
          const wishlistData = await wishlistApi.getWishlistById(currentSyncWishlistId.value)
          if (wishlistData) {
            // 更新心愿单，标记为已同步
            await wishlistApi.updateWishlist({
              id: wishlistData.id,
              name: wishlistData.name,
              price: wishlistData.price,
              link: wishlistData.link,
              remark: wishlistData.remark,
              achieved: wishlistData.achieved,
              achievedAt: wishlistData.achievedAt,
            })
            // 注意：syncedToAsset字段应该由后端自动更新，这里只是确保心愿单信息被更新
          }
        } catch (error) {
          // 忽略更新心愿单状态的错误，不影响资产创建
          console.error('更新心愿单同步状态失败:', error)
        }
      }
      
      assetDrawerVisible.value = false
      loadWishlists() // 刷新心愿单列表以更新同步状态
    } catch (error) {
      ElMessage.error('创建失败')
    } finally {
      assetSubmitting.value = false
    }
  })
}

onMounted(() => {
  loadWishlists()
  isInitialized.value = true
})

// 当页面被激活时（从其他页面返回时）刷新列表
onActivated(() => {
  // 只在已经初始化后才刷新，避免首次加载时重复请求
  if (isInitialized.value) {
    loadWishlists()
  }
})
</script>

<style scoped lang="scss">
.wishlist-list-view {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.main-card {
  border-radius: 12px;
  overflow: hidden;
}

.wishlist-list-header {
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

.wishlist-list-filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-item {
  min-width: 140px;
}

.filter-item :deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.header-action-btn {
  border-radius: 10px;
  padding: 8px 16px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-dropdown-btn {
  padding: 4px 8px;
  font-weight: 400;
  font-size: 13px;
}

.action-dropdown-btn .el-icon {
  margin-right: 4px;
}

.dropdown-icon {
  margin-right: 8px;
}

/* 抽屉样式 */
.drawer-content {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--ph-bg-body, #f1f5f9);
}

/* 详情页样式 */
.detail-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: var(--ph-bg-elevated, rgba(255, 255, 255, 0.92));
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.detail-icon {
  font-size: 24px;
  color: var(--text-secondary, #334155);
}

.detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.status-tag {
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: 400;
}

.detail-price {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 20px;
  background: var(--ph-bg-elevated, rgba(255, 255, 255, 0.92));
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
}

.price-label {
  font-size: 14px;
  color: var(--text-tertiary, #64748b);
  font-weight: 400;
}

.price-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--primary-color, #2563eb);
}

.detail-descriptions {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.detail-descriptions :deep(.el-descriptions__label) {
  font-weight: 500;
  color: var(--text-secondary, #334155);
  width: 100px;
  background-color: var(--ph-bg-elevated, rgba(255, 255, 255, 0.92));
}

.detail-descriptions :deep(.el-descriptions__content) {
  color: var(--text-primary, #0f172a);
  font-weight: 400;
}

.detail-descriptions :deep(.el-descriptions__table) {
  border-radius: 12px;
}

.empty-text {
  color: var(--text-disabled, #cbd5f5);
  font-style: normal;
}

.name-text {
  font-weight: 400;
  color: var(--text-primary, #0f172a);
  font-size: 14px;
}

.price-text {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.link-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 100%;
  transition: all 0.2s ease;
  padding: 2px 4px;
  border-radius: 4px;
}

.link-text {
  font-size: 13px;
  font-weight: 400;
}

.time-cell {
  font-size: 13px;
  color: var(--text-tertiary, #64748b);
  font-weight: 400;
}

.detail-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: var(--ph-bg-elevated, rgba(255, 255, 255, 0.92));
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 400;
}

.detail-link:hover {
  background: var(--primary-light, rgba(37, 99, 235, 0.16));
}

.remark-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  color: var(--text-secondary, #334155);
  padding: 12px;
  background: var(--ph-bg-elevated, rgba(255, 255, 255, 0.92));
  border-radius: 8px;
  min-height: 60px;
  border: 1px solid var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
  font-size: 13px;
}

/* 表单样式 */
.wishlist-form {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.wishlist-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.wishlist-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary, #334155);
  font-size: 14px;
  padding-bottom: 6px;
}

.wishlist-form :deep(.el-input__wrapper),
.wishlist-form :deep(.el-textarea__inner) {
  border-radius: 6px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
}

.wishlist-form :deep(.el-input__wrapper:hover),
.wishlist-form :deep(.el-textarea__inner:hover) {
  border-color: var(--ph-border-strong, rgba(100, 116, 139, 0.9));
}

.wishlist-form :deep(.el-input__wrapper.is-focus),
.wishlist-form :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color, #2563eb);
}

.wishlist-form :deep(.el-input-number) {
  width: 100%;
}

.wishlist-form :deep(.el-input-number__decrease),
.wishlist-form :deep(.el-input-number__increase) {
  border-radius: 0 4px 4px 0;
}

.drawer-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
  background: var(--surface-color, rgba(255, 255, 255, 0.86));
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.drawer-footer .footer-btn {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 80px;
}

.w-100 {
  width: 100%;
}


/* 标签样式 */
:deep(.el-tag--success.is-plain) {
  background-color: rgba(22, 163, 74, 0.1);
  border-color: rgba(22, 163, 74, 0.3);
  color: var(--accent-success, #16a34a);
  font-weight: 400;
}

:deep(.el-tag--info.is-plain) {
  background-color: var(--ph-bg-elevated, rgba(255, 255, 255, 0.92));
  border-color: var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
  color: var(--text-tertiary, #64748b);
  font-weight: 400;
}

/* 抽屉标题样式 */
:deep(.el-drawer__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--ph-border-subtle, rgba(148, 163, 184, 0.4));
  background: var(--surface-color, rgba(255, 255, 255, 0.86));
  margin-bottom: 0;
}

:deep(.el-drawer__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary, #0f172a);
}

:deep(.el-drawer__body) {
  padding: 0;
}

/* 资产创建表单样式 */
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
</style>

