import Mock from 'mockjs'
import type { ToolStat } from '@/types/tools'

const DEFAULT_STATS: ToolStat[] = [
  { id: 'food', name: '今天吃什么', clicks: 12 },
  { id: 'json', name: 'JSON格式化', clicks: 18 },
  { id: 'time', name: '时间转换', clicks: 15 },
  { id: 'screen-clock', name: '屏幕时钟', clicks: 9 },
  { id: 'password-gen', name: '密码生成器', clicks: 7 },
  { id: 'cron-helper', name: 'Cron表达式', clicks: 11 },
  { id: 'regex-tester', name: '正则表达式', clicks: 6 },
  { id: 'yaml-validator', name: 'YAML格式检查', clicks: 4 },
  { id: 'workday', name: '工作日计算', clicks: 5 },
]

let stats: ToolStat[] = [...DEFAULT_STATS]

const sortStats = () => {
  stats = [...stats].sort((a, b) => b.clicks - a.clicks)
}

Mock.mock('/api/tools/stats', 'get', () => {
  sortStats()
  return {
    code: 0,
    message: 'OK',
    data: stats,
  }
})

Mock.mock('/api/tools/track', 'post', ({ body }) => {
  try {
    const { toolId } = JSON.parse(body) as { toolId?: string }
    if (toolId) {
      const current = stats.find((item) => item.id === toolId)
      if (current) {
        current.clicks += 1
      } else {
        stats.push({
          id: toolId,
          name: toolId,
          clicks: 1,
        })
      }
      sortStats()
    }
  } catch {
    // ignore parse errors in mock handler
  }
  return {
    code: 0,
    message: 'OK',
    data: stats,
  }
})


