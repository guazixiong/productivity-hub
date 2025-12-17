import http, { request } from './http'
import type { LoginPayload, AuthResponse, ManagedUser, UserCreatePayload } from '@/types/auth'
import type { ConfigItem, ConfigUpdatePayload, ConfigCreateOrUpdatePayload } from '@/types/config'
import type { BaseMessagePayload, MessageHistoryItem, MessageSendResult } from '@/types/messages'
import type { AgentSummary, AgentInvocationPayload, AgentInvocationResult, AgentLogEntry } from '@/types/agents'
import type { ToolStat } from '@/types/tools'
import type { CursorCommodity } from '@/types/cursorShop'
import type { PageResult } from '@/types/common'
import type { HotSection } from '@/types/hotSections'
import type {
  CompanyTemplate,
  DatabaseConfig,
  TableInfo,
  GeneratedCode,
  CodeGenerationConfig,
} from '@/types/codeGenerator'

export const authApi = {
  login: (payload: LoginPayload) =>
    request<AuthResponse>({
      url: '/api/auth/login',
      method: 'POST',
      data: payload,
    }),
  resetPassword: () =>
    request<{ success: boolean; message: string }>({
      url: '/api/auth/reset-password',
      method: 'POST',
    }),
}

export const adminUserApi = {
  list: () =>
    request<ManagedUser[]>({
      url: '/api/admin/users',
      method: 'GET',
    }),
  create: (payload: UserCreatePayload) =>
    request<ManagedUser>({
      url: '/api/admin/users',
      method: 'POST',
      data: payload,
    }),
}

export const configApi = {
  list: () =>
    request<ConfigItem[]>({
      url: '/api/config',
      method: 'GET',
    }),
  update: (payload: ConfigUpdatePayload) =>
    request<ConfigItem>({
      url: '/api/config',
      method: 'PUT',
      data: payload,
    }),
  createOrUpdate: (payload: ConfigCreateOrUpdatePayload) =>
    request<ConfigItem>({
      url: '/api/config/create-or-update',
      method: 'POST',
      data: payload,
    }),
}

export const messageApi = {
  send: (payload: BaseMessagePayload) =>
    request<MessageSendResult>({
      url: '/api/messages/send',
      method: 'POST',
      data: payload,
    }),
  history: (params?: { page?: number; pageSize?: number }) =>
    request<PageResult<MessageHistoryItem>>({
      url: '/api/messages/history',
      method: 'GET',
      params,
    }),
}

export const agentApi = {
  list: () =>
    request<AgentSummary[]>({
      url: '/api/agents',
      method: 'GET',
    }),
  invoke: (payload: AgentInvocationPayload) =>
    request<AgentInvocationResult>({
      url: '/api/agents/invoke',
      method: 'POST',
      data: payload,
    }),
  logs: (params?: { page?: number; pageSize?: number }) =>
    request<PageResult<AgentLogEntry>>({
      url: '/api/agents/logs',
      method: 'GET',
      params,
    }),
}

export const toolApi = {
  stats: () =>
    request<ToolStat[]>({
      url: '/api/tools/stats',
      method: 'GET',
    }),
  track: (toolId: string) =>
    request<ToolStat[]>({
      url: '/api/tools/track',
      method: 'POST',
      data: { toolId },
      timeout: 5000, // 统计请求使用更短的超时时间（5秒）
      silent: true, // 静默处理错误，不显示错误消息
    }),
}

export const cursorShopApi = {
  commodities: () =>
    request<CursorCommodity[]>({
      url: '/api/tools/cursor-shop/commodities',
      method: 'GET',
    }),
}

export const scheduleApi = {
  // 获取所有热点标签列表
  getHotSectionNames: () => {
    return request<string[]>({
      url: '/api/home/hot-sections/names',
      method: 'GET',
    })
  },
  // 获取指定标签的热点数据
  getHotSection: (sectionName: string, limit?: number) => {
    const params: Record<string, any> = {
      sectionName,
    }
    if (limit !== undefined && limit !== null) {
      params.limit = limit
    }
    return request<HotSection>({
      url: '/api/home/hot-sections/section',
      method: 'GET',
      params,
    })
  },
  // 兼容旧接口
  getHotSections: (limit?: number) => {
    const params: Record<string, any> = {}
    if (limit !== undefined && limit !== null) {
      params.limit = limit
    }
    return request<HotSection[]>({
      url: '/api/home/hot-sections',
      method: 'GET',
      params,
    })
  },
}

