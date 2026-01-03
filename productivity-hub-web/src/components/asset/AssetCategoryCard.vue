<template>
  <div class="asset-category-card">
    <el-card shadow="hover" class="category-card">
      <div class="category-content">
        <div class="category-icon">
          <el-icon v-if="getIconComponent(category.icon)" :size="32">
            <component :is="getIconComponent(category.icon)" />
          </el-icon>
          <el-icon v-else :size="32">
            <Folder />
          </el-icon>
        </div>
        <div class="category-info">
          <div class="category-name">
            {{ category.name }}
            <el-tag v-if="category.isDefault" type="success" size="small" class="ml-8">
              默认
            </el-tag>
          </div>
          <div class="category-count">资产数量：{{ category.assetCount || 0 }}</div>
          <div v-if="category.sortOrder !== undefined" class="category-sort">
            排序：{{ category.sortOrder }}
          </div>
        </div>
        <div class="category-actions">
          <el-button size="small" @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            size="small"
            type="danger"
            :disabled="category.isDefault"
            @click="handleDelete"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Folder, Edit, Delete } from '@element-plus/icons-vue'
import type { AssetCategory } from '@/types/asset'
import { getIconComponent } from '@/utils/iconMapper'

const props = defineProps<{
  category: AssetCategory
}>()

const emit = defineEmits<{
  edit: [category: AssetCategory]
  delete: [category: AssetCategory]
}>()

const handleEdit = () => {
  emit('edit', props.category)
}

const handleDelete = () => {
  emit('delete', props.category)
}
</script>

<style scoped lang="scss">
.asset-category-card {
  .category-card {
    margin-bottom: 16px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }

  .category-content {
    display: flex;
    align-items: center;
    gap: 16px;

    .category-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 64px;
      height: 64px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: white;
      flex-shrink: 0;
    }

    .category-info {
      flex: 1;
      min-width: 0;

      .category-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
        display: flex;
        align-items: center;

        .ml-8 {
          margin-left: 8px;
        }
      }

      .category-count {
        font-size: 14px;
        color: #909399;
        margin-bottom: 4px;
      }

      .category-sort {
        font-size: 12px;
        color: #c0c4cc;
      }
    }

    .category-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }
  }
}
</style>

