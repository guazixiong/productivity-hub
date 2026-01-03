<template>
  <div class="wishlist-detail-view">
    <el-page-header @back="handleBack">
      <template #content>
        <span>心愿单详情</span>
      </template>
      <template #extra>
        <el-button type="primary" @click="handleEdit">编辑</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-16" v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="心愿名称">
          {{ wishlist?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="价格">
          {{ formatCurrency(wishlist?.price) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="wishlist" :type="wishlist.achieved ? 'success' : 'info'">
            {{ wishlist.achieved ? '已实现' : '未实现' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实现时间">
          {{ wishlist?.achievedAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ wishlist?.createdAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="链接" :span="2">
          <el-link
            v-if="wishlist?.link"
            :href="wishlist.link"
            target="_blank"
            type="primary"
          >
            {{ wishlist.link }}
          </el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <div class="remark-content">
            {{ wishlist?.remark || '无' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { wishlistApi } from '@/services/wishlistApi'
import type { Wishlist } from '@/types/wishlist'
import { formatCurrency } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const wishlist = ref<Wishlist | null>(null)

const loadWishlist = async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('心愿单ID不能为空')
    return
  }

  loading.value = true
  try {
    const res = await wishlistApi.getWishlistById(id)
    wishlist.value = res || null
  } catch (error) {
    ElMessage.error('加载心愿单详情失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/wishlist')
}

const handleEdit = () => {
  const id = route.params.id as string
  router.push(`/wishlist/edit/${id}`)
}

onMounted(() => {
  loadWishlist()
})
</script>

<style scoped>
.wishlist-detail-view {
  padding: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.remark-content {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>

