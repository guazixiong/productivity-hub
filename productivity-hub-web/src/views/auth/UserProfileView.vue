<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile } from 'element-plus'
import { useDevice } from '@/composables/useDevice'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/services/api'
import type { UserProfileUpdatePayload } from '@/types/auth'
import { ArrowLeft, User, Upload, Loading } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils/imageUtils'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const avatarLoading = ref(false)

const form = reactive<UserProfileUpdatePayload>({
  name: '',
  email: '',
  avatar: '',
  bio: '',
  phone: '',
  gender: '',
  birthday: '',
  address: '',
  company: '',
  position: '',
  website: '',
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 1, max: 50, message: '姓名长度应在1-50个字符之间', trigger: 'blur' },
  ],
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: 'blur',
    },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' },
  ],
  address: [
    { max: 500, message: '地址不能超过500个字符', trigger: 'blur' },
  ],
  company: [
    { max: 200, message: '公司名称不能超过200个字符', trigger: 'blur' },
  ],
  position: [
    { max: 100, message: '职位不能超过100个字符', trigger: 'blur' },
  ],
  website: [
    {
      type: 'url',
      message: '请输入正确的网址',
      trigger: 'blur',
    },
  ],
}

// 计算头像完整URL
const avatarUrl = computed(() => {
  return getImageUrl(form.avatar)
})

// 初始化表单数据
const initForm = () => {
  const user = authStore.user
  if (user) {
    form.name = user.name || ''
    form.email = user.email || ''
    form.avatar = user.avatar || ''
    form.bio = user.bio || ''
    form.phone = user.phone || ''
    form.gender = user.gender || ''
    form.birthday = user.birthday || ''
    form.address = user.address || ''
    form.company = user.company || ''
    form.position = user.position || ''
    form.website = user.website || ''
  }
}

// 头像上传前的验证
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义上传方法
const handleAvatarUpload = async (options: { file: File }) => {
  const { file } = options
  avatarLoading.value = true
  try {
    const response = await userApi.uploadAvatar(file)
    // response 已经是 UserInfo 类型（request 函数已经提取了 data）
    form.avatar = response.avatar || ''
    ElMessage.success('头像上传成功')
    // 更新store中的用户信息
    if (authStore.user) {
      authStore.user.avatar = response.avatar
      authStore.user.name = response.name
      authStore.user.email = response.email
      authStore.user.bio = response.bio
      authStore.user.phone = response.phone
      authStore.user.gender = response.gender
      authStore.user.birthday = response.birthday
      authStore.user.address = response.address
      authStore.user.company = response.company
      authStore.user.position = response.position
      authStore.user.website = response.website
      localStorage.setItem('phub/user', JSON.stringify(authStore.user))
    }
  } catch (error) {
    ElMessage.error((error as Error).message || '头像上传失败')
  } finally {
    avatarLoading.value = false
  }
}

// 移除头像
const removeAvatar = () => {
  ElMessageBox.confirm('确定要移除头像吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      form.avatar = ''
      if (authStore.user) {
        authStore.user.avatar = ''
        localStorage.setItem('phub/user', JSON.stringify(authStore.user))
      }
      ElMessage.success('头像已移除')
    })
    .catch(() => {
      // 用户取消
    })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const updatedUser = await authStore.updateUserInfo(form)
      ElMessage.success('个人信息更新成功')
      // 延迟一下让用户看到成功提示
      setTimeout(() => {
        router.back()
      }, 500)
    } catch (error) {
      ElMessage.error((error as string) || '更新失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 取消编辑
const handleCancel = () => {
  router.back()
}

onMounted(async () => {
  // 如果用户信息不存在，先获取
  if (!authStore.user) {
    try {
      const user = await userApi.getProfile()
      authStore.user = user
      localStorage.setItem('phub/user', JSON.stringify(user))
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  initForm()
})
</script>

<template>
  <div class="user-profile-page">
    <div class="page-header">
      <el-button text type="primary" :icon="ArrowLeft" @click="handleCancel">返回</el-button>
    </div>

    <el-card class="profile-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>我的信息</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="profile-form">
        <!-- 头像上传 -->
        <el-form-item label="头像">
          <div class="avatar-upload-container">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
            >
              <div v-if="form.avatar" class="avatar-preview">
                <img :src="avatarUrl" alt="头像" />
                <div class="avatar-overlay">
                  <el-icon><Upload /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
              <div v-else class="avatar-placeholder">
                <el-icon v-if="!avatarLoading"><User /></el-icon>
                <el-icon v-else class="is-loading"><Loading /></el-icon>
                <span v-if="!avatarLoading">上传头像</span>
                <span v-else>上传中...</span>
              </div>
            </el-upload>
            <div v-if="form.avatar" class="avatar-actions">
              <el-button text type="danger" size="small" @click="removeAvatar">移除头像</el-button>
            </div>
            <div class="avatar-hint">
              <el-text type="info" size="small">支持 JPG、PNG 格式，大小不超过 2MB</el-text>
            </div>
          </div>
        </el-form-item>

        <!-- 姓名 -->
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" type="email" placeholder="请输入邮箱地址" />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="11" />
        </el-form-item>

        <!-- 个人简介 -->
        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 性别 -->
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" clearable style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 生日 -->
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="请选择生日"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 地址 -->
        <el-form-item label="地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="2"
            placeholder="请输入地址（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 公司/组织 -->
        <el-form-item label="公司/组织" prop="company">
          <el-input v-model="form.company" placeholder="请输入公司或组织名称（可选）" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 职位 -->
        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" placeholder="请输入职位（可选）" maxlength="100" show-word-limit />
        </el-form-item>

        <!-- 个人网站 -->
        <el-form-item label="个人网站" prop="website">
          <el-input v-model="form.website" placeholder="请输入个人网站URL（可选）" />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.user-profile-page {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.profile-card {
  background: var(--surface-color);
  border: 1px solid rgba(99, 102, 241, 0.16);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.profile-form {
  padding: 8px 0;
}

.avatar-upload-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-preview:hover {
  border-color: #6366f1;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-overlay span {
  font-size: 12px;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border: 2px dashed #d1d5db;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f9fafb;
}

.avatar-placeholder:hover {
  border-color: #6366f1;
  background: #f3f4f6;
}

.avatar-placeholder .el-icon {
  font-size: 32px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.avatar-placeholder span {
  font-size: 12px;
  color: #6b7280;
}

.avatar-actions {
  margin-top: 8px;
}

.avatar-hint {
  margin-top: 4px;
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .user-profile-page {
    padding: 0;
  }

  .profile-card {
    border-radius: 0;
    margin: 0 -12px;

    :deep(.el-card__header) {
      padding: 16px;
    }

    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .profile-header {
    flex-direction: column;
    align-items: center;
    gap: 16px;
    text-align: center;
  }

  .avatar-section {
    width: 100%;
    align-items: center;
  }

  .profile-form {
    :deep(.el-form-item__label) {
      width: 100% !important;
      text-align: left;
      margin-bottom: 8px;
    }

    :deep(.el-form-item__content) {
      width: 100%;
    }
  }

  .form-actions {
    flex-direction: column;
    gap: 12px;

    .el-button {
      width: 100%;
    }
  }
}
</style>

