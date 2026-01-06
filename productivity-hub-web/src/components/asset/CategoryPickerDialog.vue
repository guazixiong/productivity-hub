<template>
  <el-dialog
    v-model="visible"
    title="选择资产分类"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="category-picker-dialog">
      <el-empty
        v-if="!loading && categories.length === 0"
        description="暂无分类数据"
        :image-size="100"
      />

      <div v-else class="category-tree">
        <div
          v-for="parent in categories"
          :key="parent.id"
          class="category-group"
        >
          <!-- 大分类 -->
          <div
            class="category-item parent-category"
            :class="{ active: selectedId === parent.id }"
            @click="handleSelect(parent)"
          >
            <div class="category-content">
              <el-icon v-if="getIconComponent(parent.icon)" :size="20" class="category-icon">
                <component :is="getIconComponent(parent.icon)" />
              </el-icon>
              <span v-else class="no-icon-placeholder">📁</span>
              <span class="category-name">{{ parent.name }}</span>
              <el-tag v-if="parent.isDefault" type="success" size="small" class="ml-8">
                默认
              </el-tag>
            </div>
            <el-icon v-if="selectedId === parent.id" class="check-icon">
              <Check />
            </el-icon>
          </div>

          <!-- 小分类 -->
          <div
            v-for="child in parent.children || []"
            :key="child.id"
            class="category-item child-category"
            :class="{ active: selectedId === child.id }"
            @click="handleSelect(child)"
          >
            <div class="category-content">
              <span class="child-indent">└─</span>
              <el-icon v-if="getIconComponent(child.icon)" :size="18" class="category-icon">
                <component :is="getIconComponent(child.icon)" />
              </el-icon>
              <span v-else class="no-icon-placeholder">📄</span>
              <span class="category-name">{{ child.name }}</span>
              <el-tag v-if="child.isDefault" type="success" size="small" class="ml-8">
                默认
              </el-tag>
            </div>
            <el-icon v-if="selectedId === child.id" class="check-icon">
              <Check />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedCategory">
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Check } from '@element-plus/icons-vue'
import type { AssetCategory } from '@/types/asset'
import { getIconComponent } from '@/utils/iconMapper'
import { useAssetCategory } from '@/composables/useAssetCategory'

const props = defineProps<{
  modelValue: boolean
  currentCategoryId?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: [category: AssetCategory]
}>()

const { getSelectableCategories } = useAssetCategory()

const visible = ref(false)
const categories = ref<AssetCategory[]>([])
const selectedId = ref<string>('')
const selectedCategory = ref<AssetCategory | null>(null)

// 监听 modelValue 变化
watch(
  () => props.modelValue,
  (newVal) => {
    visible.value = newVal
    if (newVal) {
      loadCategories()
      selectedId.value = props.currentCategoryId || ''
    }
  },
  { immediate: true }
)

// 监听 visible 变化，同步到 modelValue
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const loading = computed(() => categories.value.length === 0 && visible.value)

const loadCategories = async () => {
  try {
    const res = await getSelectableCategories()
    categories.value = res || []
  } catch (error) {
    categories.value = []
  }
}

const handleSelect = (category: AssetCategory) => {
  selectedId.value = category.id
  selectedCategory.value = category
}

const handleConfirm = () => {
  if (selectedCategory.value) {
    emit('confirm', selectedCategory.value)
    handleClose()
  }
}

const handleClose = () => {
  visible.value = false
  selectedId.value = ''
  selectedCategory.value = null
}
</script>

<style scoped lang="scss">
.category-picker-dialog {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;

  .category-tree {
    .category-group {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .category-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      margin-bottom: 8px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
        border-color: #409eff;
      }

      &.active {
        background-color: #ecf5ff;
        border-color: #409eff;
        color: #409eff;
      }

      .category-content {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;

        .child-indent {
          color: #409eff;
          margin-right: 8px;
          font-weight: 600;
          font-size: 14px;
        }

        .category-icon {
          color: inherit;
        }

        .no-icon-placeholder {
          font-size: 18px;
        }

        .category-name {
          font-size: 14px;
          font-weight: 500;
        }

        .ml-8 {
          margin-left: 8px;
        }
      }

      .check-icon {
        color: #409eff;
        font-size: 20px;
      }

      &.parent-category {
        font-weight: 600;
        background: linear-gradient(90deg, #f0f9ff 0%, #fafafa 100%);
        font-size: 15px;
        border-left: 4px solid #67c23a;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

        &:hover {
          background: linear-gradient(90deg, #e0f2fe 0%, #f5f7fa 100%);
          border-left-color: #52c41a;
        }

        &.active {
          background: linear-gradient(90deg, #ecf5ff 0%, #e6f7ff 100%);
          border-left-color: #409eff;
          box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
        }
      }

      &.child-category {
        margin-left: 40px;
        font-weight: 400;
        background-color: #f8f9fa;
        border-left: 3px solid #409eff;
        padding-left: 24px;
        position: relative;
        
        &::before {
          content: '';
          position: absolute;
          left: -3px;
          top: 0;
          bottom: 0;
          width: 3px;
          background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
        }
        
        &:hover {
          background-color: #f0f4ff;
          border-left-color: #66b1ff;
        }
        
        &.active {
          background-color: #ecf5ff;
          border-left-color: #409eff;
          box-shadow: 0 2px 4px rgba(64, 158, 255, 0.15);
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

