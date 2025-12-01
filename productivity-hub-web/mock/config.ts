import Mock from 'mockjs'
import type { ConfigItem, ConfigUpdatePayload } from '@/types/config'

let configs: ConfigItem[] = [
  {
    id: 'cfg_1',
    module: 'auth',
    key: 'token.expireDays',
    value: '7',
    description: '登录态 Token 过期时间（单位：天）',
    updatedAt: '2025-11-28 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_2',
    module: 'message',
    key: 'sendgrid.template',
    value: 'welcome-template-v3',
    description: '默认 SendGrid 模板',
    updatedAt: '2025-11-26 14:33',
    updatedBy: '产品',
  },
  {
    id: 'cfg_3',
    module: 'agents',
    key: 'dify.timeout',
    value: '30',
    description: 'Dify 智能体超时时间（秒）',
    updatedAt: '2025-11-20 09:12',
    updatedBy: '研发',
  },
]

Mock.mock('/api/config', 'get', () => ({
  code: 0,
  message: 'OK',
  data: configs,
}))

Mock.mock('/api/config', 'put', ({ body }) => {
  const payload = JSON.parse(body) as ConfigUpdatePayload
  configs = configs.map((item) =>
    item.id === payload.id
      ? {
          ...item,
          value: payload.value,
          description: payload.description ?? item.description,
          updatedAt: new Date().toISOString(),
          updatedBy: 'Mock Admin',
        }
      : item,
  )

  const updated = configs.find((item) => item.id === payload.id)
  return {
    code: 0,
    message: '保存成功',
    data: updated,
  }
})

