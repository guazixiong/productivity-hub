export interface AgentSummary {
  id: string
  name: string
  description: string
  version: string
  tags: string[]
  latencyMs: number
  owner: string
}

export interface AgentInvocationPayload {
  agentId: string
  input: Record<string, unknown>
  mode: 'sync' | 'async'
  context?: string
}

export interface AgentInvocationResult {
  taskId: string
  status: 'success' | 'running' | 'failed'
  output: string
  startedAt: string
  finishedAt?: string
}

export interface AgentLogEntry {
  id: string
  agentId: string
  agentName: string
  status: 'success' | 'failed' | 'running'
  duration: number
  createdAt: string
  input: string
  output: string
}

