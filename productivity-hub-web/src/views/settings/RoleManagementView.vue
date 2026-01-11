<script setup lang="ts">
/**
 * ACL角色管理页面
 * TASK-REQ-002-05: 前端角色管理页 - 列表+表单+菜单树勾选
 */
import { onMounted, reactive, ref, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, User, Message, Filter, Refresh } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { aclRoleApi, aclMenuApi, aclUserRoleApi, adminUserApi } from '@/services/api'
import type { AclRoleVO, AclRoleCreateDTO, AclRoleUpdateDTO, AclRoleMenuBindDTO, AclMenuTreeVO, AclUserRoleBindDTO } from '@/types/acl'
import type { ManagedUser } from '@/types/auth'
import { RoleType, AclStatus, RoleTypeDesc, AclStatusDesc } from '@/types/acl'
import type { ElTree } from 'element-plus'

const { isMobile, isTablet } = useDevice()

const roles = ref<AclRoleVO[]>([])
const menuTree = ref<AclMenuTreeVO[]>([])
const users = ref<ManagedUser[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const bindDialogVisible = ref(false)
const userBindDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const menuTreeRef = ref<InstanceType<typeof ElTree>>()
const isEdit = ref(false)
const currentRole = ref<AclRoleVO | null>(null)

const formModel = reactive<AclRoleCreateDTO & { id?: number }>({
  id: undefined,
  name: '',
  type: RoleType.CUSTOM,
  status: AclStatus.ENABLED,
  remark: '',
  idempotentKey: '',
})

const checkedMenuIds = ref<number[]>([])
const checkedUserIds = ref<string[]>([])
const userSearchKeyword = ref('')
const isIndeterminate = ref(false)
const checkAll = ref(false)

// 搜索和筛选
const searchKeyword = ref('')
const filterType = ref<RoleType | ''>('')
const filterStatus = ref<AclStatus | ''>('')

const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
}

