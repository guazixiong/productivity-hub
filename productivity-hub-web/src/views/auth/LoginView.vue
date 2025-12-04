<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await authStore.login(form)
    ElMessage.success('登录成功')
    router.replace((route.query.redirect as string) || '/home')
  } catch (error) {
    const errorMessage = error instanceof Error ? error.message : (error as string) || '登录失败'
    ElMessage.error(errorMessage)
  }
}
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
  background: radial-gradient(circle at top, #172554, #020617);
  padding: 24px;
}

.login-card {
  width: 420px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: #f8fafc;
}

.login-card h1 {
  margin: 0 0 8px;
}

.login-card p {
  margin-top: 0;
  color: #94a3b8;
}

.login-form {
  margin-top: 24px;
}

.submit-btn {
  width: 100%;
}
</style>

