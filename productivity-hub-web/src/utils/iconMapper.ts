/**
 * 图标映射工具
 * 将图标名称字符串映射到 Element Plus 图标组件
 */
import type { Component } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

// 图标名称到组件的映射
const iconMap = new Map<string, Component>()

// 初始化图标映射
Object.keys(ElementPlusIcons).forEach((key) => {
  iconMap.set(key, (ElementPlusIcons as any)[key])
})

/**
 * 根据图标名称获取图标组件
 * @param iconName 图标名称（如 'Folder', 'Edit' 等）
 * @returns 图标组件，如果不存在则返回 null
 */
export function getIconComponent(iconName?: string): Component | null {
  if (!iconName) return null
  return iconMap.get(iconName) || null
}

/**
 * 获取所有可用的图标名称列表
 * @returns 图标名称数组
 */
export function getAvailableIconNames(): string[] {
  return Array.from(iconMap.keys()).sort()
}

/**
 * 检查图标名称是否存在
 * @param iconName 图标名称
 * @returns 是否存在
 */
export function hasIcon(iconName: string): boolean {
  return iconMap.has(iconName)
}

/**
 * 获取常用图标列表（用于图标选择器）
 */
export function getCommonIcons(): Array<{ name: string; component: Component }> {
  const commonIconNames = [
    'Folder',
    'FolderOpened',
    'Document',
    'Picture',
    'VideoCamera',
    'Music',
    'Goods',
    'Shop',
    'ShoppingCart',
    'Money',
    'CreditCard',
    'Wallet',
    'House',
    'OfficeBuilding',
    'School',
    'Car',
    'Bicycle',
    'Phone',
    'Monitor',
    'Laptop',
    'Printer',
    'Camera',
    'Headset',
    'Watch',
    'Box',
    'Collection',
    'Files',
    'FolderAdd',
    'FolderDelete',
    'Edit',
    'EditPen',
    'Delete',
    'Plus',
    'Minus',
    'Search',
    'Setting',
    'Tools',
    'Operation',
    'Connection',
    'Switch',
    'Timer',
    'Clock',
    'Calendar',
    'Bell',
    'Message',
    'ChatDotRound',
    'User',
    'UserFilled',
    'Lock',
    'Key',
    'Star',
    'StarFilled',
    'CollectionTag',
    'Promotion',
    'Food',
    'Coffee',
    'Cup',
    'Location',
    'LocationFilled',
    'MapLocation',
    'Flag',
    'Trophy',
    'Medal',
    'TrophyBase',
    'TrendCharts',
    'DataAnalysis',
    'DataLine',
    'PieChart',
    'Histogram',
    'Grid',
    'Menu',
    'List',
    'Grid',
    'Refresh',
    'RefreshRight',
    'RefreshLeft',
    'Download',
    'Upload',
    'UploadFilled',
    'Share',
    'Link',
    'CopyDocument',
    'DocumentCopy',
    'View',
    'Hide',
    'Show',
    'ZoomIn',
    'ZoomOut',
    'FullScreen',
    'Aim',
    'ArrowLeft',
    'ArrowRight',
    'ArrowUp',
    'ArrowDown',
    'Top',
    'Bottom',
    'Back',
    'Right',
    'Left',
    'TopRight',
    'TopLeft',
    'BottomRight',
    'BottomLeft',
    'DArrowRight',
    'DArrowLeft',
    'Sort',
    'SortUp',
    'SortDown',
    'Rank',
    'Loading',
    'Warning',
    'WarningFilled',
    'InfoFilled',
    'CircleCheck',
    'CircleCheckFilled',
    'CircleClose',
    'CircleCloseFilled',
    'SuccessFilled',
    'Close',
    'Check',
    'CloseBold',
    'QuestionFilled',
  ]

  // 去重并过滤存在的图标
  const uniqueIconNames = Array.from(new Set(commonIconNames))
  
  return uniqueIconNames
    .filter((name) => iconMap.has(name))
    .map((name) => ({
      name,
      component: iconMap.get(name)!,
    }))
}