export interface WeatherInfo {
  city: string
  province?: string
  address?: string
  weather: string
  temp: string
  wind: string
  humidity: string
}

export const homeApi = {
  getWeather: (params?: { latitude?: number; longitude?: number; cityName?: string }) =>
    request<WeatherInfo>({
      url: '/api/home/weather',
      method: 'GET',
      params,
    }),
}

export const codeGeneratorApi = {
  // 公司模板管理
  getAllCompanyTemplates: () =>
    request<CompanyTemplate[]>({
      url: '/api/code-generator/templates',
      method: 'GET',
    }),
  getCompanyTemplateById: (id: string) =>
    request<CompanyTemplate>({
      url: `/api/code-generator/templates/${id}`,
      method: 'GET',
    }),
  saveCompanyTemplate: (template: CompanyTemplate) =>
    request<CompanyTemplate>({
      url: '/api/code-generator/templates',
      method: 'POST',
      data: template,
    }),
  deleteCompanyTemplate: (id: string) =>
    request<void>({
      url: `/api/code-generator/templates/${id}`,
      method: 'DELETE',
    }),

  // 数据库配置管理
  getAllDatabaseConfigs: () =>
    request<DatabaseConfig[]>({
      url: '/api/code-generator/database-configs',
      method: 'GET',
    }),
  getDatabaseConfigById: (id: string) =>
    request<DatabaseConfig>({
      url: `/api/code-generator/database-configs/${id}`,
      method: 'GET',
    }),
  saveDatabaseConfig: (config: DatabaseConfig) =>
    request<DatabaseConfig>({
      url: '/api/code-generator/database-configs',
      method: 'POST',
      data: config,
    }),
  deleteDatabaseConfig: (id: string) =>
    request<void>({
      url: `/api/code-generator/database-configs/${id}`,
      method: 'DELETE',
    }),

  // 表结构解析
  parseTableStructure: (params: { databaseConfigId: string; tableName?: string }) =>
    request<TableInfo[]>({
      url: '/api/code-generator/parse-table',
      method: 'POST',
      data: params,
    }),

  // 代码生成
  generateCode: (config: CodeGenerationConfig) =>
    request<GeneratedCode[]>({
      url: '/api/code-generator/generate',
      method: 'POST',
      data: config,
    }),
  generateCodeZip: async (config: CodeGenerationConfig) => {
    const response = await http.request<Blob>({
      url: '/api/code-generator/generate/zip',
      method: 'POST',
      data: config,
      responseType: 'blob',
    })

    const disposition = response.headers['content-disposition'] as string | undefined
    const fileNameMatch = disposition?.match(/filename\\*=UTF-8''([^;]+)|filename="?([^\";]+)"?/i)
    const fileName = fileNameMatch && (fileNameMatch[1] || fileNameMatch[2])
      ? decodeURIComponent(fileNameMatch[1] || fileNameMatch[2] || '')
      : 'code-generator.zip'

    return { blob: response.data, fileName }
  },
}

export interface DifyChatOptions {
  apiUrl: string
  apiKey: string
  conversationId?: string
  message: string
  onChunk: (chunk: string) => void
  onComplete: (conversationId?: string) => void
  onError: (error: string) => void
}

/**
 * 与 Dify 聊天助手进行流式对话
 * 使用 Server-Sent Events (SSE) 接收流式响应
 */
