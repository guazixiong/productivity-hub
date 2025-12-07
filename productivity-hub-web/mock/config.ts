import Mock from 'mockjs'
import type { ConfigItem, ConfigUpdatePayload } from '@/types/config'

let configs: ConfigItem[] = [
  {
    id: 'cfg_1',
    module: 'dingtalk',
    key: 'dingtalk.webhook',
    value: 'https://oapi.dingtalk.com/robot/send?access_token=your_token_here',
    description: '钉钉 Webhook 地址',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_2',
    module: 'dingtalk',
    key: 'dingtalk.sign',
    value: 'your_sign_secret',
    description: '钉钉加签秘钥（为空则不启用签名）',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_3',
    module: 'dingtalk',
    key: '_module.description',
    value: '钉钉通知配置',
    description: '模块描述',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
]

Mock.mock('/api/config', 'get', () => ({
  code: 0,
  message: 'OK',
  data: configs,
}))

Mock.mock('/api/config', 'put', ({ body }) => {
  const payload = JSON.parse(body) as ConfigUpdatePayload
  const now = new Date()
  const formattedDate = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  
  configs = configs.map((item) =>
    item.id === payload.id
      ? {
          ...item,
          value: payload.value,
          description: payload.description ?? item.description,
          updatedAt: formattedDate,
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

