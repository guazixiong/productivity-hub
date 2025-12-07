export interface ConfigItem {
  id: string
  module: string
  key: string
  value: string
  description: string
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface ConfigUpdatePayload {
  id: string
  value: string
  description?: string
}

export interface ConfigCreateOrUpdatePayload {
  module: string
  key: string
  value: string
  description?: string
}

