<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()

const handleResetPassword = async () => {
  if (!formRef.value) return
  try {
    await ElMessageBox.confirm('确定要将密码重置为默认密码 123456 吗？', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await authStore.resetPassword()
    ElMessage.success('密码已重置为 123456，请妥善保管')
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置密码失败')
    }
  }
}
</script>

<template>
  <div class="reset-password-page">
    <el-card class="reset-card" shadow="hover">
      <div class="card-header">
        <h1>重置密码</h1>
        <p>将当前登录用户的密码重置为默认密码</p>
      </div>

      <div class="warning-box">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>
            <div class="warning-content">
              <strong>重要提示</strong>
              <p>重置密码后，您的密码将被设置为默认密码：<code>123456</code></p>
              <p>请妥善保管新密码，建议在重置后立即修改为更安全的密码。</p>
            </div>
          </template>
        </el-alert>
      </div>

      <div class="user-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="当前用户">
            {{ authStore.user?.name ?? '访客' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户角色">
            {{ authStore.user?.roles?.join(', ') ?? '--' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="action-buttons">
        <el-button ref="formRef" type="primary" :loading="authStore.loading" @click="handleResetPassword">
          确认重置密码
        </el-button>
        <el-button @click="router.back()">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.reset-password-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.reset-card {
  width: 560px;
  background: var(--surface-color);
  border: 1px solid rgba(99, 102, 241, 0.16);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}

.card-header h1 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #0f172a;
}

.card-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.warning-box {
  margin-bottom: 24px;
}

.warning-content {
  line-height: 1.6;
}

.warning-content strong {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
}

.warning-content p {
  margin: 4px 0;
  font-size: 13px;
}

.warning-content code {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.user-info {
  margin-bottom: 24px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 120px;
}
</style>

