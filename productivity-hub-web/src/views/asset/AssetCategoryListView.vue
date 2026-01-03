<template>
  <div class="asset-category-list-view">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="title">资产分类管理</span>
          <div class="header-actions" v-if="isAdmin">
            <el-button type="primary" @click="handleCreateParent">
              <el-icon><Plus /></el-icon>
              添加大分类
            </el-button>
          </div>
        </div>
      </template>

      <!-- 分类列表 -->
      <div class="category-list-container">
        <el-empty
          v-if="!loading && categoryList.length === 0"
          description="暂无分类"
          :image-size="200"
        >
          <el-button v-if="isAdmin" type="primary" @click="handleCreateParent">
            添加大分类
          </el-button>
        </el-empty>

        <el-table
          v-else
          v-loading="loading"
          :data="categoryList"
          border
          stripe
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          style="width: 100%"
        >
          <el-table-column prop="name" label="分类名称" min-width="250">
            <template #default="{ row }">
              <div class="category-name-cell" :class="{ 'child-category-row': row.level === 2, 'parent-category-row': row.level === 1 }">
                <el-icon v-if="getIconComponent(row.icon)" :size="20" class="category-icon">
                  <component :is="getIconComponent(row.icon)" />
                </el-icon>
                <span v-else class="no-icon-placeholder">📁</span>
                <span class="category-name" :class="{ 'child-name': row.level === 2, 'parent-name': row.level === 1 }">
                  {{ row.name }}
                </span>
                <el-tag v-if="row.level === 1" type="primary" size="small" class="level-tag">大分类</el-tag>
                <el-tag v-else type="success" size="small" class="level-tag">小分类</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="assetCount" label="资产数量" width="140" align="center">
            <template #default="{ row }">
              <div class="asset-count-cell">
                <el-tag 
                  :type="(row.assetCount || 0) > 0 ? 'warning' : 'info'" 
                  size="small"
                  :effect="(row.assetCount || 0) > 0 ? 'dark' : 'plain'"
                >
                  {{ row.assetCount || 0 }}
                </el-tag>
                <span v-if="row.level === 1 && row.children && row.children.length > 0" class="total-count-hint">
                  (含子分类: {{ getTotalAssetCount(row) }})
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
          <el-table-column label="是否默认" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column v-if="isAdmin" label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.level === 1"
                type="success"
                link
                size="small"
                @click="handleCreateChild(row)"
              >
                <el-icon><Plus /></el-icon>
                添加小分类
              </el-button>
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleDelete(row)"
                :disabled="row.isDefault || getTotalAssetCount(row) > 0 || (row.level === 1 && row.children && row.children.length > 0)"
                :title="getDeleteButtonTooltip(row)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类级别" v-if="!isEdit">
          <el-radio-group v-model="categoryLevel" @change="handleLevelChange">
            <el-radio :label="1">大分类</el-radio>
            <el-radio :label="2">小分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="父分类"
          prop="parentId"
          v-if="categoryLevel === 2 || (isEdit && (formData.parentId !== undefined || categoryLevel === 2))"
        >
          <el-select
            v-model="formData.parentId"
            placeholder="请选择父分类（小分类必选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="parent in parentCategories"
              :key="parent.id"
              :label="parent.name"
              :value="parent.id"
            />
          </el-select>
          <div class="form-tip" v-if="categoryLevel === 2">小分类必须选择父分类</div>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入分类名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="分类图标" prop="icon">
          <div class="icon-selector-wrapper">
            <div class="icon-input-wrapper" @click="showIconPicker = true">
              <el-input
                v-model="formData.icon"
                placeholder="请选择图标（可选）"
                readonly
                class="icon-input"
              >
                <template #prefix>
                  <el-icon v-if="getIconComponent(formData.icon)" :size="18">
                    <component :is="getIconComponent(formData.icon)" />
                  </el-icon>
                </template>
                <template #suffix>
                  <el-icon class="icon-selector-trigger"><Search /></el-icon>
                </template>
              </el-input>
            </div>
            <el-button
              v-if="formData.icon"
              type="danger"
              link
              size="small"
              @click.stop="formData.icon = ''"
              class="clear-icon-btn"
            >
              清除
            </el-button>
          </div>
          <div class="form-tip">点击输入框选择图标</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器对话框 -->
    <el-dialog
      v-model="showIconPicker"
      title="选择图标"
      width="600px"
    >
      <IconPicker v-model="formData.icon" @icon-selected="showIconPicker = false" />
      <template #footer>
        <el-button @click="showIconPicker = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import { assetCategoryApi } from '@/services/assetApi'
