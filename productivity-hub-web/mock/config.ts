import Mock from 'mockjs'
import type { ConfigItem, ConfigUpdatePayload } from '@/types/config'

let configs: ConfigItem[] = [
  {
    id: 'cfg_1',
    module: 'auth',
    key: 'token.expireDays',
    value: '7',
    description: '登录态 Token 过期时间（单位：天）',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-28 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_2',
    module: 'sendgrid',
    key: 'sendgrid.template',
    value: 'welcome-template-v3',
    description: '默认 SendGrid 模板',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-26 14:33',
    updatedBy: '产品',
  },
  {
    id: 'cfg_3',
    module: 'agents',
    key: 'dify.timeout',
    value: '30',
    description: 'Dify 智能体超时时间（秒）',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-20 09:12',
    updatedBy: '研发',
  },
  {
    id: 'cfg_4',
    module: 'sendgrid',
    key: 'sendgrid.recipients',
    value: 'team@company.com,admin@company.com',
    description: '默认 SendGrid 收件人（逗号分隔）',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_5',
    module: 'sendgrid',
    key: 'sendgrid.subject',
    value: '系统通知',
    description: '默认 SendGrid 邮件主题',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_6',
    module: 'sendgrid',
    key: 'sendgrid.content',
    value: '<p>这是一封系统通知邮件</p>',
    description: '默认 SendGrid 邮件内容（HTML）',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_7',
    module: 'dingtalk',
    key: 'dingtalk.webhook',
    value: 'https://oapi.dingtalk.com/robot/send?access_token=your_token_here',
    description: '默认钉钉 Webhook 地址',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_8',
    module: 'dingtalk',
    key: 'dingtalk.msgType',
    value: 'markdown',
    description: '默认钉钉消息类型（text/markdown/link）',
    createdAt: '2025-11-20 09:00',
    updatedAt: '2025-11-29 10:00',
    updatedBy: '系统',
  },
  {
    id: 'cfg_9',
    module: 'dingtalk',
    key: 'dingtalk.content',
    value: '## 系统通知\n\n这是一条系统通知消息',
    description: '默认钉钉消息内容',
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

