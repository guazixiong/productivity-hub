<script setup lang="ts">
/**
 * ACL菜单管理页面
 * TASK-REQ-001-05: 前端菜单管理页 - 树/表格编辑、显隐/排序、校验、空态
 */
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Rank, Menu as MenuIcon, Edit, Delete, Plus as PlusIcon, Folder, Document } from '@element-plus/icons-vue'
import { useDevice } from '@/composables/useDevice'
import { aclMenuApi } from '@/services/api'
import type { AclMenuTreeVO, AclMenuVO, AclMenuCreateDTO, AclMenuUpdateDTO } from '@/types/acl'
import { MenuType, MenuVisible, AclStatus, MenuTypeDesc, MenuVisibleDesc, AclStatusDesc } from '@/types/acl'
import { getIconComponent } from '@/utils/iconMapper'

const { isMobile, isTablet } = useDevice()

const menuTree = ref<AclMenuTreeVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const expandedKeys = ref<number[]>([])

const formModel = reactive<AclMenuCreateDTO & { id?: number }>({
  id: undefined,
  parentId: null,
  name: '',
  path: null,
  component: null,
  icon: null,
  type: MenuType.MENU,
  visible: MenuVisible.VISIBLE,
  orderNum: 0,
  status: AclStatus.ENABLED,
  idempotentKey: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  visible: [{ required: true, message: '请选择可见性', trigger: 'change' }],
  orderNum: [{ type: 'number', message: '排序号必须为数字', trigger: 'blur' }],
}

// 扁平化菜单树，用于表格显示
const flatMenus = computed(() => {
  const flatten = (menus: AclMenuTreeVO[], level = 0): Array<AclMenuTreeVO & { level: number }> => {
    const result: Array<AclMenuTreeVO & { level: number }> = []
    menus.forEach(menu => {
      result.push({ ...menu, level })
      if (menu.children && menu.children.length > 0) {
        result.push(...flatten(menu.children, level + 1))
      }
    })
    return result
  }
  return flatten(menuTree.value)
})

const fetchMenus = async () => {
  loading.value = true
  try {
    menuTree.value = await aclMenuApi.getTree()
    // 默认展开所有节点
    expandedKeys.value = getAllMenuIds(menuTree.value)
  } catch (error) {
    ElMessage.error((error as Error).message || '获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

const getAllMenuIds = (menus: AclMenuTreeVO[]): number[] => {
  const ids: number[] = []
  menus.forEach(menu => {
    ids.push(menu.id)
    if (menu.children && menu.children.length > 0) {
      ids.push(...getAllMenuIds(menu.children))
    }
  })
  return ids
}

const openDialog = (menu?: AclMenuTreeVO, parentId?: number | null) => {
  resetForm()
  if (menu) {
    // 编辑模式
    isEdit.value = true
    formModel.id = menu.id
    formModel.parentId = menu.parentId
    formModel.name = menu.name
    formModel.path = menu.path || null
    formModel.component = menu.component || null
    formModel.icon = menu.icon || null
    formModel.type = menu.type
    formModel.visible = menu.visible
    formModel.orderNum = menu.orderNum
    formModel.status = menu.status
  } else {
    // 创建模式
    isEdit.value = false
    formModel.parentId = parentId !== undefined ? parentId : null
    formModel.idempotentKey = `menu_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
  dialogVisible.value = true
}

const resetForm = () => {
  formModel.id = undefined
  formModel.parentId = null
  formModel.name = ''
  formModel.path = null
  formModel.component = null
  formModel.icon = null
  formModel.type = MenuType.MENU
  formModel.visible = MenuVisible.VISIBLE
  formModel.orderNum = 0
  formModel.status = AclStatus.ENABLED
  formModel.idempotentKey = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value) {
      const updateDTO: AclMenuUpdateDTO = {
        id: formModel.id!,
        parentId: formModel.parentId,
        name: formModel.name,
        path: formModel.path,
        component: formModel.component,
        icon: formModel.icon,
        type: formModel.type,
        visible: formModel.visible,
        orderNum: formModel.orderNum,
        status: formModel.status,
      }
      await aclMenuApi.update(updateDTO)
      ElMessage.success('菜单更新成功')
    } else {
      const createDTO: AclMenuCreateDTO = {
        parentId: formModel.parentId,
        name: formModel.name,
        path: formModel.path,
        component: formModel.component,
        icon: formModel.icon,
        type: formModel.type,
        visible: formModel.visible,
        orderNum: formModel.orderNum,
        status: formModel.status,
        idempotentKey: formModel.idempotentKey,
      }
      await aclMenuApi.create(createDTO)
      ElMessage.success('菜单创建成功')
    }
    dialogVisible.value = false
    await fetchMenus()
  } catch (error) {
    ElMessage.error((error as Error).message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    loading.value = false
  }
}

const handleDelete = async (menu: AclMenuTreeVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除菜单 "${menu.name}" 吗？${menu.children && menu.children.length > 0 ? '删除后其子菜单也将被删除。' : ''}`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    loading.value = true
    try {
      await aclMenuApi.delete(menu.id)
      ElMessage.success('菜单已删除')
      await fetchMenus()
    } catch (error) {
      ElMessage.error((error as Error).message || '删除失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消删除
  }
}

// 根据ID查找菜单
const findMenuById = (id: number | null, menus: AclMenuTreeVO[]): AclMenuTreeVO | null => {
  if (id === null) return null
  for (const menu of menus) {
    if (menu.id === id) {
      return menu
    }
    if (menu.children && menu.children.length > 0) {
      const found = findMenuById(id, menu.children)
      if (found) return found
    }
  }
  return null
}

const getParentMenuOptions = (excludeId?: number, currentParentId?: number | null): Array<{ label: string; value: number | null }> => {
  const options: Array<{ label: string; value: number | null }> = [
    { label: '根菜单', value: null }
  ]
  
  const addedIds = new Set<number | null>([null])
  
  const addMenu = (menus: AclMenuTreeVO[], prefix = '') => {
    menus.forEach(menu => {
      if (menu.id !== excludeId && menu.type === MenuType.DIR) {
        if (!addedIds.has(menu.id)) {
          options.push({ label: `${prefix}${menu.name}`, value: menu.id })
          addedIds.add(menu.id)
        }
        if (menu.children && menu.children.length > 0) {
          addMenu(menu.children, `${prefix}  └─ `)
        }
      }
    })
  }
  
  addMenu(menuTree.value)
  
  // 如果当前菜单有父菜单，但父菜单不在选项中（可能是MENU类型），需要添加它
  if (currentParentId !== null && currentParentId !== undefined) {
    const parentMenu = findMenuById(currentParentId, menuTree.value)
    if (parentMenu && !addedIds.has(currentParentId)) {
      // 即使父菜单是MENU类型，也要添加到选项中，以便正确回显
      options.push({ label: parentMenu.name, value: parentMenu.id })
      addedIds.add(currentParentId)
    }
  }
  
  return options
}

// 判断是否允许拖拽
const allowDrag = (node: any) => {
  return true // 所有节点都可以拖拽
}

// 判断是否允许放置
const allowDrop = (draggingNode: any, dropNode: any, type: 'prev' | 'inner' | 'next') => {
  // 不允许拖拽到自身
  if (draggingNode.data.id === dropNode.data.id) {
    return false
  }
  
  // 不允许将节点拖拽到自己的子节点中
  const isDescendant = (parent: any, child: any): boolean => {
    if (!parent.children || parent.children.length === 0) {
      return false
    }
    for (const item of parent.children) {
      if (item.id === child.data.id) {
        return true
      }
      if (item.children && item.children.length > 0) {
        if (isDescendant(item, child)) {
          return true
        }
      }
    }
    return false
  }
  
  if (type === 'inner' && isDescendant(draggingNode.data, dropNode)) {
    return false
  }
  
  return true
}

// 处理节点拖拽完成
const handleNodeDrop = async (draggingNode: any, dropNode: any, dropType: 'before' | 'after' | 'inner', ev: DragEvent) => {
  try {
    loading.value = true
    
    // Element Plus 的 el-tree 在拖拽后已经自动更新了树结构
    // 我们只需要重新计算整个菜单树的 orderNum 和 parentId，然后批量更新
    
    const updatePromises: Promise<any>[] = []
    
    // 递归更新菜单的 orderNum 和 parentId
    const updateMenuTree = (menus: AclMenuTreeVO[], parentId: number | null) => {
      menus.forEach((menu, index) => {
        const newOrderNum = index + 1
        const newParentId = parentId
        
        // 如果 parentId 或 orderNum 发生变化，需要更新
        if (menu.parentId !== newParentId || menu.orderNum !== newOrderNum) {
          updatePromises.push(
            aclMenuApi.update({
              id: menu.id,
              parentId: newParentId,
              name: menu.name,
              path: menu.path,
              component: menu.component,
              icon: menu.icon,
              type: menu.type,
              visible: menu.visible,
              orderNum: newOrderNum,
              status: menu.status,
            })
          )
          // 更新本地数据
          menu.parentId = newParentId
          menu.orderNum = newOrderNum
        }
        
        // 递归处理子菜单
        if (menu.children && menu.children.length > 0) {
          updateMenuTree(menu.children, menu.id)
        }
      })
    }
    
    // 更新整个菜单树
    updateMenuTree(menuTree.value, null)
    
    // 执行所有更新
    if (updatePromises.length > 0) {
      await Promise.all(updatePromises)
      ElMessage.success('菜单排序已更新')
    }
    
    // 重新获取菜单树以确保数据同步
    await fetchMenus()
  } catch (error) {
    ElMessage.error((error as Error).message || '更新菜单排序失败')
    // 如果更新失败，重新获取菜单树恢复原状
    await fetchMenus()
  } finally {
    loading.value = false
  }
}

onMounted(fetchMenus)
</script>

<template>
  <div class="menu-management-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon class="header-icon"><MenuIcon /></el-icon>
            <span class="title-text">菜单管理</span>
            <el-tag v-if="menuTree.length > 0" size="small" type="info" class="menu-count">
              {{ getAllMenuIds(menuTree).length }} 个菜单
            </el-tag>
          </div>
          <el-button type="primary" @click="openDialog()" :disabled="loading" class="add-button">
            <el-icon><PlusIcon /></el-icon>
            新增菜单
          </el-button>
        </div>
      </template>

      <!-- 空态 -->
      <div v-if="!loading && menuTree.length === 0" class="empty-state">
        <el-empty description="暂无菜单数据">
          <el-button type="primary" @click="openDialog()">
            <el-icon><PlusIcon /></el-icon>
            创建第一个菜单
          </el-button>
        </el-empty>
      </div>

      <!-- 菜单树 -->
      <el-tree
        v-else
        v-loading="loading"
        :data="menuTree"
        :props="{ children: 'children', label: 'name' }"
        :expand-on-click-node="false"
        :default-expand-all="true"
        node-key="id"
        draggable
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        @node-drop="handleNodeDrop"
        class="menu-tree"
      >
        <template #default="{ node, data }">
          <div class="menu-tree-node" :class="{ 'node-disabled': data.status === AclStatus.DISABLED }">
            <div class="menu-info">
              <el-icon class="drag-handle" title="拖拽调整顺序">
                <Rank />
              </el-icon>
              <div class="menu-icon-wrapper">
                <el-icon v-if="data.icon && getIconComponent(data.icon)" class="menu-icon">
                  <component :is="getIconComponent(data.icon)!" />
                </el-icon>
                <el-icon v-else class="menu-icon default-icon">
                  <component :is="data.type === MenuType.DIR ? Folder : Document" />
                </el-icon>
              </div>
              <div class="menu-content">
                <div class="menu-title-row">
                  <span class="menu-name">{{ data.name }}</span>
                  <div class="menu-badges">
                    <el-tag 
                      size="small" 
                      :type="data.type === MenuType.DIR ? 'info' : 'success'"
                      class="type-badge"
                    >
                      {{ MenuTypeDesc[data.type] }}
                    </el-tag>
                    <el-tag 
                      size="small" 
                      :type="data.visible === MenuVisible.VISIBLE ? 'success' : 'warning'"
                      class="visible-badge"
                      effect="plain"
                    >
                      {{ MenuVisibleDesc[data.visible] }}
                    </el-tag>
                    <el-tag 
                      size="small" 
                      :type="data.status === AclStatus.ENABLED ? 'success' : 'danger'"
                      class="status-badge"
                      effect="plain"
                    >
                      {{ AclStatusDesc[data.status] }}
                    </el-tag>
                  </div>
                </div>
                <div class="menu-meta" v-if="data.path || data.component">
                  <span class="menu-path" v-if="data.path">
                    <el-icon class="meta-icon"><Document /></el-icon>
                    {{ data.path }}
                  </span>
                  <span class="menu-component" v-if="data.component">
                    <el-icon class="meta-icon"><Folder /></el-icon>
                    {{ data.component }}
                  </span>
                </div>
              </div>
            </div>
            <div class="menu-actions">
              <el-button 
                size="small" 
                type="primary" 
                :icon="Edit"
                circle
                @click="openDialog(data)"
                title="编辑"
              />
              <el-button 
                size="small" 
                type="success" 
                :icon="PlusIcon"
                circle
                @click="openDialog(undefined, data.id)"
                title="添加子菜单"
              />
              <el-button 
                size="small" 
                type="danger" 
                :icon="Delete"
                circle
                @click="handleDelete(data)"
                title="删除"
              />
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 菜单表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑菜单' : '新增菜单'"
      width="680px"
      :close-on-click-modal="false"
      class="menu-dialog"
    >
      <el-form
        ref="formRef"
        :model="formModel"
        :rules="rules"
        label-width="110px"
        label-position="left"
        class="menu-form"
      >
        <el-form-item label="父菜单" prop="parentId">
          <el-select
            v-model="formModel.parentId"
            placeholder="请选择父菜单（留空为根菜单）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="option in getParentMenuOptions(formModel.id, formModel.parentId)"
              :key="option.value === null ? 'root' : `parent-${option.value}`"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="formModel.name" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="formModel.type">
            <el-radio :value="MenuType.DIR">{{ MenuTypeDesc[MenuType.DIR] }}</el-radio>
            <el-radio :value="MenuType.MENU">{{ MenuTypeDesc[MenuType.MENU] }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="路由路径" prop="path" v-if="formModel.type === MenuType.MENU">
          <el-input v-model="formModel.path" placeholder="请输入路由路径，如：/settings/menus" />
        </el-form-item>

        <el-form-item label="组件路径" prop="component" v-if="formModel.type === MenuType.MENU">
          <el-input v-model="formModel.component" placeholder="请输入组件路径，如：views/settings/MenuManagementView" />
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input 
            v-model="formModel.icon" 
            placeholder="请输入图标名称（Element Plus图标）"
            clearable
          >
            <template #prefix>
              <el-icon v-if="formModel.icon && getIconComponent(formModel.icon)">
                <component :is="getIconComponent(formModel.icon)!" />
              </el-icon>
            </template>
          </el-input>
          <div class="form-tip">例如：Menu、Setting、User 等 Element Plus 图标名称</div>
        </el-form-item>

        <el-form-item label="可见性" prop="visible">
          <el-radio-group v-model="formModel.visible">
            <el-radio :value="MenuVisible.VISIBLE">{{ MenuVisibleDesc[MenuVisible.VISIBLE] }}</el-radio>
            <el-radio :value="MenuVisible.HIDDEN">{{ MenuVisibleDesc[MenuVisible.HIDDEN] }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序号" prop="orderNum">
          <el-input-number v-model="formModel.orderNum" :min="0" :max="9999" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio :value="AclStatus.ENABLED">{{ AclStatusDesc[AclStatus.ENABLED] }}</el-radio>
            <el-radio :value="AclStatus.DISABLED">{{ AclStatusDesc[AclStatus.DISABLED] }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="default">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading" size="default">
            {{ isEdit ? '保存修改' : '创建菜单' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.menu-management-view {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  :deep(.el-card) {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: none;
  }

  :deep(.el-card__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px 12px 0 0;
    padding: 20px 24px;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      display: flex;
      align-items: center;
      gap: 12px;

      .header-icon {
        font-size: 24px;
        color: #fff;
      }

      .title-text {
        font-size: 20px;
        font-weight: 600;
        color: #fff;
        letter-spacing: 0.5px;
      }

      .menu-count {
        background: rgba(255, 255, 255, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #fff;
        font-weight: 500;
      }
    }

    .add-button {
      background: #fff;
      color: #667eea;
      border: none;
      font-weight: 500;
      padding: 10px 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  .empty-state {
    padding: 60px 20px;
    text-align: center;
  }

  .menu-tree {
    margin-top: 0;
    padding: 16px 0;

    :deep(.el-tree-node__content) {
      height: auto;
      min-height: 64px;
      padding: 12px 16px;
      margin-bottom: 8px;
      border-radius: 8px;
      transition: all 0.3s;
      border: 1px solid transparent;

      &:hover {
        background: #f0f4ff;
        border-color: #c0d9ff;
        box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
      }
    }

    :deep(.el-tree-node__expand-icon) {
      color: #667eea;
      font-size: 16px;
    }

    .menu-tree-node {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-right: 8px;
      width: 100%;

      &.node-disabled {
        opacity: 0.6;
      }

      .menu-info {
        display: flex;
        align-items: center;
        gap: 12px;
        flex: 1;
        min-width: 0;

        .drag-handle {
          font-size: 18px;
          color: #909399;
          cursor: move;
          cursor: grab;
          transition: all 0.2s;
          flex-shrink: 0;

          &:hover {
            color: #667eea;
            transform: scale(1.1);
          }

          &:active {
            cursor: grabbing;
            transform: scale(0.95);
          }
        }

        .menu-icon-wrapper {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 36px;
          height: 36px;
          border-radius: 8px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          flex-shrink: 0;

          .menu-icon {
            font-size: 18px;
            color: #fff;
          }

          .default-icon {
            font-size: 18px;
            color: #fff;
          }
        }

        .menu-content {
          flex: 1;
          min-width: 0;
          display: flex;
          flex-direction: column;
          gap: 6px;

          .menu-title-row {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;

            .menu-name {
              font-weight: 600;
              font-size: 15px;
              color: #303133;
              flex-shrink: 0;
            }

            .menu-badges {
              display: flex;
              gap: 6px;
              flex-wrap: wrap;

              .type-badge,
              .visible-badge,
              .status-badge {
                font-size: 11px;
                padding: 2px 8px;
                border-radius: 4px;
              }
            }
          }

          .menu-meta {
            display: flex;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
            font-size: 12px;
            color: #909399;

            .menu-path,
            .menu-component {
              display: flex;
              align-items: center;
              gap: 4px;

              .meta-icon {
                font-size: 14px;
              }
            }
          }
        }
      }

      .menu-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
        margin-left: 12px;

        .el-button {
          width: 32px;
          height: 32px;
          padding: 0;
          transition: all 0.2s;

          &:hover {
            transform: scale(1.1);
          }

          &:active {
            transform: scale(0.95);
          }
        }
      }
    }
  }

  .menu-dialog {
    :deep(.el-dialog__header) {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px 24px;
      border-radius: 8px 8px 0 0;

      .el-dialog__title {
        color: #fff;
        font-weight: 600;
        font-size: 18px;
      }

      .el-dialog__headerbtn .el-dialog__close {
        color: #fff;
        font-size: 20px;

        &:hover {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }

    :deep(.el-dialog__body) {
      padding: 24px;
      background: #fafbfc;
    }

    .menu-form {
      .el-form-item {
        margin-bottom: 20px;

        .el-form-item__label {
          font-weight: 500;
          color: #606266;
        }
      }

      .form-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
        line-height: 1.5;
      }

      :deep(.el-input),
      :deep(.el-select),
      :deep(.el-input-number) {
        .el-input__wrapper {
          border-radius: 6px;
          box-shadow: 0 0 0 1px #dcdfe6 inset;
          transition: all 0.2s;

          &:hover {
            box-shadow: 0 0 0 1px #c0c4cc inset;
          }

          &.is-focus {
            box-shadow: 0 0 0 1px #667eea inset;
          }
        }
      }

      :deep(.el-radio-group) {
        .el-radio {
          margin-right: 24px;

          .el-radio__label {
            font-weight: 500;
          }
        }
      }
    }

    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;

      .el-button {
        min-width: 100px;
        border-radius: 6px;
        font-weight: 500;
      }
    }
  }
}

/* iPad适配 (768px - 1024px) */
@media (min-width: 768px) and (max-width: 1024px) {
  .menu-management-view {
    padding: 16px;

    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .header-title {
        width: 100%;
      }

      .add-button {
        width: 100%;
      }
    }

    .menu-tree {
      :deep(.el-tree-node__content) {
        padding: 10px 12px;
      }

      .menu-tree-node {
        .menu-info {
          .menu-content {
            .menu-title-row {
              .menu-badges {
                gap: 4px;
              }
            }
          }
        }

        .menu-actions {
          margin-left: 8px;
          gap: 6px;
        }
      }
    }
  }

  :deep(.menu-dialog) {
    width: 90% !important;
    max-width: 680px;
  }
}

/* 手机适配 (< 768px) */
@media (max-width: 768px) {
  .menu-management-view {
    padding: 12px;
    background: #f5f7fa;

    :deep(.el-card__header) {
      padding: 16px;
    }

    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .header-title {
        width: 100%;

        .title-text {
          font-size: 18px;
        }
      }

      .add-button {
        width: 100%;
        padding: 10px 16px;
      }
    }

    .menu-tree {
      padding: 12px 0;

      :deep(.el-tree-node__content) {
        padding: 10px 12px;
        margin-bottom: 6px;
      }

      .menu-tree-node {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;

        .menu-info {
          width: 100%;
          gap: 10px;

          .drag-handle {
            font-size: 16px;
          }

          .menu-icon-wrapper {
            width: 32px;
            height: 32px;

            .menu-icon {
              font-size: 16px;
            }
          }

          .menu-content {
            flex: 1;
            min-width: 0;

            .menu-title-row {
              gap: 8px;

              .menu-name {
                font-size: 14px;
              }

              .menu-badges {
                gap: 4px;

                .type-badge,
                .visible-badge,
                .status-badge {
                  font-size: 10px;
                  padding: 2px 6px;
                }
              }
            }

            .menu-meta {
              font-size: 11px;
              gap: 12px;
              flex-wrap: wrap;
            }
          }
        }

        .menu-actions {
          width: 100%;
          justify-content: flex-end;
          margin-left: 0;
          gap: 8px;

          .el-button {
            width: 36px;
            height: 36px;
          }
        }
      }
    }
  }

  :deep(.menu-dialog) {
    width: 95% !important;
    margin: 5vh auto;

    .el-dialog__header {
      padding: 16px 20px;
    }

    .el-dialog__body {
      padding: 20px 16px;
    }

    .menu-form {
      .el-form-item {
        margin-bottom: 18px;

        .el-form-item__label {
          font-size: 13px;
          font-weight: 500;
        }
      }

      .form-tip {
        font-size: 11px;
      }
    }

    .dialog-footer {
      padding-top: 12px;

      .el-button {
        min-width: 80px;
        font-size: 14px;
      }
    }
  }
}
</style>

