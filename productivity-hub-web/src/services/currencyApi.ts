import { request } from './http'
import type {
  Currency,
  ExchangeRate,
  CurrencyDefaultDTO,
  ExchangeRateParams,
} from '@/types/currency'

/**
 * 货币API
 */
export const currencyApi = {
  /**
   * 获取货币列表
   */
  getCurrencyList: () =>
    request<Currency[]>({
      url: '/api/currency/list',
      method: 'GET',
    }),

  /**
   * 获取默认货币
   */
  getDefaultCurrency: () =>
    request<string>({
      url: '/api/currency/default',
      method: 'GET',
    }),

  /**
   * 设置默认货币
   */
  setDefaultCurrency: (data: CurrencyDefaultDTO) =>
    request<void>({
      url: '/api/currency/default',
      method: 'POST',
      data,
    }),

  /**
   * 获取汇率
   */
  getExchangeRate: (params: ExchangeRateParams) =>
    request<ExchangeRate>({
      url: '/api/currency/exchange-rate',
      method: 'GET',
      params,
    }),
}