const fetchRoles = async () => {
  loading.value = true
  try {
    roles.value = await aclRoleApi.list()
  } catch (error) {
    ElMessage.error((error as Error).message || '获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const fetchMenus = async () => {
  try {
    menuTree.value = await aclMenuApi.getTree()
  } catch (error) {
    ElMessage.error((error as Error).message || '获取菜单列表失败')
  }
}

const fetchUsers = async () => {
  try {
    users.value = await adminUserApi.list()
  } catch (error) {
    ElMessage.error((error as Error).message || '获取用户列表失败')
  }
}

const openDialog = (role?: AclRoleVO) => {
  resetForm()
  if (role) {
    // 编辑模式
    isEdit.value = true
    formModel.id = role.id
    formModel.name = role.name
    formModel.type = role.type
    formModel.status = role.status
    formModel.remark = role.remark || ''
  } else {
    // 创建模式
    isEdit.value = false
    formModel.idempotentKey = `role_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
  dialogVisible.value = true
}

const resetForm = () => {
  formModel.id = undefined
  formModel.name = ''
  formModel.type = RoleType.CUSTOM
  formModel.status = AclStatus.ENABLED
  formModel.remark = ''
  formModel.idempotentKey = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value) {
      const updateDTO: AclRoleUpdateDTO = {
        id: formModel.id!,
        name: formModel.name,
        status: formModel.status,
        remark: formModel.remark,
      }
      await aclRoleApi.update(updateDTO)
      ElMessage.success('角色更新成功')
    } else {
      const createDTO: AclRoleCreateDTO = {
        name: formModel.name,
        type: formModel.type,
        status: formModel.status,
        remark: formModel.remark,
        idempotentKey: formModel.idempotentKey,
      }
      await aclRoleApi.create(createDTO)
      ElMessage.success('角色创建成功')
    }
    dialogVisible.value = false
    await fetchRoles()
  } catch (error) {
    ElMessage.error((error as Error).message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    loading.value = false
  }
}

const handleDelete = async (role: AclRoleVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${role.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    loading.value = true
    try {
      await aclRoleApi.delete(role.id)
      ElMessage.success('角色已删除')
      await fetchRoles()
    } catch (error) {
      ElMessage.error((error as Error).message || '删除失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消删除
  }
}

const openBindDialog = async (role: AclRoleVO) => {
  currentRole.value = role
  checkedMenuIds.value = [...(role.menuIds || [])]
  bindDialogVisible.value = true
  if (menuTree.value.length === 0) {
    await fetchMenus()
  }
  // 等待对话框打开后，更新树组件的选中状态
  await nextTick()
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys(checkedMenuIds.value)
  }
}

// 监听对话框打开，更新树组件的选中状态（作为备用方案）
watch(bindDialogVisible, async (visible) => {
  if (visible && currentRole.value && menuTreeRef.value) {
    await nextTick()
    const menuIds = currentRole.value.menuIds || []
    checkedMenuIds.value = [...menuIds]
    menuTreeRef.value.setCheckedKeys(menuIds)
  } else if (!visible) {
    // 对话框关闭时清理状态
    checkedMenuIds.value = []
  }
})

const handleBindMenus = async () => {
  if (!currentRole.value) return

  loading.value = true
  try {
    // 从树组件获取当前选中的节点（更可靠）
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() as number[] || checkedMenuIds.value
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() as number[] || []
    
    // 只使用完全选中的节点，不包含半选中的节点
    const finalMenuIds = checkedKeys || []
    
    // 确保 menuIds 是数字数组
    const validMenuIds = finalMenuIds.filter(id => typeof id === 'number' && !isNaN(id))
    
    const bindDTO: AclRoleMenuBindDTO = {
      roleId: String(currentRole.value.id),
      menuIds: validMenuIds,
    }
    
    console.log('发送绑定请求:', {
      roleId: bindDTO.roleId,
      menuIds: bindDTO.menuIds,
      menuCount: bindDTO.menuIds.length,
    })
    
    await aclRoleApi.bindMenus(bindDTO)
    ElMessage.success('菜单绑定成功')
    
    // 更新当前角色的 menuIds，以便下次打开对话框时能正确回显
    currentRole.value.menuIds = [...validMenuIds]
    checkedMenuIds.value = [...validMenuIds]
    
    // 更新列表中的角色数据
    await fetchRoles()
    
    bindDialogVisible.value = false
  } catch (error) {
    console.error('绑定菜单失败:', error)
    ElMessage.error((error as Error).message || '绑定失败')
  } finally {
    loading.value = false
  }
}

const handleMenuCheck = () => {
  // 当用户点击复选框时，更新 checkedMenuIds
  // 使用 nextTick 确保树组件状态已更新
  nextTick(() => {
    if (menuTreeRef.value) {
      const checkedKeys = menuTreeRef.value.getCheckedKeys() as number[]
      checkedMenuIds.value = checkedKeys || []
    }
  })
}

// 过滤后的用户列表
const filteredUsers = computed(() => {
  if (!userSearchKeyword.value) {
    return users.value
  }
  const keyword = userSearchKeyword.value.toLowerCase()
  return users.value.filter(user => 
    user.name?.toLowerCase().includes(keyword) ||
    user.username?.toLowerCase().includes(keyword) ||
    user.email?.toLowerCase().includes(keyword)
  )
})

// 过滤后的已选用户数量
const filteredCheckedCount = computed(() => {
  return filteredUsers.value.filter(user => checkedUserIds.value.includes(user.id)).length
})

// 监听选中状态，更新全选和半选状态
watch([checkedUserIds, filteredUsers], () => {
  const filteredCount = filteredUsers.value.length
  const checkedCount = filteredCheckedCount.value
  checkAll.value = filteredCount > 0 && checkedCount === filteredCount
  isIndeterminate.value = checkedCount > 0 && checkedCount < filteredCount
}, { immediate: true, deep: true })

// 全选/取消全选
const handleCheckAll = (val: boolean) => {
  if (val) {
    // 全选：添加所有过滤后的用户
    const filteredUserIds = filteredUsers.value.map(u => u.id)
    checkedUserIds.value = [...new Set([...checkedUserIds.value, ...filteredUserIds])]
  } else {
    // 取消全选：移除所有过滤后的用户
    const filteredUserIds = filteredUsers.value.map(u => u.id)
    checkedUserIds.value = checkedUserIds.value.filter(id => !filteredUserIds.includes(id))
  }
}

const openUserBindDialog = async (role: AclRoleVO) => {
  currentRole.value = role
  userSearchKeyword.value = ''
  // 获取该角色关联的用户ID列表
  try {
    const userIds = await aclUserRoleApi.getUsersByRole(role.id)
    checkedUserIds.value = [...(userIds || [])]
  } catch (error) {
    checkedUserIds.value = []
  }
  userBindDialogVisible.value = true
  if (users.value.length === 0) {
    await fetchUsers()
  }
}

const handleBindUsers = async () => {
  if (!currentRole.value) return

  loading.value = true
  const errors: string[] = []
  try {
    // 获取所有用户，为每个用户更新角色绑定
    const allUsers = users.value
    const roleId = currentRole.value.id
    
    // 批量更新每个用户的角色绑定
    const results = await Promise.allSettled(
      allUsers.map(async (user) => {
        try {
          // 获取用户当前的角色ID列表
          const currentRoleIds = await aclUserRoleApi.getUserRoles(user.id)
          
          // 判断用户是否应该拥有该角色
          const shouldHaveRole = checkedUserIds.value.includes(user.id)
          const hasRole = currentRoleIds.includes(roleId)
          
          // 如果状态需要改变，更新绑定
          if (shouldHaveRole && !hasRole) {
            // 添加角色
            const newRoleIds = [...currentRoleIds, roleId]
            await aclUserRoleApi.bindRoles({
              userId: user.id,
              roleIds: newRoleIds,
            })
          } else if (!shouldHaveRole && hasRole) {
            // 移除角色
            const newRoleIds = currentRoleIds.filter(id => id !== roleId)
            await aclUserRoleApi.bindRoles({
              userId: user.id,
              roleIds: newRoleIds,
            })
          }
        } catch (error) {
          const errorMsg = `用户 ${user.name}(${user.username}) 更新失败: ${(error as Error).message}`
          errors.push(errorMsg)
          throw error
        }
      })
    )
    
    // 检查是否有失败的更新
    const failedCount = results.filter(r => r.status === 'rejected').length
    if (failedCount > 0) {
      ElMessage.warning(`部分用户更新失败 (${failedCount}/${allUsers.length})`)
      if (errors.length > 0) {
        console.error('用户关联错误:', errors)
      }
    } else {
      ElMessage.success('用户关联成功')
    }
    
    userBindDialogVisible.value = false
  } catch (error) {
    console.error('绑定用户失败:', error)
    ElMessage.error((error as Error).message || '绑定失败')
  } finally {
    loading.value = false
  }
}

// 辅助函数：获取用户首字母
const getUserInitials = (name: string): string => {
  if (!name) return '?'
  const chars = name.trim().split('')
  if (chars.length >= 2) {
    return (chars[0] + chars[chars.length - 1]).toUpperCase()
  }
  return chars[0].toUpperCase()
}

// 辅助函数：根据用户名生成头像颜色
const getAvatarColor = (name: string): string => {
  const colors = [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
    '#ff7875', '#ffa940', '#52c41a', '#1890ff', '#722ed1'
  ]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

// 过滤后的角色列表
const filteredRoles = computed(() => {
  let result = roles.value

  // 按名称搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(role => 
      role.name.toLowerCase().includes(keyword) ||
      role.remark?.toLowerCase().includes(keyword)
    )
  }

  // 按类型筛选
  if (filterType.value) {
    result = result.filter(role => role.type === filterType.value)
  }

  // 按状态筛选
  if (filterStatus.value) {
    result = result.filter(role => role.status === filterStatus.value)
  }

  return result
})

// 统计信息
const stats = computed(() => {
  const total = roles.value.length
  const enabled = roles.value.filter(r => r.status === AclStatus.ENABLED).length
  const disabled = roles.value.filter(r => r.status === AclStatus.DISABLED).length
  const adminCount = roles.value.filter(r => r.type === RoleType.ADMIN).length
  const customCount = roles.value.filter(r => r.type === RoleType.CUSTOM).length
  
  return {
    total,
    enabled,
    disabled,
    adminCount,
    customCount
  }
})

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  filterType.value = ''
  filterStatus.value = ''
}

onMounted(() => {
  fetchRoles()
  fetchMenus()
  fetchUsers()
})
</script>

<template>
  <div class="role-management-view">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card stat-total">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总角色数</div>
        </div>
      </div>
      <div class="stat-card stat-enabled">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.enabled }}</div>
          <div class="stat-label">已启用</div>
        </div>
      </div>
      <div class="stat-card stat-disabled">
        <div class="stat-icon">❌</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.disabled }}</div>
          <div class="stat-label">已禁用</div>
        </div>
      </div>
      <div class="stat-card stat-admin">
        <div class="stat-icon">🔐</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.adminCount }}</div>
          <div class="stat-label">管理员角色</div>
        </div>
      </div>
    </div>

    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2 class="card-title">角色管理</h2>
          <el-button type="primary" @click="openDialog()" :disabled="loading" class="add-button">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索角色名称或备注"
            clearable
            class="search-input"
            :prefix-icon="Search"
            style="width: 300px;"
          />
          <el-select
            v-model="filterType"
            placeholder="角色类型"
            clearable
            class="filter-select"
            style="width: 150px;"
          >
            <el-option
              v-for="type in [RoleType.ADMIN, RoleType.USER, RoleType.CUSTOM]"
              :key="type"
              :label="RoleTypeDesc[type]"
              :value="type"
            />
          </el-select>
          <el-select
            v-model="filterStatus"
            placeholder="状态"
            clearable
            class="filter-select"
            style="width: 120px;"
          >
            <el-option
              v-for="status in [AclStatus.ENABLED, AclStatus.DISABLED]"
              :key="status"
              :label="AclStatusDesc[status]"
              :value="status"
            />
          </el-select>
          <el-button
            v-if="searchKeyword || filterType || filterStatus"
            :icon="Refresh"
            @click="resetFilters"
            link
          >
            重置
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button :icon="Refresh" @click="fetchRoles" :loading="loading" circle />
        </div>
      </div>

      <!-- 空态 -->
      <el-empty v-if="!loading && filteredRoles.length === 0 && roles.length === 0" description="暂无角色数据" />
      <el-empty v-else-if="!loading && filteredRoles.length === 0" description="未找到匹配的角色" />

      <!-- 角色列表 -->
      <div v-else class="table-wrapper">
        <el-table :data="filteredRoles" v-loading="loading" stripe class="role-table" :default-sort="{ prop: 'id', order: 'ascending' }">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="type" label="角色类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === RoleType.ADMIN ? 'danger' : row.type === RoleType.USER ? 'success' : 'info'">
              {{ RoleTypeDesc[row.type] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === AclStatus.ENABLED ? 'success' : 'danger'">
              {{ AclStatusDesc[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuIds" label="菜单数量" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain">
              {{ row.menuIds?.length || 0 }} 个
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.remark" style="color: #64748b;">{{ row.remark }}</span>
            <span v-else style="color: #cbd5e1;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
              <el-button size="small" type="primary" link @click="openBindDialog(row)">
                菜单({{ row.menuIds?.length || 0 }})
              </el-button>
              <el-button size="small" type="info" link @click="openUserBindDialog(row)">用户</el-button>
              <el-button 
                size="small" 
                type="danger" 
                link 
                @click="handleDelete(row)"
                :disabled="row.type === RoleType.ADMIN"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      </div>
    </el-card>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="600px"
      :close-on-click-modal="false"
      class="role-form-dialog"
      align-center
    >
      <el-form
        ref="formRef"
        :model="formModel"
        :rules="rules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formModel.name" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="角色类型" prop="type">
          <el-radio-group v-model="formModel.type" :disabled="isEdit" class="role-type-radio">
            <el-radio :value="RoleType.ADMIN" class="radio-item">
              <el-tag type="danger" size="small">{{ RoleTypeDesc[RoleType.ADMIN] }}</el-tag>
            </el-radio>
            <el-radio :value="RoleType.USER" class="radio-item">
              <el-tag type="success" size="small">{{ RoleTypeDesc[RoleType.USER] }}</el-tag>
            </el-radio>
            <el-radio :value="RoleType.CUSTOM" class="radio-item">
              <el-tag type="info" size="small">{{ RoleTypeDesc[RoleType.CUSTOM] }}</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status" class="status-radio">
            <el-radio :value="AclStatus.ENABLED" class="radio-item">
              <el-tag type="success" size="small">{{ AclStatusDesc[AclStatus.ENABLED] }}</el-tag>
            </el-radio>
            <el-radio :value="AclStatus.DISABLED" class="radio-item">
              <el-tag type="danger" size="small">{{ AclStatusDesc[AclStatus.DISABLED] }}</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formModel.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading" size="large">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 菜单绑定对话框 -->
    <el-dialog
      v-model="bindDialogVisible"
      :title="`绑定菜单 - ${currentRole?.name}`"
      width="800px"
      :close-on-click-modal="false"
      class="menu-bind-dialog"
      align-center
    >
      <el-tree
        ref="menuTreeRef"
        v-loading="loading"
        :data="menuTree"
        :props="{ children: 'children', label: 'name' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        @check="handleMenuCheck"
        class="menu-bind-tree"
      >
        <template #default="{ node, data }">
          <div class="menu-tree-node">
            <el-icon v-if="data.icon" class="menu-icon">
              <component :is="data.icon" />
            </el-icon>
            <span class="menu-name">{{ data.name }}</span>
            <el-tag size="small" :type="data.type === 'DIR' ? 'info' : 'success'">
              {{ data.type === 'DIR' ? '目录' : '菜单' }}
            </el-tag>
            <span class="menu-path" v-if="data.path">{{ data.path }}</span>
          </div>
        </template>
      </el-tree>

      <template #footer>
        <div class="dialog-footer">
          <div class="footer-info">
            已选择 <strong>{{ checkedMenuIds.length }}</strong> 个菜单
          </div>
          <div class="footer-actions">
            <el-button @click="bindDialogVisible = false" size="large">取消</el-button>
            <el-button type="primary" @click="handleBindMenus" :loading="loading" size="large">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 用户关联对话框 -->
    <el-dialog
      v-model="userBindDialogVisible"
      :title="`关联用户 - ${currentRole?.name}`"
      width="900px"
      :close-on-click-modal="false"
      class="user-bind-dialog"
      align-center
    >
      <div class="user-bind-content">
        <!-- 统计信息和操作栏 -->
        <div class="user-bind-header">
          <div class="user-stats">
            <span class="stat-item">
              总用户数: <strong>{{ users.length }}</strong>
            </span>
            <span class="stat-item">
              已关联: <strong class="stat-highlight">{{ checkedUserIds.length }}</strong>
            </span>
            <span v-if="userSearchKeyword" class="stat-item">
              搜索结果: <strong>{{ filteredUsers.length }}</strong>
            </span>
          </div>
          <div class="user-actions">
            <el-checkbox
              v-model="checkAll"
              :indeterminate="isIndeterminate"
              @change="handleCheckAll"
              class="check-all-box"
            >
              全选当前{{ userSearchKeyword ? '结果' : '列表' }}
            </el-checkbox>
          </div>
        </div>

        <!-- 搜索框 -->
        <div class="user-search-bar">
          <el-input
            v-model="userSearchKeyword"
            placeholder="搜索用户（姓名、用户名、邮箱）"
            clearable
            :prefix-icon="Search"
            class="search-input"
          />
        </div>

        <!-- 用户列表 -->
        <div class="user-list-container">
          <el-checkbox-group v-model="checkedUserIds" class="user-checkbox-group">
            <div
              v-for="user in filteredUsers"
              :key="user.id"
              class="user-card"
            >
              <el-checkbox :value="user.id" class="user-checkbox">
                <div class="user-item">
                  <div class="user-avatar">
                    <el-avatar :size="40" :style="{ backgroundColor: getAvatarColor(user.name || user.username) }">
                      {{ getUserInitials(user.name || user.username) }}
                    </el-avatar>
                  </div>
                  <div class="user-info">
                    <div class="user-name-row">
                      <span class="user-name">{{ user.name || '-' }}</span>
                      <el-tag v-if="checkedUserIds.includes(user.id)" size="small" type="success" class="user-tag">
                        已关联
                      </el-tag>
                    </div>
                    <div class="user-meta">
                      <span class="user-username">
                        <el-icon><User /></el-icon>
                        {{ user.username }}
                      </span>
                      <span v-if="user.email" class="user-email">
                        <el-icon><Message /></el-icon>
                        {{ user.email }}
                      </span>
                    </div>
                  </div>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>

          <div v-if="filteredUsers.length === 0" class="empty-users">
            <el-empty 
              :description="userSearchKeyword ? '未找到匹配的用户' : '暂无用户数据'" 
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="footer-info">
            已选择 <strong>{{ checkedUserIds.length }}</strong> 个用户
          </div>
          <div class="footer-actions">
            <el-button @click="userBindDialogVisible = false" size="large">取消</el-button>
            <el-button type="primary" @click="handleBindUsers" :loading="loading" size="large">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.role-management-view {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);

  // 统计卡片
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 20px;

    .stat-card {
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
      border-radius: 16px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.8);
      transition: all 0.3s ease;
      backdrop-filter: blur(10px);

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .stat-icon {
        font-size: 32px;
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        flex-shrink: 0;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #303133;
          line-height: 1.2;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }

      &.stat-enabled .stat-icon {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      }

      &.stat-disabled .stat-icon {
        background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
      }

      &.stat-admin .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
    }
  }

  .main-card {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 4px;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;

    .card-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .add-button {
      border-radius: 12px;
      padding: 10px 20px;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
      }
    }
  }

  // 工具栏
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background: rgba(248, 250, 252, 0.8);
    border-radius: 12px;
    border: 1px solid rgba(226, 232, 240, 0.8);

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex-wrap: wrap;

      .search-input {
        :deep(.el-input__wrapper) {
          border-radius: 10px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        }
      }

      .filter-select {
        :deep(.el-input__wrapper) {
          border-radius: 10px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        }
      }
    }

    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }

  // 操作按钮
  .action-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  // 表格样式优化
  .table-wrapper {
    border-radius: 12px;
    overflow: hidden;
  }

  .role-table {
    :deep(.el-table__header) {
      th {
        background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
        color: #1e293b;
        font-weight: 600;
        border-bottom: 2px solid #e2e8f0;
      }
    }

    :deep(.el-table__row) {
      transition: all 0.2s ease;

      &:hover {
        background-color: rgba(102, 126, 234, 0.05) !important;
        transform: scale(1.01);
      }
    }

    :deep(.el-table__cell) {
      padding: 16px 12px;
    }
  }

  .menu-bind-tree {
    max-height: 500px;
    overflow-y: auto;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 12px;

    .menu-tree-node {
      display: flex;
      align-items: center;
      gap: 8px;

      .menu-icon {
        font-size: 16px;
        color: #667eea;
      }

      .menu-name {
        font-weight: 500;
        color: #1e293b;
      }

      .menu-path {
        color: #64748b;
        font-size: 12px;
        margin-left: 8px;
        font-family: 'Monaco', 'Courier New', monospace;
      }
    }
  }

  // 对话框样式优化
  // 对话框全局样式
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);

    .el-dialog__header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px 24px;
      margin: 0;

      .el-dialog__title {
        color: #fff;
        font-size: 18px;
        font-weight: 600;
      }

      .el-dialog__headerbtn {
        .el-dialog__close {
          color: #fff;
          font-size: 20px;

          &:hover {
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }
    }

    .el-dialog__body {
      padding: 24px;
      background: #fff;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      background: #f8fafc;
      border-top: 1px solid #e2e8f0;
    }
  }

  // 角色表单对话框
  .role-form-dialog {
    :deep(.el-form) {
      .el-form-item {
        margin-bottom: 24px;

        .el-form-item__label {
          font-weight: 500;
          color: #1e293b;
        }

        .el-input__wrapper,
        .el-textarea__inner {
          border-radius: 8px;
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
          }

          &.is-focus {
            box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
          }
        }
      }

      .role-type-radio,
      .status-radio {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;

        .radio-item {
          margin-right: 0;
          padding: 8px 16px;
          border: 2px solid #e2e8f0;
          border-radius: 8px;
          transition: all 0.3s ease;

          &:hover {
            border-color: #667eea;
            background: rgba(102, 126, 234, 0.05);
          }

          &.is-checked {
            border-color: #667eea;
            background: rgba(102, 126, 234, 0.1);
          }

          :deep(.el-radio__input) {
            margin-right: 8px;
          }

          :deep(.el-radio__label) {
            padding-left: 0;
          }
        }
      }
    }
  }

  // 菜单绑定对话框
  .menu-bind-dialog {
    .dialog-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;

      .footer-info {
        font-size: 14px;
        color: #606266;

        strong {
          color: #409eff;
          font-size: 16px;
          margin: 0 4px;
        }
      }
    }
  }

  // 对话框底部统一样式
  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    .footer-info {
      font-size: 14px;
      color: #606266;

      strong {
        color: #409eff;
        font-size: 16px;
        margin: 0 4px;
      }
    }

    .footer-actions {
      display: flex;
      gap: 12px;
    }
  }

  .user-bind-dialog {
    .user-bind-content {
      .user-bind-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid #ebeef5;
        margin-bottom: 16px;

        .user-stats {
          display: flex;
          gap: 24px;
          align-items: center;

          .stat-item {
            font-size: 14px;
            color: #606266;

            strong {
              color: #303133;
              margin-left: 4px;
            }

            .stat-highlight {
              color: #409eff;
            }
          }
        }

        .user-actions {
          .check-all-box {
            font-weight: 500;
          }
        }
      }

      .user-search-bar {
        margin-bottom: 16px;

        .search-input {
          width: 100%;
        }
      }

      .user-list-container {
        max-height: 500px;
        overflow-y: auto;
        border: 1px solid #ebeef5;
        border-radius: 4px;
        padding: 8px;

        .user-checkbox-group {
          display: flex;
          flex-direction: column;
          gap: 8px;

          .user-card {
            border: 1px solid #ebeef5;
            border-radius: 6px;
            padding: 12px;
            transition: all 0.3s;
            background-color: #fafafa;

            &:hover {
              background-color: #f0f9ff;
              border-color: #409eff;
              box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
            }

            .user-checkbox {
              width: 100%;

              :deep(.el-checkbox__label) {
                width: 100%;
                padding-left: 8px;
              }
            }

            .user-item {
              display: flex;
              align-items: center;
              gap: 12px;
              width: 100%;

              .user-avatar {
                flex-shrink: 0;
              }

              .user-info {
                flex: 1;
                min-width: 0;

                .user-name-row {
                  display: flex;
                  align-items: center;
                  gap: 8px;
                  margin-bottom: 6px;

                  .user-name {
                    font-weight: 500;
                    font-size: 15px;
                    color: #303133;
                    flex: 1;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }

                  .user-tag {
                    flex-shrink: 0;
                  }
                }

                .user-meta {
                  display: flex;
                  flex-wrap: wrap;
                  gap: 16px;
                  font-size: 13px;
                  color: #909399;

                  .user-username,
                  .user-email {
                    display: flex;
                    align-items: center;
                    gap: 4px;

                    .el-icon {
                      font-size: 14px;
                    }
                  }
                }
              }
            }
          }
        }

        .empty-users {
          padding: 60px 0;
          text-align: center;
        }
      }
    }
  }
}

/* iPad适配 (768px - 1024px) */
@media (min-width: 768px) and (max-width: 1024px) {
  .role-management-view {
    padding: 16px;

    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }

    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .card-header .el-button {
      width: 100%;
    }

    .toolbar {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;

      .toolbar-left {
        width: 100%;
      }
    }

    .table-wrapper {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }

    .role-table {
      min-width: 800px;
    }
  }

  :deep(.el-dialog) {
    width: 90% !important;
    max-width: 800px;
  }

  :deep(.el-table__cell) {
    padding: 10px 8px;
    font-size: 13px;
  }
}

/* 手机适配 (< 768px) */
@media (max-width: 768px) {
  .role-management-view {
    padding: 10px;

    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .stat-card {
        padding: 16px;

        .stat-icon {
          width: 50px;
          height: 50px;
          font-size: 24px;
        }

        .stat-content .stat-value {
          font-size: 22px;
        }
      }
    }

    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .card-header .el-button {
      width: 100%;
    }

    .toolbar {
      flex-direction: column;
      align-items: stretch;
      gap: 12px;
      padding: 12px;

      .toolbar-left {
        width: 100%;
        flex-direction: column;

        .search-input,
        .filter-select {
          width: 100% !important;
        }
      }
    }

    .table-wrapper {
      margin: 0 -10px;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }

    .role-table {
      min-width: 700px;
    }

    .action-buttons {
      flex-direction: column;
      gap: 4px;
    }
  }

  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto;
    border-radius: 12px !important;

    .el-dialog__header {
      padding: 16px !important;

      .el-dialog__title {
        font-size: 16px !important;
      }
    }

    .el-dialog__body {
      padding: 16px !important;
    }

    .el-dialog__footer {
      padding: 12px 16px !important;
    }
  }

  :deep(.el-table__cell) {
    padding: 8px 6px;
    font-size: 12px;
  }

  :deep(.el-table__header-wrapper th) {
    padding: 8px 6px;
    font-size: 12px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
  }

  :deep(.el-table__body tr:hover > td) {
    background: transparent !important;
  }

  .menu-bind-tree {
    max-height: 400px;
  }
}
</style>