import type { AssetCategory, AssetCategoryCreateDTO, AssetCategoryUpdateDTO } from '@/types/asset'
import type { FormInstance, FormRules } from 'element-plus'
import { getIconComponent } from '@/utils/iconMapper'
import IconPicker from '@/components/asset/IconPicker.vue'
import { useAuthStore } from '@/stores/auth'
import { useAssetCategory } from '@/composables/useAssetCategory'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.user?.roles?.includes('admin') ?? false)
const { clearCache } = useAssetCategory()

const loading = ref(false)
const categoryList = ref<AssetCategory[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const showIconPicker = ref(false)
const categoryLevel = ref<1 | 2>(1)

const formData = ref<AssetCategoryCreateDTO | AssetCategoryUpdateDTO>({
  name: '',
  icon: '',
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度必须在1-50个字符之间', trigger: 'blur' },
  ],
  parentId: [
    { required: true, message: '请选择父分类', trigger: 'change' },
  ],
}

const dialogTitle = computed(() => {
  if (isEdit.value) {
    return '编辑分类'
  }
  return categoryLevel.value === 1 ? '添加大分类' : '添加小分类'
})

// 获取所有大分类（用于小分类的父分类选择）
const parentCategories = computed(() => {
  const flatten = (categories: AssetCategory[]): AssetCategory[] => {
    const result: AssetCategory[] = []
    categories.forEach(cat => {
      if (cat.level === 1) {
        result.push(cat)
      }
    })
    return result
  }
  return flatten(categoryList.value)
})

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 加载分类列表
const loadCategories = async () => {
  loading.value = true
  try {
    const data = await assetCategoryApi.getAllCategories()
    categoryList.value = data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载分类列表失败')
  } finally {
    loading.value = false
  }
}

// 创建大分类
const handleCreateParent = () => {
  isEdit.value = false
  categoryLevel.value = 1
  formData.value = {
    name: '',
    icon: '',
  }
  dialogVisible.value = true
}

// 创建小分类
const handleCreateChild = (parent: AssetCategory) => {
  isEdit.value = false
  categoryLevel.value = 2
  formData.value = {
    name: '',
    icon: '',
    parentId: parent.id,
  }
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row: AssetCategory) => {
  isEdit.value = true
  categoryLevel.value = (row.level || 1) as 1 | 2
  formData.value = {
    id: row.id,
    name: row.name,
    icon: row.icon || '',
    parentId: row.parentId || undefined,
  }
  dialogVisible.value = true
}

// 计算分类及其子分类的总资产数量
const getTotalAssetCount = (category: AssetCategory): number => {
  let total = category.assetCount || 0
  if (category.children && category.children.length > 0) {
    total += category.children.reduce((sum, child) => sum + (child.assetCount || 0), 0)
  }
  return total
}

// 获取删除按钮的提示信息
const getDeleteButtonTooltip = (row: AssetCategory): string => {
  if (row.isDefault) {
    return '默认分类不能删除'
  }
  const totalAssetCount = getTotalAssetCount(row)
  if (totalAssetCount > 0) {
    return `该分类${row.level === 1 && row.children && row.children.length > 0 ? '及其子分类' : ''}下存在 ${totalAssetCount} 个资产，无法删除`
  }
  if (row.level === 1 && row.children && row.children.length > 0) {
    return '该分类下存在子分类，无法删除'
  }
  return '删除分类'
}

// 删除分类
const handleDelete = async (row: AssetCategory) => {
  if (row.isDefault) {
    ElMessage.warning('默认分类不能删除')
    return
  }

  // 检查是否有资产关联（包括子分类的资产）
  const totalAssetCount = getTotalAssetCount(row)
  if (totalAssetCount > 0) {
    const hasChildren = row.children && row.children.length > 0
    const message = hasChildren
      ? `该分类及其子分类下共存在 ${totalAssetCount} 个资产，无法删除。请先删除或转移这些资产后再删除分类。`
      : `该分类下存在 ${totalAssetCount} 个资产，无法删除。请先删除或转移这些资产后再删除分类。`
    ElMessage.warning(message)
    return
  }

  // 如果是大分类，检查是否有子分类
  if (row.level === 1 && row.children && row.children.length > 0) {
    ElMessage.warning('该分类下存在子分类，无法删除。请先删除所有子分类后再删除该分类。')
    return
  }

  const message = `确定要删除分类"${row.name}"吗？此操作不可恢复。`

  try {
    await ElMessageBox.confirm(message, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await assetCategoryApi.deleteCategory(row.id)
    ElMessage.success('删除成功')
    clearCache() // 清除缓存
    loadCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 后端也会进行验证，显示后端返回的错误信息
      const errorMessage = error.message || '删除失败'
      if (errorMessage.includes('资产') || errorMessage.includes('子分类')) {
        ElMessage.warning(errorMessage)
      } else {
        ElMessage.error(errorMessage)
      }
    }
  }
}

// 分类级别改变
const handleLevelChange = (level: 1 | 2) => {
  if (level === 1) {
    formData.value.parentId = undefined
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  // 如果是小分类，验证父分类
  if (categoryLevel.value === 2 && !formData.value.parentId) {
    ElMessage.warning('请选择父分类')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await assetCategoryApi.updateCategory(formData.value as AssetCategoryUpdateDTO)
        ElMessage.success('更新成功')
      } else {
        // 如果是大分类，清除 parentId
        if (categoryLevel.value === 1) {
          formData.value.parentId = undefined
        }
        await assetCategoryApi.createCategory(formData.value as AssetCategoryCreateDTO)
        ElMessage.success('创建成功')
      }
      clearCache() // 清除缓存
      dialogVisible.value = false
      loadCategories()
    } catch (error: any) {
      ElMessage.error(error.message || (isEdit.value ? '更新失败' : '创建失败'))
    } finally {
      submitting.value = false
    }
  })
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.value = {
    name: '',
    icon: '',
  }
  categoryLevel.value = 1
}

