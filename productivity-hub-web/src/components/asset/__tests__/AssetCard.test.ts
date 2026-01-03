/**
 * AssetCard 组件单元测试
 * TC-PAGE-REQ-002-01: 资产列表页E2E测试相关
 */

import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AssetCard from '../AssetCard.vue'
import type { AssetListItem } from '@/types/asset'
import ElementPlus from 'element-plus'

// Mock Element Plus
vi.mock('element-plus', () => ({
  default: {},
  ElCard: {
    name: 'ElCard',
    template: '<div class="el-card"><slot /></div>',
  },
  ElButton: {
    name: 'ElButton',
    template: '<button class="el-button" @click="$emit(\'click\')"><slot /></button>',
  },
  ElTag: {
    name: 'ElTag',
    template: '<span class="el-tag"><slot /></span>',
  },
  ElIcon: {
    name: 'ElIcon',
    template: '<i class="el-icon"><slot /></i>',
  },
}))

describe('AssetCard', () => {
  const mockAsset: AssetListItem = {
    id: 'asset_001',
    name: 'MacBook Pro',
    price: 12999.0,
    categoryId: 'cat_001',
    categoryName: '电子产品',
    status: 'IN_SERVICE',
    totalValue: 12999.0,
    dailyAveragePrice: 35.6,
    purchaseDate: '2025-01-01',
    image: null,
  }

  it('应该正确渲染资产名称', () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('MacBook Pro')
  })

  it('应该正确渲染分类名称', () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('电子产品')
  })

  it('应该正确渲染价格', () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('¥12,999.00')
  })

  it('应该正确渲染状态标签', () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('正在服役')
  })

  it('应该触发 view 事件当点击查看按钮', async () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    const viewButton = wrapper.findAll('button').find((btn) => btn.text().includes('查看'))
    if (viewButton) {
      await viewButton.trigger('click')
      expect(wrapper.emitted('view')).toBeTruthy()
      expect(wrapper.emitted('view')?.[0]).toEqual([mockAsset])
    }
  })

  it('应该触发 edit 事件当点击编辑按钮', async () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    const editButton = wrapper.findAll('button').find((btn) => btn.text().includes('编辑'))
    if (editButton) {
      await editButton.trigger('click')
      expect(wrapper.emitted('edit')).toBeTruthy()
      expect(wrapper.emitted('edit')?.[0]).toEqual([mockAsset])
    }
  })

  it('应该触发 delete 事件当点击删除按钮', async () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: mockAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    const deleteButton = wrapper.findAll('button').find((btn) => btn.text().includes('删除'))
    if (deleteButton) {
      await deleteButton.trigger('click')
      expect(wrapper.emitted('delete')).toBeTruthy()
      expect(wrapper.emitted('delete')?.[0]).toEqual([mockAsset])
    }
  })

  it('应该正确显示不同状态', () => {
    const retiredAsset = { ...mockAsset, status: 'RETIRED' as const }
    const wrapper = mount(AssetCard, {
      props: {
        asset: retiredAsset,
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('已退役')
  })

  it('应该显示暂无图片当没有图片时', () => {
    const wrapper = mount(AssetCard, {
      props: {
        asset: { ...mockAsset, image: null },
      },
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('暂无图片')
  })
})

