import Mock from 'mockjs'
import type { BaseMessagePayload, MessageHistoryItem, MessageSendResult } from '@/types/messages'

const Random = Mock.Random

let history: MessageHistoryItem[] = [
  {
    id: Random.guid(),
    channel: 'sendgrid',
    request: { subject: '欢迎邮件', recipients: 'team@company.com' },
    status: 'success',
    response: 'SendGrid accepted message',
    createdAt: '2025-11-29 08:41',
  },
  {
    id: Random.guid(),
    channel: 'dingtalk',
    request: { msgType: 'markdown', title: '巡检报告' },
    status: 'failed',
    response: '签名错误',
    createdAt: '2025-11-27 16:20',
  },
]

Mock.mock('/api/messages/send', 'post', ({ body }) => {
  const payload = JSON.parse(body) as BaseMessagePayload
  const status = Math.random() > 0.1 ? 'success' : 'failed'
  const result: MessageSendResult = {
    requestId: Random.guid(),
    status: status === 'success' ? 'success' : 'failed',
    detail: status === 'success' ? '消息已送达（Mock）' : '钉钉限流，请稍后重试',
  }

  history = [
    {
      id: result.requestId,
      channel: payload.channel,
      request: payload.data,
      status: status as 'success' | 'failed',
      response: result.detail,
      createdAt: new Date().toISOString(),
    },
    ...history,
  ].slice(0, 20)

  return { code: 0, message: 'OK', data: result }
})

Mock.mock('/api/messages/history', 'get', () => ({
  code: 0,
  message: 'OK',
  data: history,
}))

