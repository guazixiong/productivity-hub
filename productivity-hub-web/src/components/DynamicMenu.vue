<template>
  <template v-for="menu in sortedMenus" :key="menu.id">
    <!-- 目录类型且有子菜单，渲染为 el-sub-menu -->
    <el-sub-menu
      v-if="menu.type === 'DIR' && menu.children && menu.children.length > 0"
      :index="menu.path || `menu-${menu.id}`"
    >
      <template #title>
        <el-icon v-if="iconComponent(menu.icon)">
          <component :is="iconComponent(menu.icon)" />
        </el-icon>
        <span v-show="!isCollapsed">{{ menu.name }}</span>
      </template>
      <DynamicMenu :menus="menu.children" :is-collapsed="isCollapsed" />
    </el-sub-menu>
    
    <!-- 菜单类型，渲染为 el-menu-item -->
    <el-menu-item
      v-else
      :index="menu.path || `menu-${menu.id}`"
    >
      <el-icon v-if="iconComponent(menu.icon)">
        <component :is="iconComponent(menu.icon)" />
      </el-icon>
      <template #title>{{ menu.name }}</template>
    </el-menu-item>
  </template>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AclMenuTreeVO } from '@/types/acl'
import { getIconComponent } from '@/utils/iconMapper'
import type { Component } from 'vue'

interface Props {
  menus: AclMenuTreeVO[]
  isCollapsed?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isCollapsed: false
})

// 计算属性：获取排序后的菜单（确保按照 orderNum 排序）
const sortedMenus = computed(() => {
  const menus = props.menus || []
  return [...menus].sort((a, b) => (a.orderNum || 0) - (b.orderNum || 0))
})

// 获取图标组件
const iconComponent = (iconName?: string | null): Component | null => {
  if (!iconName) return null
  return getIconComponent(iconName)
}
</script>

