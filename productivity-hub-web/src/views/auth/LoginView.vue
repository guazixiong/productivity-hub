<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useDevice } from '@/composables/useDevice'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/services/api'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const captchaImage = ref('')
const loadingCaptcha = ref(false)

const form = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const loadCaptcha = async () => {
  loadingCaptcha.value = true
  try {
    const response = await authApi.getCaptcha()
    // 确保 response 有 image 和 key 字段
    if (!response || !response.image || !response.key) {
      throw new Error('验证码响应数据不完整')
    }
    // 构建 Base64 图片数据 URL
    captchaImage.value = `data:image/png;base64,${response.image}`
    form.captchaKey = response.key
    form.captcha = ''
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请刷新页面重试')
    // 清空验证码图片，显示占位符
    captchaImage.value = ''
  } finally {
    loadingCaptcha.value = false
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await authStore.login({
      username: form.username,
      password: form.password,
      captcha: form.captcha,
      captchaKey: form.captchaKey,
    })
    ElMessage.success('登录成功')
    router.replace((route.query.redirect as string) || '/home')
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : (error as string) || '登录失败'
    ElMessage.error(errorMessage)
    // 登录失败后刷新验证码
    loadCaptcha()
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h1>Productivity Hub 控制台</h1>

      <el-form ref="loginFormRef" :model="form" :rules="rules" label-position="top" size="large" class="login-form">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" autocomplete="username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-container">
            <el-input
              v-model="form.captcha"
              placeholder="请输入验证码"
              maxlength="4"
              class="captcha-input"
            />
            <div class="captcha-image-wrapper" @click="loadCaptcha" :class="{ loading: loadingCaptcha }">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-image" />
              <div v-else class="captcha-placeholder">点击获取验证码</div>
            </div>
          </div>
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="authStore.loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: 
    radial-gradient(circle at 20% 30%, rgba(99, 102, 241, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.25) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(236, 72, 153, 0.15) 0%, transparent 50%),
    linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #312e81 100%);
  background-attachment: fixed;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 70% 80%, rgba(255, 255, 255, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

.login-card {
  width: 440px;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.9) 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #f8fafc;
  backdrop-filter: blur(20px) saturate(180%);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6, #ec4899, #f97316);
  opacity: 0.8;
}

.login-card h1 {
  margin: 0 0 12px;
  background: linear-gradient(135deg, #ffffff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
  font-size: 28px;
  letter-spacing: -0.5px;
}

.login-card p {
  margin-top: 0;
  color: #94a3b8;
}

.login-form {
  margin-top: 32px;
}

.login-form :deep(.el-form-item__label) {
  color: #e2e8f0;
  font-weight: 500;
  margin-bottom: 8px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item:has(.captcha-container)) {
  display: flex;
  flex-direction: column;
}

.login-form :deep(.el-form-item:has(.captcha-container) .el-form-item__content) {
  display: flex;
  align-items: center;
  width: 100%;
  min-height: 50px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(99, 102, 241, 0.5);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.25);
}

.login-form :deep(.el-input__inner) {
  color: #f8fafc;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
  border: none;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: 8px;
}

.submit-btn:hover {
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.5);
}

.captcha-container {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-image-wrapper {
  width: 140px;
  height: 50px;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  padding: 0;
  box-sizing: border-box;
}

.captcha-image-wrapper:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(99, 102, 241, 0.4);
}

.captcha-image-wrapper.loading {
  opacity: 0.6;
  cursor: not-allowed;
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: fill;
  display: block;
}

.captcha-placeholder {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  text-align: center;
  padding: 0 8px;
}

/* 移动端适配 - REQ-001 */
@media (max-width: 768px) {
  .login-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 40px;
  }

  .login-card {
    width: 100%;
    max-width: 100%;
    border-radius: 16px;

    :deep(.el-card__body) {
      padding: 24px 20px;
    }
  }

  .login-card h1 {
    font-size: 24px;
    margin-bottom: 8px;
  }

  .login-form {
    margin-top: 24px;
  }

  .login-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  .submit-btn {
    height: 44px;
    font-size: 15px;
  }

  .captcha-container {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .captcha-input {
    width: 100%;
  }

  .captcha-image-wrapper {
    width: 100%;
    min-height: 50px;
    height: auto;
    align-self: stretch;
  }
}
</style>

