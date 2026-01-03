<template>
  <div class="wishlist-form-view">
    <el-page-header @back="handleBack">
      <template #content>
        <span>{{ isEdit ? '编辑心愿单' : '创建心愿单' }}</span>
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
            <el-form-item label="心愿名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入心愿名称" maxlength="100" show-word-limit />
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
                placeholder="请输入价格"
              />
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="链接" prop="link">
              <el-input
                v-model="form.link"
                placeholder="请输入心愿链接（可选）"
                maxlength="500"
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
                :rows="4"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { wishlistApi } from '@/services/wishlistApi'
import type { WishlistCreateDTO, WishlistUpdateDTO } from '@/types/wishlist'
import { useTabsStore } from '@/stores/tabs'

const route = useRoute()
const router = useRouter()
const tabsStore = useTabsStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const isEdit = ref(false)

const form = reactive<WishlistCreateDTO & { id?: string }>({
  name: '',
  price: 0,
  link: '',
  remark: '',
})

const rules: FormRules = {
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

const loadWishlist = async () => {
  const id = route.params.id as string
  if (!id) return

  isEdit.value = true
  loading.value = true
  try {
    const data = await wishlistApi.getWishlistById(id)
    if (data) {
      form.id = data.id
      form.name = data.name
      form.price = data.price
      form.link = data.link || ''
      form.remark = data.remark || ''
    }
  } catch (error) {
    ElMessage.error('加载心愿单失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  router.push('/wishlist')
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        const updateData: WishlistUpdateDTO = {
          id: form.id!,
          name: form.name,
          price: form.price,
          link: form.link || undefined,
          remark: form.remark || undefined,
        }
        await wishlistApi.updateWishlist(updateData)
        ElMessage.success('更新成功')
      } else {
        const createData: WishlistCreateDTO = {
          name: form.name,
          price: form.price,
          link: form.link || undefined,
          remark: form.remark || undefined,
        }
        await wishlistApi.createWishlist(createData)
        ElMessage.success('创建成功')
        // 关闭当前创建表单的标签页
        tabsStore.removeTab(route.fullPath)
      }
      router.push('/wishlist')
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  if (route.params.id) {
    loadWishlist()
  }
})
</script>

<style scoped>
.wishlist-form-view {
  padding: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.w-100 {
  width: 100%;
}
</style>