export const chatWithDify = async (options: DifyChatOptions): Promise<void> => {
  const { apiUrl, apiKey, conversationId, message, onChunk, onComplete, onError } = options

  try {
    // 构建 Dify API 请求 URL
    const url = `${apiUrl.replace(/\/$/, '')}/v1/chat-messages`
    
    // 构建请求体
    const body: Record<string, any> = {
      inputs: {},
      query: message,
      response_mode: 'streaming',
      user: 'productivity-hub-user',
    }
    
    if (conversationId) {
      body.conversation_id = conversationId
    }

    // 使用 fetch 进行流式请求
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })

    if (!response.ok) {
      const errorText = await response.text()
      onError(`HTTP ${response.status}: ${errorText || response.statusText}`)
      return
    }

    if (!response.body) {
      onError('响应体为空')
      return
    }

    // 读取流式响应
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let lastAnswer = '' // 用于存储上一次的完整答案，以便计算增量
    let finalConversationId: string | undefined = conversationId

    try {
      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          break
        }

        // 解码数据块
        buffer += decoder.decode(value, { stream: true })
        
        // 处理 SSE 格式的数据
        // SSE 格式: data: {...}\n\n 或 data: {...}\n
        const lines = buffer.split('\n')
        buffer = lines.pop() || '' // 保留最后一个不完整的行

        for (const line of lines) {
          const trimmedLine = line.trim()
          if (!trimmedLine) continue
          
          if (trimmedLine.startsWith('data: ')) {
            const dataStr = trimmedLine.slice(6) // 移除 'data: ' 前缀
            
            if (dataStr === '[DONE]') {
              onComplete(finalConversationId)
              return
            }

            try {
              const data = JSON.parse(dataStr)
              
              // 保存会话ID
              if (data.conversation_id) {
                finalConversationId = data.conversation_id
              }
              
              // 处理 Dify 的流式响应格式
              if (data.event === 'message') {
                // message 事件包含增量答案
                if (data.answer !== undefined) {
                  // Dify 的 answer 字段是累积的完整答案，我们需要计算增量
                  const currentAnswer = data.answer
                  if (currentAnswer.length > lastAnswer.length) {
                    const chunk = currentAnswer.slice(lastAnswer.length)
                    onChunk(chunk)
                    lastAnswer = currentAnswer
                  }
                }
              } else if (data.event === 'message_end') {
                // 消息结束事件
                if (data.answer && data.answer.length > lastAnswer.length) {
                  const chunk = data.answer.slice(lastAnswer.length)
                  onChunk(chunk)
                  lastAnswer = data.answer
                }
                onComplete(finalConversationId)
                return
              } else if (data.event === 'message_file') {
                // 处理文件消息
                if (data.answer) {
                  const currentAnswer = data.answer
                  if (currentAnswer.length > lastAnswer.length) {
                    const chunk = currentAnswer.slice(lastAnswer.length)
                    onChunk(chunk)
                    lastAnswer = currentAnswer
                  }
                }
              } else if (data.event === 'error') {
                onError(data.message || data.error?.message || '未知错误')
                return
              } else if (data.event === 'workflow_started' || data.event === 'workflow_finished') {
                // 工作流事件，可以忽略或处理
                continue
              }
            } catch (e) {
              // 忽略 JSON 解析错误，可能是部分数据
              console.warn('Failed to parse SSE data:', dataStr, e)
            }
          }
        }
      }

      // 处理剩余的 buffer
      if (buffer.trim()) {
        const trimmedBuffer = buffer.trim()
        if (trimmedBuffer.startsWith('data: ')) {
          const dataStr = trimmedBuffer.slice(6)
          if (dataStr !== '[DONE]') {
            try {
              const data = JSON.parse(dataStr)
              if (data.conversation_id) {
                finalConversationId = data.conversation_id
              }
              if (data.event === 'message' || data.event === 'message_end') {
                if (data.answer) {
                  const currentAnswer = data.answer
                  if (currentAnswer.length > lastAnswer.length) {
                    const chunk = currentAnswer.slice(lastAnswer.length)
                    onChunk(chunk)
                    lastAnswer = currentAnswer
                  }
                }
              }
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
      }

      onComplete(finalConversationId)
    } catch (error) {
      onError((error as Error).message || '读取流数据时出错')
    } finally {
      reader.releaseLock()
    }
  } catch (error) {
    onError((error as Error).message || '请求失败')
  }
}

