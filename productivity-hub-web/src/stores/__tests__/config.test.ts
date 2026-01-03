/**
 * 配置Store单元测试
 * TASK-TEST-03: 前端单元测试编写
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useConfigStore } from '../config'
import { configApi } from '@/services/api'

// Mock API
vi.mock('@/services/api', () => ({
  configApi: {
    list: vi.fn(),
    update: vi.fn(),
    createOrUpdate: vi.fn(),
  },
}))

describe('useConfigStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('应该正确初始化状态', () => {
    const store = useConfigStore()

    expect(store.configs).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.lastFetched).toBe(0)
    expect(store.isStale).toBe(true)
  })

  it('应该正确获取配置列表', async () => {
    const store = useConfigStore()
    const mockConfigs = [
      { id: '1', module: 'asset', key: 'key1', value: 'value1' },
      { id: '2', module: 'asset', key: 'key2', value: 'value2' },
    ]

    vi.mocked(configApi.list).mockResolvedValue(mockConfigs as any)

    await store.fetchConfigs()

    expect(store.configs).toEqual(mockConfigs)
    expect(store.loading).toBe(false)
    expect(store.lastFetched).toBeGreaterThan(0)
    expect(store.isStale).toBe(false)
  })

  it('应该使用缓存而不重新获取', async () => {
    const store = useConfigStore()
    const mockConfigs = [{ id: '1', module: 'asset', key: 'key1', value: 'value1' }]

    vi.mocked(configApi.list).mockResolvedValue(mockConfigs as any)

    // 第一次获取
    await store.fetchConfigs()
    const firstFetched = store.lastFetched

    // 第二次获取（应该使用缓存）
    await store.fetchConfigs(false)

    expect(store.lastFetched).toBe(firstFetched)
    expect(configApi.list).toHaveBeenCalledTimes(1)
  })

  it('应该强制刷新配置', async () => {
    const store = useConfigStore()
    const mockConfigs = [{ id: '1', module: 'asset', key: 'key1', value: 'value1' }]

    vi.mocked(configApi.list).mockResolvedValue(mockConfigs as any)

    await store.fetchConfigs()
    await store.fetchConfigs(true) // 强制刷新

    expect(configApi.list).toHaveBeenCalledTimes(2)
  })

  it('应该正确获取模块描述', async () => {
    const store = useConfigStore()
    const mockConfigs = [
      { id: '1', module: 'asset', key: '_module.description', value: '资产管理模块' },
      { id: '2', module: 'asset', key: 'other_key', value: 'other_value' },
    ]

    vi.mocked(configApi.list).mockResolvedValue(mockConfigs as any)

    await store.fetchConfigs()

    const description = store.getModuleDescription('asset')
    expect(description).toBe('资产管理模块')
  })

  it('应该正确更新配置', async () => {
    const store = useConfigStore()
    store.configs = [
      { id: '1', module: 'asset', key: 'key1', value: 'value1' },
    ] as any

    const updatedConfig = { id: '1', module: 'asset', key: 'key1', value: 'updated_value' }
    vi.mocked(configApi.update).mockResolvedValue(updatedConfig as any)

    await store.updateConfig({
      id: '1',
      value: 'updated_value',
    })

    expect(store.configs[0].value).toBe('updated_value')
  })

  it('应该正确重置状态', () => {
    const store = useConfigStore()
    store.configs = [{ id: '1', module: 'asset', key: 'key1', value: 'value1' }] as any
    store.lastFetched = Date.now()
    store.loading = true

    store.reset()

    expect(store.configs).toEqual([])
    expect(store.lastFetched).toBe(0)
    expect(store.loading).toBe(false)
  })
})

