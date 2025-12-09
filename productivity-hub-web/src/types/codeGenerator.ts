// 代码生成器相关类型定义

export interface DatabaseConfig {
  id?: string
  name: string
  type: 'mysql' | 'postgresql' | 'oracle' | 'sqlserver' | 'sqlite'
  host: string
  port: number
  database: string
  username: string
  password: string
  schema?: string
}

export interface TableColumn {
  name: string
  type: string
  length?: number
  nullable: boolean
  primaryKey: boolean
  autoIncrement: boolean
  defaultValue?: string
  comment?: string
  javaType?: string
  javaField?: string
}

export interface TableInfo {
  name: string
  comment?: string
  columns: TableColumn[]
  javaClassName?: string
  javaPackage?: string
}

export interface FieldMapping {
  dbField: string
  javaField: string
  javaType: string
  comment?: string
}

export interface FileTemplate {
  id?: string
  name: string
  type: 'controller' | 'service' | 'serviceImpl' | 'mapper' | 'mapperXml' | 'entity' | 'dto' | 'vo' | 'custom'
  content: string
  fileNamePattern: string // 例如: {className}Controller.java
  enabled: boolean
}

export interface CompanyTemplate {
  id?: string
  name: string
  description?: string
  basePackage: string
  author: string
  templates: FileTemplate[]
  createdAt?: string
  updatedAt?: string
}

export interface CodeGenerationConfig {
  companyTemplateId?: string
  tableInfo: TableInfo
  fieldMappings: FieldMapping[]
  outputPath?: string
  selectedTemplateIds: string[] // template ids
}

export interface GeneratedCode {
  fileName: string
  content: string
  type: string
}

export interface CodePreview {
  templateId: string
  templateName: string
  fileName: string
  content: string
}

