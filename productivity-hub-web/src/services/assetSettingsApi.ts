import { request } from './http'
import type { AssetSettings, AssetSettingsUpdateDTO } from '@/types/assetSettings'

/**
 * 资产设置API
 */
export const assetSettingsApi = {
  /**
   * 获取资产设置
   */
  getAssetSettings: () =>
    request<AssetSettings>({
      url: '/api/settings/asset',
      method: 'GET',
    }),

  /**
   * 更新资产设置
   */
  updateAssetSettings: (data: AssetSettingsUpdateDTO) =>
    request<AssetSettings>({
      url: '/api/settings/asset',
      method: 'POST',
      data,
    }),
}

