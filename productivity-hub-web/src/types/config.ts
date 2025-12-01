export interface ConfigItem {
  id: string
  module: string
  key: string
  value: string
  description: string
  updatedAt: string
  updatedBy: string
}

export interface ConfigUpdatePayload {
  id: string
  value: string
  description?: string
}

