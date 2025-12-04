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
  ]

  return { code: 0, message: 'OK', data: result }
})

Mock.mock(/\/api\/messages\/history.*/, 'get', ({ url }) => {
  const search = url.split('?')[1] ?? ''
  const params = new URLSearchParams(search)
  const page = Number(params.get('page') ?? 1)
  const pageSize = Number(params.get('pageSize') ?? 10)
  const safePage = Math.max(page, 1)
  const safeSize = Math.max(pageSize, 1)
  const start = (safePage - 1) * safeSize
  const sorted = [...history].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
  )
  const items = sorted.slice(start, start + safeSize)
  return {
    code: 0,
    message: 'OK',
    data: {
      items,
      total: history.length,
      pageNum: safePage,
      pageSize: safeSize,
    },
  }
})

