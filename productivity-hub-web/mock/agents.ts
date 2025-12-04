import Mock from 'mockjs'
import type { AgentSummary, AgentInvocationPayload, AgentInvocationResult, AgentLogEntry } from '@/types/agents'
import type { PageResult } from '@/types/common'

const Random = Mock.Random

const agents: AgentSummary[] = [
  {
    id: 'agent-cs-bot',
    name: '客服问答助手',
    description: '根据 FAQ 与知识库自动回复客户问题',
    version: 'v2.4.1',
    tags: ['客服', 'QA'],
    latencyMs: 1200,
    owner: '智能体验组',
  },
  {
    id: 'agent-report',
    name: '日报生成器',
    description: '从埋点数据自动生成日报摘要与图表',
    version: 'v1.8.0',
    tags: ['数据', '自动化'],
    latencyMs: 2200,
    owner: '数据中台',
  },
  {
    id: 'agent-workflow',
    name: '流程编排助手',
    description: '协调多智能体执行复杂流程，支持异步模式',
    version: 'v0.9.5-beta',
    tags: ['编排', 'Beta'],
    latencyMs: 3400,
    owner: 'AI 实验室',
  },
]

let logs: AgentLogEntry[] = [
  {
    id: Random.guid(),
    agentId: 'agent-cs-bot',
    agentName: '客服问答助手',
    status: 'success',
    duration: 1020,
    createdAt: '2025-11-28 09:30',
    input: '问题：退款流程？',
    output: '已返回标准退款流程',
  },
]

Mock.mock('/api/agents', 'get', () => ({
  code: 0,
  message: 'OK',
  data: agents,
}))

Mock.mock(/\/api\/agents\/logs.*$/, 'get', (config) => {
  const url = new URL(config.url, 'http://localhost')
  const page = Number(url.searchParams.get('page') ?? '1')
  const pageSize = Number(url.searchParams.get('pageSize') ?? '10')
  const safePage = Number.isNaN(page) ? 1 : Math.max(page, 1)
  const safeSize = Number.isNaN(pageSize) ? 10 : Math.min(Math.max(pageSize, 1), 50)
  const offset = (safePage - 1) * safeSize
  const items = logs.slice(offset, offset + safeSize)
  const data: PageResult<AgentLogEntry> = {
    items,
    total: logs.length,
    pageNum: safePage,
    pageSize: safeSize,
  }
  return {
    code: 0,
    message: 'OK',
    data,
  }
})

Mock.mock('/api/agents/invoke', 'post', ({ body }) => {
  const payload = JSON.parse(body) as AgentInvocationPayload
  const agent = agents.find((item) => item.id === payload.agentId)
  if (!agent) {
    return { code: 404, message: '智能体不存在', data: null }
  }

  const succeeded = Math.random() > 0.15
  const result: AgentInvocationResult = {
    taskId: Random.guid(),
    status: succeeded ? 'success' : 'failed',
    output: succeeded ? '智能体响应：处理完成（Mock）' : '执行出错：上游调用异常',
    startedAt: new Date().toISOString(),
    finishedAt: new Date(Date.now() + agent.latencyMs).toISOString(),
  }

  logs = [
    {
      id: result.taskId,
      agentId: agent.id,
      agentName: agent.name,
      status: result.status,
      duration: agent.latencyMs,
      createdAt: new Date().toISOString(),
      input: JSON.stringify(payload.input).slice(0, 60),
      output: result.output,
    },
    ...logs,
  ].slice(0, 30)

  return { code: 0, message: 'OK', data: result }
})

