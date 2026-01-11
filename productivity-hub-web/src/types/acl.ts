/**
 * ACL权限控制相关类型定义
 * 与后端Java枚举对齐
 * 
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */

/**
 * 角色类型枚举
 */
export enum RoleType {
  /** 管理员 */
  ADMIN = 'ADMIN',
  /** 普通用户 */
  USER = 'USER',
  /** 自定义 */
  CUSTOM = 'CUSTOM'
}

/**
 * 角色类型描述映射
 */
export const RoleTypeDesc: Record<RoleType, string> = {
  [RoleType.ADMIN]: '管理员',
  [RoleType.USER]: '普通用户',
  [RoleType.CUSTOM]: '自定义'
}

/**
 * 菜单类型枚举
 */
export enum MenuType {
  /** 目录 */
  DIR = 'DIR',
  /** 菜单 */
  MENU = 'MENU'
}

/**
 * 菜单类型描述映射
 */
export const MenuTypeDesc: Record<MenuType, string> = {
  [MenuType.DIR]: '目录',
  [MenuType.MENU]: '菜单'
}

/**
 * 菜单可见性枚举
 */
export enum MenuVisible {
  /** 隐藏 */
  HIDDEN = 0,
  /** 显示 */
  VISIBLE = 1
}

/**
 * 菜单可见性描述映射
 */
export const MenuVisibleDesc: Record<MenuVisible, string> = {
  [MenuVisible.HIDDEN]: '隐藏',
  [MenuVisible.VISIBLE]: '显示'
}

/**
 * ACL状态枚举
 */
export enum AclStatus {
  /** 启用 */
  ENABLED = 'ENABLED',
  /** 禁用 */
  DISABLED = 'DISABLED'
}

/**
 * ACL状态描述映射
 */
export const AclStatusDesc: Record<AclStatus, string> = {
  [AclStatus.ENABLED]: '启用',
  [AclStatus.DISABLED]: '禁用'
}

/**
 * 菜单树节点VO
 */
export interface AclMenuTreeVO {
  id: number
  parentId: number | null
  name: string
  path: string | null
  component: string | null
  icon: string | null
  type: MenuType
  visible: MenuVisible
  orderNum: number
  status: AclStatus
  children?: AclMenuTreeVO[]
}

/**
 * 菜单VO
 */
export interface AclMenuVO {
  id: number
  parentId: number | null
  name: string
  path: string | null
  component: string | null
  icon: string | null
  type: MenuType
  visible: MenuVisible
  orderNum: number
  status: AclStatus
  createdAt: string
  updatedAt: string
}

/**
 * 角色VO
 */
export interface AclRoleVO {
  id: number
  name: string
  type: RoleType
  status: AclStatus
  remark: string | null
  menuIds: number[]
  createdAt: string
  updatedAt: string
}

/**
 * 角色创建DTO
 */
export interface AclRoleCreateDTO {
  name: string
  type: RoleType
  status?: AclStatus
  remark?: string
  idempotentKey?: string
}

/**
 * 角色更新DTO
 */
export interface AclRoleUpdateDTO {
  id: number
  name?: string
  status?: AclStatus
  remark?: string
}

/**
 * 角色-菜单绑定DTO
 */
export interface AclRoleMenuBindDTO {
  roleId: string
  menuIds: number[]
}

/**
 * 用户-角色绑定DTO
 */
export interface AclUserRoleBindDTO {
  userId: string
  roleIds: number[]
}

/**
 * 菜单创建DTO
 */
export interface AclMenuCreateDTO {
  parentId?: number | null
  name: string
  path?: string | null
  component?: string | null
  icon?: string | null
  type: MenuType
  visible: MenuVisible
  orderNum?: number
  status?: AclStatus
  idempotentKey?: string
}

/**
 * 菜单更新DTO
 */
export interface AclMenuUpdateDTO {
  id: number
  parentId?: number | null
  name?: string
  path?: string | null
  component?: string | null
  icon?: string | null
  type?: MenuType
  visible?: MenuVisible
  orderNum?: number
  status?: AclStatus
}

