<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { adminUserApi } from '@/services/api'
import type { ManagedUser, UserCreatePayload } from '@/types/auth'

const users = ref<ManagedUser[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const roleOptions = ['admin', 'ops', 'user']
const formModel = reactive<UserCreatePayload>({
  username: '',
  password: '',
  name: '',
  email: '',
  roles: ['user'],
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roles: [{ type: 'array', required: true, message: '请至少选择一个角色', trigger: 'change' }],
}

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await adminUserApi.list()
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  formModel.username = ''
  formModel.password = ''
  formModel.name = ''
  formModel.email = ''
  formModel.roles = ['user']
}

const handleCreate = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const created = await adminUserApi.create(formModel)
    users.value = [created, ...users.value]
    ElMessage.success('用户已创建，配置已按模板初始化')
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error((error as Error).message || '创建失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (user: ManagedUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.name} (${user.username})" 吗？此操作将删除该用户的所有数据，包括配置、书签、健康记录、待办事项等，且无法恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
      }
    )
    loading.value = true
    try {
      await adminUserApi.delete(user.id)
      users.value = users.value.filter(u => u.id !== user.id)
      ElMessage.success('用户已删除')
    } catch (error) {
      ElMessage.error((error as Error).message || '删除失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消删除
  }
}

onMounted(fetchUsers)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div class="header">
        <div>
          <h3>系统用户管理</h3>
          <p class="desc">仅管理员可见，新增用户时会自动复制默认配置模板</p>
        </div>
        <el-button type="primary" @click="openDialog">新增用户</el-button>
      </div>
    </template>

    <el-table :data="users" :loading="loading" border>
      <el-table-column prop="username" label="用户名" width="160" />
      <el-table-column prop="name" label="姓名" width="140" />
      <el-table-column label="角色" width="200">
        <template #default="{ row }">
          <el-tag v-for="role in row.roles" :key="role" size="small" style="margin-right: 6px">
            {{ role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column prop="updatedAt" label="最近更新时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            type="danger"
            size="small"
            :loading="loading"
            @click="handleDelete(row)"
            v-button-lock
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增用户" width="520px" destroy-on-close>
    <el-form ref="formRef" :model="formModel" :rules="rules" label-width="90px" label-position="left">
      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="formModel.username" 
          placeholder="请输入登录用户名" 
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="初始密码" prop="password">
        <el-input 
          v-model="formModel.password" 
          placeholder="留空则使用默认密码 123456" 
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input 
          v-model="formModel.name" 
          placeholder="请输入姓名" 
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input 
          v-model="formModel.email" 
          placeholder="可选，用于通知" 
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="角色" prop="roles">
        <el-select v-model="formModel.roles" multiple placeholder="请选择角色" style="width: 100%">
          <el-option v-for="role in roleOptions" :key="role" :label="role" :value="role" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-alert type="info" show-icon :closable="false" title="创建后会复制默认配置到该用户下，密码可在登录后自行重置" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleCreate" v-button-lock>确认创建</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.desc {
  margin: 4px 0 0;
  color: #64748b;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>