onMounted(() => {
  if (isAdmin.value) {
    loadCategories()
  }
})
</script>

<style scoped lang="scss">
.asset-category-list-view {
  .main-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: 18px;
        font-weight: 600;
      }

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .category-list-container {
    .category-name-cell {
      display: flex;
      align-items: center;
      gap: 10px;
      min-height: 40px;

      .category-icon {
        flex-shrink: 0;
      }

      .no-icon-placeholder {
        font-size: 20px;
        flex-shrink: 0;
      }

      .category-name {
        flex: 1;
        font-size: 14px;
        line-height: 1.5;
      }

      .level-tag {
        margin-left: auto;
        flex-shrink: 0;
      }

      &.parent-category-row {
        .category-icon {
          color: #67c23a;
        }
        
        .category-name {
          font-weight: 600;
          color: #303133;
          font-size: 15px;
        }
      }

      &.child-category-row {
        padding-left: 40px;
        position: relative;
        background-color: #fafbfc;
        border-left: 3px solid #409eff;
        margin-left: 8px;
        
        &::before {
          content: '└─';
          position: absolute;
          left: 16px;
          color: #409eff;
          font-weight: 700;
          font-size: 16px;
        }
        
        .category-name {
          font-weight: 400;
          color: #606266;
        }
        
        .category-icon {
          color: #409eff;
        }
      }
    }

    .asset-count-cell {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .total-count-hint {
        font-size: 11px;
        color: #909399;
        white-space: nowrap;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
  }

  .icon-selector-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;

    .icon-input-wrapper {
      flex: 1;
      cursor: pointer;

      .icon-input {
        cursor: pointer;
      }
    }

    .icon-selector-trigger {
      cursor: pointer;
    }

    .clear-icon-btn {
      flex-shrink: 0;
    }
  }
}
</style>
