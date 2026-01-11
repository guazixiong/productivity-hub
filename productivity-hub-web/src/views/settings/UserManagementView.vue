<script setup lang="ts">
/**
 * 系统用户管理页面组件
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useDevice } from '@/composables/useDevice'
import { adminUserApi, aclRoleApi, aclUserRoleApi } from '@/services/api'
import type { ManagedUser, UserCreatePayload } from '@/types/auth'
import type { AclRoleVO } from '@/types/acl'
import { AclStatus, RoleType, RoleTypeDesc, AclStatusDesc } from '@/types/acl'

// 响应式设备检测 - REQ-001
const { isMobile, isTablet } = useDevice()

const users = ref<ManagedUser[]>([])
const roles = ref<AclRoleVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const bindDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const currentUser = ref<ManagedUser | null>(null)
const checkedRoleIds = ref<number[]>([])
const selectedRoleIds = ref<number[]>([])

const formModel = reactive<UserCreatePayload>({
  username: '',
  password: '',
  name: '',
  email: '',
  roles: [], // 保留用于向后兼容，但实际使用ACL角色
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

const fetchRoles = async () => {
  try {
    roles.value = await aclRoleApi.list()
  } catch (error) {
    ElMessage.error((error as Error).message || '获取角色列表失败')
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await adminUserApi.list()
    // 为每个用户加载ACL角色信息
    await Promise.all(users.value.map(async (user) => {
      try {
        const roleIds = await aclUserRoleApi.getUserRoles(user.id)
        // 将角色ID存储到用户的扩展属性中
        ;(user as any).aclRoleIds = roleIds || []
      } catch (error) {
        ;(user as any).aclRoleIds = []
      }
    }))
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
  formModel.roles = []
  selectedRoleIds.value = []
}

const handleCreate = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  // 验证是否选择了角色
  if (selectedRoleIds.value.length === 0) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  
  loading.value = true
  try {
    // 为了向后兼容，将选中的角色ID转换为角色名称（使用第一个角色的名称作为默认值）
    // 实际的角色绑定通过ACL系统完成
    const defaultRole = roles.value.find(r => r.id === selectedRoleIds.value[0])
    formModel.roles = defaultRole ? [defaultRole.name.toLowerCase()] : ['user']
    
    const created = await adminUserApi.create(formModel)
    
    // 创建用户后，绑定ACL角色
    if (selectedRoleIds.value.length > 0) {
      try {
        await aclUserRoleApi.bindRoles({
          userId: created.id,
          roleIds: selectedRoleIds.value,
        })
      } catch (error) {
        ElMessage.warning('用户已创建，但角色绑定失败：' + ((error as Error).message || '未知错误'))
      }
    }
    
    await fetchUsers() // 重新获取用户列表以包含角色信息
    ElMessage.success('用户已创建，配置已按模板初始化')
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error((error as Error).message || '创建失败')
  } finally {
    loading.value = false
  }
}

const openBindDialog = async (user: ManagedUser) => {
  currentUser.value = user
  // 获取用户当前的角色ID列表
  try {
    const roleIds = await aclUserRoleApi.getUserRoles(user.id)
    checkedRoleIds.value = [...(roleIds || [])]
  } catch (error) {
    checkedRoleIds.value = []
  }
  bindDialogVisible.value = true
}

const handleBindRoles = async () => {
  if (!currentUser.value) return

  loading.value = true
  try {
    await aclUserRoleApi.bindRoles({
      userId: currentUser.value.id,
      roleIds: checkedRoleIds.value,
    })
    ElMessage.success('角色绑定成功')
    bindDialogVisible.value = false
    await fetchUsers()
  } catch (error) {
    ElMessage.error((error as Error).message || '绑定失败')
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

const getRoleName = (roleId: number): string => {
  const role = roles.value.find(r => r.id === roleId)
  return role ? role.name : `角色${roleId}`
}

const getRoleTagType = (roleId: number): string => {
  const role = roles.value.find(r => r.id === roleId)
  if (!role) return ''
  if (role.type === RoleType.ADMIN) return 'danger'
  if (role.type === RoleType.USER) return 'success'
  return 'info'
}

onMounted(() => {
  fetchRoles()
  fetchUsers()
})
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

    <div class="table-wrapper">
      <el-table :data="users" :loading="loading" border class="user-table">
      <el-table-column prop="username" label="用户名" width="160" />
      <el-table-column prop="name" label="姓名" width="140" />
      <el-table-column label="ACL角色" min-width="200">
        <template #default="{ row }">
          <el-tag
            v-for="roleId in (row as any).aclRoleIds || []"
            :key="roleId"
            size="small"
            style="margin-right: 6px"
            :type="getRoleTagType(roleId)"
          >
            {{ getRoleName(roleId) }}
          </el-tag>
          <span v-if="!((row as any).aclRoleIds || []).length" class="text-muted">未分配角色</span>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column prop="updatedAt" label="最近更新时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            link
            @click="openBindDialog(row)"
            v-button-lock
          >
            分配角色
          </el-button>
          <el-button
            type="danger"
            size="small"
            link
            :loading="loading"
            @click="handleDelete(row)"
            v-button-lock
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
  </el-card>

  <el-dialog v-model="dialogVisible" title="新增用户" :width="isMobile ? '90%' : '520px'" destroy-on-close>
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
      <el-form-item label="ACL角色" required>
        <el-select 
          v-model="selectedRoleIds" 
          multiple 
          placeholder="请选择角色" 
          style="width: 100%"
          :disabled="roles.length === 0"
        >
          <el-option
            v-for="role in roles"
            :key="role.id"
            :label="role.name"
            :value="role.id"
            :disabled="role.status !== AclStatus.ENABLED"
          >
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>{{ role.name }}</span>
              <el-tag size="small" :type="role.type === RoleType.ADMIN ? 'danger' : role.type === RoleType.USER ? 'success' : 'info'">
                {{ RoleTypeDesc[role.type] }}
              </el-tag>
              <el-tag size="small" :type="role.status === AclStatus.ENABLED ? 'success' : 'danger'">
                {{ AclStatusDesc[role.status] }}
              </el-tag>
            </div>
          </el-option>
        </el-select>
        <div class="form-tip">请至少选择一个角色，只有启用状态的角色可选</div>
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

  <!-- 角色绑定对话框 -->
  <el-dialog
    v-model="bindDialogVisible"
    :title="`分配角色 - ${currentUser?.name} (${currentUser?.username})`"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-checkbox-group v-model="checkedRoleIds" class="role-checkbox-group">
      <el-checkbox
        v-for="role in roles"
        :key="role.id"
        :label="role.id"
        :disabled="role.status !== AclStatus.ENABLED"
      >
        <div class="role-item">
          <span class="role-name">{{ role.name }}</span>
          <el-tag size="small" :type="role.type === RoleType.ADMIN ? 'danger' : role.type === RoleType.USER ? 'success' : 'info'">
            {{ RoleTypeDesc[role.type] }}
          </el-tag>
          <el-tag size="small" :type="role.status === AclStatus.ENABLED ? 'success' : 'danger'">
            {{ AclStatusDesc[role.status] }}
          </el-tag>
          <span v-if="role.remark" class="role-remark">{{ role.remark }}</span>
        </div>
      </el-checkbox>
    </el-checkbox-group>

    <div v-if="roles.length === 0" class="empty-roles">
      <el-empty description="暂无可用角色，请先创建角色" />
    </div>

    <template #footer>
      <el-button @click="bindDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleBindRoles" :loading="loading">确定</el-button>
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

.table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.table-wrapper::-webkit-scrollbar {
  height: 6px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.user-table {
  min-width: 700px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

.role-checkbox-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .role-item {
    display: flex;
    align-items: center;
    gap: 8px;

    .role-name {
      font-weight: 500;
      min-width: 100px;
    }

    .role-remark {
      color: #909399;
      font-size: 12px;
      margin-left: auto;
    }
  }
}

.empty-roles {
  padding: 40px 0;
}

/* 移动端适配 - REQ-001-02 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header h3 {
    font-size: 18px;
    margin: 0;
  }

  .desc {
    font-size: 13px;
    margin-top: 4px;
  }

  .header .el-button {
    width: 100%;
  }

  /* 表格横向滚动支持 - REQ-001-05 */
  .table-wrapper {
    margin: 0 -12px;
  }

  :deep(.el-table__cell) {
    padding: 8px 6px;
    font-size: 13px;
  }

  :deep(.el-table__header-wrapper th) {
    padding: 8px 6px;
    font-size: 12px;
  }

  /* 对话框移动端优化 */
  :deep(.el-dialog) {
    margin: 5vh auto;
  }

  :deep(.el-dialog__body) {
    padding: 16px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
  }

  :deep(.el-input__inner),
  :deep(.el-select) {
    font-size: 14px;
  }

  /* 禁用移动端hover效果 */
  :deep(.el-table__body tr:hover > td) {
    background: transparent !important;
  }
}
</style>

