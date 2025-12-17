<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Edit, Delete, View, Download, Refresh, Connection, Document, Setting } from '@element-plus/icons-vue'
import { codeGeneratorApi } from '@/services/api'
import type { 
  DatabaseConfig, 
  TableInfo, 
  TableColumn, 
  CompanyTemplate, 
  FileTemplate, 
  CodeGenerationConfig,
  GeneratedCode,
  CodePreview
} from '@/types/codeGenerator'

const router = useRouter()

// 当前步骤
const activeStep = ref(0)
const steps = [
  { title: '数据库配置', icon: Connection },
  { title: '表结构解析', icon: Document },
  { title: '模板配置', icon: Setting },
  { title: '代码生成', icon: Download },
]

const defaultTemplatePreset: CompanyTemplate = {
  name: '默认模板',
  description: 'Spring Boot + MyBatis 标准模板',
  basePackage: 'com.example',
  author: 'System',
  templates: [
    {
      id: 'entity',
      name: '实体类',
      type: 'entity',
      content: `package {{basePackage}}.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class {{className}} implements Serializable {
{{fields}}
}`,
      fileNamePattern: '{{className}}.java',
      enabled: true,
    },
    {
      id: 'controller',
      name: 'Controller',
      type: 'controller',
      content: `package {{basePackage}}.controller;

import {{basePackage}}.entity.{{className}};
import {{basePackage}}.service.{{className}}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/{{classNameLower}}")
@RequiredArgsConstructor
public class {{className}}Controller {

    private final {{className}}Service {{classNameLower}}Service;

    @GetMapping
    public List<{{className}}> list() {
        return {{classNameLower}}Service.listAll();
    }

    @GetMapping("/{id}")
    public {{className}} detail(@PathVariable("id") {{pkJavaType}} {{pkJavaField}}) {
        return {{classNameLower}}Service.getById({{pkJavaField}});
    }

    @PostMapping
    public int create(@RequestBody {{className}} body) {
        return {{classNameLower}}Service.create(body);
    }

    @PutMapping("/{id}")
    public int update(@PathVariable("id") {{pkJavaType}} {{pkJavaField}}, @RequestBody {{className}} body) {
        body.set{{pkJavaFieldCapitalized}}({{pkJavaField}});
        return {{classNameLower}}Service.update(body);
    }

    @DeleteMapping("/{id}")
    public int delete(@PathVariable("id") {{pkJavaType}} {{pkJavaField}}) {
        return {{classNameLower}}Service.delete({{pkJavaField}});
    }
}`,
      fileNamePattern: '{{className}}Controller.java',
      enabled: true,
    },
    {
      id: 'service',
      name: 'Service接口',
      type: 'service',
      content: `package {{basePackage}}.service;

import {{basePackage}}.entity.{{className}};
import java.util.List;

public interface {{className}}Service {
    List<{{className}}> listAll();

    {{className}} getById({{pkJavaType}} {{pkJavaField}});

    int create({{className}} data);

    int update({{className}} data);

    int delete({{pkJavaType}} {{pkJavaField}});
}`,
      fileNamePattern: '{{className}}Service.java',
      enabled: true,
    },
    {
      id: 'serviceImpl',
      name: 'Service实现',
      type: 'serviceImpl',
      content: `package {{basePackage}}.service.impl;

import {{basePackage}}.entity.{{className}};
import {{basePackage}}.service.{{className}}Service;
import {{basePackage}}.mapper.{{className}}Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class {{className}}ServiceImpl implements {{className}}Service {

    private final {{className}}Mapper {{classNameLower}}Mapper;

    @Override
    public java.util.List<{{className}}> listAll() {
        return {{classNameLower}}Mapper.selectAll();
    }

    @Override
    public {{className}} getById({{pkJavaType}} {{pkJavaField}}) {
        return {{classNameLower}}Mapper.selectById({{pkJavaField}});
    }

    @Override
    public int create({{className}} data) {
        return {{classNameLower}}Mapper.insert(data);
    }

    @Override
    public int update({{className}} data) {
        return {{classNameLower}}Mapper.update(data);
    }

    @Override
    public int delete({{pkJavaType}} {{pkJavaField}}) {
        return {{classNameLower}}Mapper.delete({{pkJavaField}});
    }
}`,
      fileNamePattern: '{{className}}ServiceImpl.java',
      enabled: true,
    },
    {
      id: 'mapper',
      name: 'Mapper接口',
      type: 'mapper',
      content: `package {{basePackage}}.mapper;

import {{basePackage}}.entity.{{className}};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface {{className}}Mapper {
    List<{{className}}> selectAll();

    {{className}} selectById(@Param("id") {{pkJavaType}} {{pkJavaField}});

    int insert({{className}} entity);

    int update({{className}} entity);

    int delete(@Param("id") {{pkJavaType}} {{pkJavaField}});
}`,
      fileNamePattern: '{{className}}Mapper.java',
      enabled: true,
    },
    {
      id: 'mapperXml',
      name: 'Mapper XML',
      type: 'mapperXml',
      content: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="{{basePackage}}.mapper.{{className}}Mapper">
    <resultMap id="BaseResultMap" type="{{basePackage}}.entity.{{className}}">
{{resultMap}}
    </resultMap>

    <sql id="Base_Column_List">
        {{allColumns}}
    </sql>

    <select id="selectAll" resultMap="BaseResultMap">
        select
        <include refid="Base_Column_List"/>
        from {{tableName}}
    </select>

    <select id="selectById" parameterType="{{pkJavaType}}" resultMap="BaseResultMap">
        select
        <include refid="Base_Column_List"/>
        from {{tableName}}
        where {{pkColumn}} = {{pkParam}}
    </select>

    <insert id="insert" parameterType="{{basePackage}}.entity.{{className}}">
        insert into {{tableName}}
        (
        {{insertColumns}}
        )
        values
        (
        {{insertValues}}
        )
    </insert>

    <update id="update" parameterType="{{basePackage}}.entity.{{className}}">
        update {{tableName}}
        set
{{updateSets}}
        where {{pkColumn}} = {{pkParam}}
    </update>

    <delete id="delete" parameterType="{{pkJavaType}}">
        delete from {{tableName}}
        where {{pkColumn}} = {{pkParam}}
    </delete>
</mapper>`,
      fileNamePattern: '{{className}}Mapper.xml',
      enabled: true,
    },
  ],
}

const cloneDefaultTemplate = (): CompanyTemplate => JSON.parse(JSON.stringify(defaultTemplatePreset))

// 数据库配置
const databaseConfigs = ref<DatabaseConfig[]>([])
const selectedDbConfigId = ref<string>()
const currentDbConfig = ref<DatabaseConfig>({
  name: '',
  type: 'mysql',
  host: 'localhost',
  port: 3306,
  database: '',
  username: '',
  password: '',
})
const dbConfigDialogVisible = ref(false)
const editingDbConfig = ref<DatabaseConfig | null>(null)

// 表结构
const tables = ref<TableInfo[]>([])
const selectedTableName = ref('')
const selectedTable = ref<TableInfo | null>(null)
const tableColumns = ref<TableColumn[]>([])
const isParsingTable = ref(false)

// 字段映射
const fieldMappings = ref<Record<string, string>>({}) // dbField -> javaField

// 公司模板
const companyTemplates = ref<CompanyTemplate[]>([])
const currentTemplate = ref<CompanyTemplate | null>(null)
const templateDialogVisible = ref(false)
const editingTemplate = ref<CompanyTemplate | null>(null)

// 代码生成配置
const generationConfig = ref<CodeGenerationConfig>({
  tableInfo: {
    name: '',
    columns: [],
  },
  fieldMappings: [],
  selectedTemplateIds: [],
})
const generatedCodes = ref<GeneratedCode[]>([])
const generatedActiveIndex = ref(0)
const activeGeneratedCode = computed(() => generatedCodes.value[generatedActiveIndex.value] || null)
const codePreview = ref<CodePreview[]>([])
const previewDialogVisible = ref(false)
const previewActiveIndex = ref(0)
const activePreview = computed(() => codePreview.value[previewActiveIndex.value] || null)
const activeFileTemplateIndex = ref<number | null>(null)
const activeFileTemplate = computed<FileTemplate | null>(() => {
  if (!currentTemplate.value || activeFileTemplateIndex.value === null) return null
  return currentTemplate.value.templates[activeFileTemplateIndex.value] || null
})

// 手动配置表结构
const manualTableDialogVisible = ref(false)
const manualTable = ref<TableInfo>({
  name: '',
  comment: '',
  columns: [],
})
const editingColumn = ref<TableColumn | null>(null)
const columnDialogVisible = ref(false)
const currentColumn = ref<TableColumn>({
  name: '',
  type: 'varchar',
  nullable: true,
  primaryKey: false,
  autoIncrement: false,
  comment: '',
})

// 加载数据库配置
const loadDatabaseConfigs = async () => {
  try {
    const configs = await codeGeneratorApi.getAllDatabaseConfigs()
    databaseConfigs.value = configs

    // 默认选中第一条或保持当前选中项
    if (databaseConfigs.value.length > 0) {
      const matched =
        selectedDbConfigId.value &&
        databaseConfigs.value.find(item => item.id === selectedDbConfigId.value)
      setCurrentDbConfig(matched || databaseConfigs.value[0])
    }
  } catch (error) {
    ElMessage.error('加载数据库配置失败: ' + (error as Error).message)
  }
}

// 添加/编辑数据库配置
const openDbConfigDialog = (config?: DatabaseConfig) => {
  if (config) {
    editingDbConfig.value = { ...config }
  } else {
    editingDbConfig.value = {
      name: '',
      type: 'mysql',
      host: 'localhost',
      port: 3306,
      database: '',
      username: '',
      password: '',
    }
  }
  currentDbConfig.value = { ...editingDbConfig.value }
  dbConfigDialogVisible.value = true
}

// 保存数据库配置
const saveDbConfig = async () => {
  if (!currentDbConfig.value.name || !currentDbConfig.value.host || !currentDbConfig.value.database) {
    ElMessage.warning('请填写完整的数据库配置信息')
    return
  }

  try {
    const config: DatabaseConfig = {
      ...currentDbConfig.value,
      id: editingDbConfig.value?.id,
    }
    await codeGeneratorApi.saveDatabaseConfig(config)
    await loadDatabaseConfigs()
    // 刷新后保持选中刚保存的配置
    const matched = databaseConfigs.value.find(item => item.name === config.name)
    if (matched) setCurrentDbConfig(matched)
    dbConfigDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败: ' + (error as Error).message)
  }
}

// 删除数据库配置
const deleteDbConfig = async (config: DatabaseConfig) => {
  try {
    await ElMessageBox.confirm('确定要删除此数据库配置吗？', '提示', {
      type: 'warning',
    })
    if (config.id) {
      await codeGeneratorApi.deleteDatabaseConfig(config.id)
      await loadDatabaseConfigs()
      ElMessage.success('删除成功')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error as Error).message)
    }
  }
}

// 选中数据库配置
const setCurrentDbConfig = (config?: DatabaseConfig) => {
  if (!config) return
  selectedDbConfigId.value = config.id
  currentDbConfig.value = { ...config }
}

const currentDbRow = computed(() =>
  databaseConfigs.value.find(item => item.id === selectedDbConfigId.value),
)

const goNextStep = () => {
  if (activeStep.value === 0) {
    if (!currentDbRow.value) {
      ElMessage.warning('请先选择数据库配置')
      return
    }
    ElMessage.success('数据库配置已完成，进入表结构解析')
  } else if (activeStep.value === 1) {
    if (!selectedTable.value) {
      ElMessage.warning('请先解析或选择表结构')
      return
    }
    ElMessage.success('表结构解析完成，进入模板配置')
  } else if (activeStep.value === 2) {
    if (!currentTemplate.value) {
      ElMessage.warning('请先选择或配置模板')
      return
    }
    ElMessage.success('模板配置完成，进入代码生成')
  }

  if (activeStep.value < steps.length - 1) {
    activeStep.value += 1
  }
}

// 连接数据库并解析表结构
const parseTableStructure = async () => {
  if (!currentDbConfig.value.id) {
    ElMessage.warning('请先选择或创建数据库配置')
    return
  }

  isParsingTable.value = true
  try {
    const tablesData = await codeGeneratorApi.parseTableStructure({
      databaseConfigId: currentDbConfig.value.id,
    })
    tables.value = tablesData
    if (tables.value.length > 0) selectTable(tables.value[0])
    ElMessage.success('表结构解析成功')
  } catch (error) {
    ElMessage.error('解析表结构失败: ' + (error as Error).message)
  } finally {
    isParsingTable.value = false
  }
}

// 手动配置表结构
const openManualTableDialog = () => {
  manualTable.value = {
    name: '',
    comment: '',
    columns: [],
  }
  manualTableDialogVisible.value = true
}

// 保存手动配置的表结构
const saveManualTable = () => {
  if (!manualTable.value.name) {
    ElMessage.warning('请输入表名')
    return
  }
  if (manualTable.value.columns.length === 0) {
    ElMessage.warning('请至少添加一个字段')
    return
  }

  // 添加到表列表
  const newTable: TableInfo = {
    ...manualTable.value,
    javaClassName: convertToPascalCase(manualTable.value.name),
  }
  tables.value.push(newTable)
  selectTable(newTable)
  manualTableDialogVisible.value = false
  ElMessage.success('表结构配置成功')
}

// 打开字段编辑对话框
const openColumnDialog = (column?: TableColumn) => {
  if (column) {
    currentColumn.value = { ...column }
    editingColumn.value = column
  } else {
    currentColumn.value = {
      name: '',
      type: 'varchar',
      nullable: true,
      primaryKey: false,
      autoIncrement: false,
      comment: '',
    }
    editingColumn.value = null
  }
  columnDialogVisible.value = true
}

// 保存字段
const saveColumn = () => {
  if (!currentColumn.value.name) {
    ElMessage.warning('请输入字段名')
    return
  }

  if (editingColumn.value) {
    // 更新现有字段
    const index = manualTable.value.columns.findIndex(c => c === editingColumn.value)
    if (index >= 0) {
      manualTable.value.columns[index] = { ...currentColumn.value }
    }
  } else {
    // 添加新字段
    manualTable.value.columns.push({ ...currentColumn.value })
  }

  columnDialogVisible.value = false
}

// 删除字段
const removeColumn = (index: number) => {
  manualTable.value.columns.splice(index, 1)
}

// 选择表
const selectTable = (table: TableInfo) => {
  selectedTable.value = table
  selectedTableName.value = table.name
  tableColumns.value = table.columns
  generationConfig.value.tableInfo = { ...table }
  
  // 初始化字段映射
  fieldMappings.value = {}
  table.columns.forEach(col => {
    if (col.javaField) {
      fieldMappings.value[col.name] = col.javaField
    } else {
      // 自动转换：下划线转驼峰
      const javaField = convertToCamelCase(col.name)
      fieldMappings.value[col.name] = javaField
    }
  })
  
  // 生成Java类名
  if (!table.javaClassName) {
    generationConfig.value.tableInfo.javaClassName = convertToPascalCase(table.name)
  }
}

const selectTableByName = (tableName: string) => {
  const table = tables.value.find(item => item.name === tableName)
  if (table) selectTable(table)
}

// 下划线转驼峰
const convertToCamelCase = (str: string): string => {
  return str.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase())
}

// 转PascalCase
const convertToPascalCase = (str: string): string => {
  const camel = convertToCamelCase(str)
  return camel.charAt(0).toUpperCase() + camel.slice(1)
}

// 加载公司模板
const loadCompanyTemplates = async () => {
  try {
    const templates = await codeGeneratorApi.getAllCompanyTemplates()
    companyTemplates.value = templates
    if (companyTemplates.value.length === 0) {
      // 如果没有模板，初始化默认模板
      await initDefaultTemplates()
    } else if (companyTemplates.value.length > 0) {
      currentTemplate.value = companyTemplates.value[0]
      generationConfig.value.companyTemplateId = companyTemplates.value[0].id
    }
  } catch (error) {
    ElMessage.error('加载模板失败: ' + (error as Error).message)
  }
}

// 初始化默认模板
const initDefaultTemplates = async () => {
  const defaultTemplate: CompanyTemplate = cloneDefaultTemplate()
  try {
    const saved = await codeGeneratorApi.saveCompanyTemplate(defaultTemplate)
    companyTemplates.value = [saved]
    currentTemplate.value = saved
    generationConfig.value.companyTemplateId = saved.id
  } catch (error) {
    ElMessage.error('初始化默认模板失败: ' + (error as Error).message)
  }
}

// 保存公司模板（已废弃，使用API）
const saveCompanyTemplates = () => {
  // 不再使用localStorage
}

// 打开模板编辑对话框
const openTemplateDialog = (template?: CompanyTemplate) => {
  if (template) {
    editingTemplate.value = JSON.parse(JSON.stringify(template))
  } else {
    editingTemplate.value = {
      name: '',
      description: '',
      basePackage: 'com.example',
      author: 'System',
      templates: [],
    }
  }
  currentTemplate.value = editingTemplate.value
  activeFileTemplateIndex.value = currentTemplate.value.templates.length > 0 ? 0 : null
  templateDialogVisible.value = true
}

// 使用默认模板初始化新模板配置
const initializeTemplateFromDefault = () => {
  if (!currentTemplate.value) return
  const cloned = cloneDefaultTemplate()
  cloned.id = undefined
  cloned.templates = cloned.templates.map(t => ({ ...t, id: undefined }))
  const mergedName = currentTemplate.value.name || `${cloned.name}副本`
  editingTemplate.value = {
    ...cloned,
    name: mergedName,
  }
  currentTemplate.value = editingTemplate.value
  activeFileTemplateIndex.value = currentTemplate.value.templates.length > 0 ? 0 : null
  ElMessage.success('已使用默认模板填充配置')
}

// 保存模板
const saveTemplate = async () => {
  if (!currentTemplate.value?.name || !currentTemplate.value?.basePackage) {
    ElMessage.warning('请填写模板名称和基础包名')
    return
  }

  try {
    const template: CompanyTemplate = {
      ...currentTemplate.value,
      id: editingTemplate.value?.id,
    }
    const saved = await codeGeneratorApi.saveCompanyTemplate(template)
    await loadCompanyTemplates()
    templateDialogVisible.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败: ' + (error as Error).message)
  }
}

// 删除模板
const deleteTemplate = async (template: CompanyTemplate) => {
  try {
    await ElMessageBox.confirm('确定要删除此模板吗？', '提示', {
      type: 'warning',
    })
    if (template.id) {
      await codeGeneratorApi.deleteCompanyTemplate(template.id)
      await loadCompanyTemplates()
      ElMessage.success('删除成功')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error as Error).message)
    }
  }
}

// 选择模板
const selectTemplate = (template: CompanyTemplate) => {
  currentTemplate.value = template
  generationConfig.value.companyTemplateId = template.id
}

const buildGenerationPayload = () => {
  if (!currentTemplate.value || !selectedTable.value) {
    ElMessage.warning('请先选择模板和表')
    return null
  }

  const enabledTemplates = currentTemplate.value.templates.filter(t => t.enabled)
  if (enabledTemplates.length === 0) {
    ElMessage.warning('请至少启用一个文件模板')
    return null
  }

  const enabledTemplateIds = enabledTemplates
    .map(t => t.id)
    .filter((id): id is string => !!id)

  const config: CodeGenerationConfig = {
    companyTemplateId: currentTemplate.value.id,
    tableInfo: generationConfig.value.tableInfo,
    fieldMappings: Object.entries(fieldMappings.value).map(([dbField, javaField]) => ({
      dbField,
      javaField,
      javaType: selectedTable.value?.columns.find(c => c.name === dbField)?.javaType || 'String',
    })),
    selectedTemplateIds: enabledTemplateIds,
  }

  return { config, enabledTemplates }
}

// 预览代码
const previewCode = async () => {
  const payload = buildGenerationPayload()
  if (!payload) return

  try {
    const { config, enabledTemplates } = payload
    const codes = await codeGeneratorApi.generateCode(config)
    codePreview.value = codes.map((code, index) => {
      const tpl = enabledTemplates[index]
      return {
        templateId: tpl?.id || '',
        templateName: tpl?.name || code.type || '文件',
        fileName: code.fileName,
        content: code.content,
      }
    })
    previewActiveIndex.value = 0
    previewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('预览代码失败: ' + (error as Error).message)
  }
}

// 从模板生成代码
const generateCodeFromTemplate = (
  template: FileTemplate,
  companyTemplate: CompanyTemplate,
  config: CodeGenerationConfig
): GeneratedCode => {
  let content = template.content
  const table = config.tableInfo
  const className = table.javaClassName || convertToPascalCase(table.name)
  const classNameLower = className.charAt(0).toLowerCase() + className.slice(1)

  // 替换变量
  content = content.replace(/\{\{basePackage\}\}/g, companyTemplate.basePackage)
  content = content.replace(/\{\{className\}\}/g, className)
  content = content.replace(/\{\{classNameLower\}\}/g, classNameLower)
  content = content.replace(/\{\{author\}\}/g, companyTemplate.author)
  content = content.replace(/\{\{tableName\}\}/g, table.name)

  // 生成字段
  if (content.includes('{{fields}}')) {
    const fields = table.columns.map(col => {
      const javaField = fieldMappings.value[col.name] || convertToCamelCase(col.name)
      const javaType = col.javaType || mapDbTypeToJava(col.type)
      const comment = col.comment ? `    // ${col.comment}` : ''
      return `    private ${javaType} ${javaField};${comment}`
    }).join('\n')
    content = content.replace('{{fields}}', fields)
  }

  // 生成ResultMap
  if (content.includes('{{resultMap}}')) {
    const resultMap = table.columns.map(col => {
      const javaField = fieldMappings.value[col.name] || convertToCamelCase(col.name)
      const property = javaField
      const column = col.name
      return `        <result column="${column}" property="${property}" />`
    }).join('\n')
    content = content.replace('{{resultMap}}', resultMap)
  }

  // 生成文件名
  let fileName = template.fileNamePattern
  fileName = fileName.replace(/\{\{className\}\}/g, className)
  fileName = fileName.replace(/\{\{classNameLower\}\}/g, classNameLower)

  return {
    fileName,
    content,
    type: template.type,
  }
}

// 数据库类型映射到Java类型
const mapDbTypeToJava = (dbType: string): string => {
  const typeMap: Record<string, string> = {
    'int': 'Integer',
    'integer': 'Integer',
    'bigint': 'Long',
    'smallint': 'Short',
    'tinyint': 'Byte',
    'decimal': 'BigDecimal',
    'numeric': 'BigDecimal',
    'float': 'Float',
    'double': 'Double',
    'varchar': 'String',
    'char': 'String',
    'text': 'String',
    'date': 'Date',
    'datetime': 'Date',
    'timestamp': 'Date',
    'time': 'Time',
    'boolean': 'Boolean',
    'bit': 'Boolean',
  }
  return typeMap[dbType.toLowerCase()] || 'String'
}

// 生成代码
const generateCode = async () => {
  const payload = buildGenerationPayload()
  if (!payload) return

  try {
    const { config } = payload
    generatedCodes.value = await codeGeneratorApi.generateCode(config)
    generatedActiveIndex.value = 0
    ElMessage.success(`成功生成 ${generatedCodes.value.length} 个文件`)
    activeStep.value = 3
  } catch (error) {
    ElMessage.error('生成代码失败: ' + (error as Error).message)
  }
}

// 下载代码
const downloadCode = (code: GeneratedCode) => {
  const blob = new Blob([code.content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = code.fileName
  link.click()
  URL.revokeObjectURL(url)
}

// 下载所有代码
const downloadAllCode = () => {
  downloadAllCodeZip()
}

const downloadAllCodeZip = async () => {
  const payload = buildGenerationPayload()
  if (!payload) return

  try {
    const { config } = payload
    const { blob, fileName } = await codeGeneratorApi.generateCodeZip(config)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName || `${config.tableInfo.name || 'code-generator'}-code.zip`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('正在下载压缩包')
  } catch (error) {
    ElMessage.error('下载全部失败: ' + (error as Error).message)
  }
}

const selectPreviewFile = (index: number) => {
  previewActiveIndex.value = index
}

const copyPreviewCode = async (content: string) => {
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(content)
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = content
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('代码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败: ' + (error as Error).message)
  }
}

// 添加文件模板
const addFileTemplate = () => {
  if (!currentTemplate.value) return
  
  const newTemplate: FileTemplate = {
    name: '新模板',
    type: 'custom',
    content: '',
    fileNamePattern: '{{className}}.java',
    enabled: true,
  }
  currentTemplate.value.templates.push(newTemplate)
  activeFileTemplateIndex.value = currentTemplate.value.templates.length - 1
}

// 删除文件模板
const removeFileTemplate = (index: number) => {
  if (!currentTemplate.value) return
  currentTemplate.value.templates.splice(index, 1)
  if (!currentTemplate.value.templates.length) {
    activeFileTemplateIndex.value = null
  } else {
    const nextIndex = Math.min(
      activeFileTemplateIndex.value ?? 0,
      currentTemplate.value.templates.length - 1
    )
    activeFileTemplateIndex.value = nextIndex
  }
}

const selectFileTemplate = (index: number) => {
  activeFileTemplateIndex.value = index
}

const selectGeneratedCode = (index: number) => {
  generatedActiveIndex.value = index
}

onMounted(async () => {
  await loadDatabaseConfigs()
  await loadCompanyTemplates()
})
</script>

<template>
  <div class="code-generator-container">
    <div class="header">
      <el-button :icon="ArrowLeft" circle @click="router.push('/tools')" />
      <h1>低代码生成</h1>
    </div>

    <el-steps :active="activeStep" class="steps">
      <el-step
        v-for="(step, index) in steps"
        :key="index"
        :title="step.title"
        :icon="step.icon"
        @click="activeStep = index"
        style="cursor: pointer"
      />
    </el-steps>

    <div class="content">
      <!-- 步骤1: 数据库配置 -->
      <div v-show="activeStep === 0" class="step-content">
        <div class="section-header">
          <h2>数据库配置</h2>
          <el-button type="primary" :icon="Plus" @click="openDbConfigDialog()">新增配置</el-button>
        </div>

        <el-alert
          v-if="currentDbRow"
          type="info"
          show-icon
          style="margin-bottom: 12px"
          :title="`当前使用的数据库配置：${currentDbRow.name}`"
        />

        <el-table
          :data="databaseConfigs"
          style="width: 100%"
          highlight-current-row
          :current-row="currentDbRow"
          @row-click="setCurrentDbConfig"
        >
          <el-table-column prop="name" label="配置名称" />
          <el-table-column prop="type" label="数据库类型" />
          <el-table-column prop="host" label="主机" />
          <el-table-column prop="port" label="端口" />
          <el-table-column prop="database" label="数据库" />
          <el-table-column label="当前" width="140">
            <template #default="{ row }">
              <el-button
                type="success"
                size="small"
                plain
                :disabled="row.id === selectedDbConfigId"
                @click.stop="setCurrentDbConfig(row)"
              >
                {{ row.id === selectedDbConfigId ? '当前使用' : '设为当前' }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="openDbConfigDialog(row)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" @click="deleteDbConfig(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-dialog v-model="dbConfigDialogVisible" title="数据库配置" width="600px">
          <el-form :model="currentDbConfig" label-width="120px">
            <el-form-item label="配置名称">
              <el-input v-model="currentDbConfig.name" placeholder="例如: 本地MySQL" />
            </el-form-item>
            <el-form-item label="数据库类型">
              <el-select v-model="currentDbConfig.type" style="width: 100%">
                <el-option label="MySQL" value="mysql" />
                <el-option label="PostgreSQL" value="postgresql" />
                <el-option label="Oracle" value="oracle" />
                <el-option label="SQL Server" value="sqlserver" />
                <el-option label="SQLite" value="sqlite" />
              </el-select>
            </el-form-item>
            <el-form-item label="主机地址">
              <el-input v-model="currentDbConfig.host" placeholder="localhost" />
            </el-form-item>
            <el-form-item label="端口">
              <el-input-number v-model="currentDbConfig.port" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
            <el-form-item label="数据库名">
              <el-input v-model="currentDbConfig.database" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="currentDbConfig.username" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="currentDbConfig.password" type="password" show-password />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="dbConfigDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveDbConfig">保存</el-button>
          </template>
        </el-dialog>

        <div class="step-actions">
          <el-button type="primary" @click="goNextStep">下一步</el-button>
        </div>
      </div>

      <!-- 步骤2: 表结构解析 -->
      <div v-show="activeStep === 1" class="step-content">
        <div class="section-header">
          <h2>表结构解析</h2>
          <div>
            <el-button :icon="Refresh" @click="parseTableStructure" :loading="isParsingTable">解析表结构</el-button>
            <el-button :icon="Plus" @click="openManualTableDialog">手动配置</el-button>
          </div>
        </div>

        <div v-if="tables.length > 0" class="table-config">
          <el-form inline class="table-select-row">
            <el-form-item label="选择表">
              <el-select
                v-model="selectedTableName"
                placeholder="请选择表名"
                style="width: 320px"
                @change="selectTableByName"
              >
                <el-option
                  v-for="table in tables"
                  :key="table.name"
                  :label="table.comment ? `${table.name} - ${table.comment}` : table.name"
                  :value="table.name"
                />
              </el-select>
            </el-form-item>
          </el-form>

          <div v-if="selectedTable" class="table-details">
            <el-descriptions title="表配置" :column="3" border size="small" style="margin-bottom: 12px">
              <el-descriptions-item label="表名">{{ selectedTable.name }}</el-descriptions-item>
              <el-descriptions-item label="表注释">{{ selectedTable.comment || '—' }}</el-descriptions-item>
              <el-descriptions-item label="字段数">{{ selectedTable.columns.length }}</el-descriptions-item>
            </el-descriptions>

            <div class="table-base-config">
              <el-form-item label="Java类名">
                <el-input
                  v-model="generationConfig.tableInfo.javaClassName"
                  placeholder="自动生成"
                  style="width: 300px"
                />
              </el-form-item>
              <el-form-item label="包名">
                <el-input
                  v-model="generationConfig.tableInfo.javaPackage"
                  placeholder="com.example.entity"
                  style="width: 300px"
                />
              </el-form-item>
            </div>

            <h3>字段映射</h3>
            <el-table :data="tableColumns" style="width: 100%">
              <el-table-column prop="name" label="字段名" />
              <el-table-column prop="type" label="类型" />
              <el-table-column prop="nullable" label="可空">
                <template #default="{ row }">
                  <el-tag :type="row.nullable ? 'info' : 'success'">{{ row.nullable ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="primaryKey" label="主键">
                <template #default="{ row }">
                  <el-tag v-if="row.primaryKey" type="danger">是</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="comment" label="注释" />
              <el-table-column label="Java字段名" width="180">
                <template #default="{ row }">
                  <el-input
                    v-model="fieldMappings[row.name]"
                    size="small"
                    placeholder="下划线自动转驼峰"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <el-empty v-else description="请先解析表结构或手动配置" />

        <!-- 手动配置表结构对话框 -->
        <el-dialog v-model="manualTableDialogVisible" title="手动配置表结构" width="900px" top="5vh">
          <el-form :model="manualTable" label-width="120px">
            <el-form-item label="表名">
              <el-input v-model="manualTable.name" placeholder="例如: user" />
            </el-form-item>
            <el-form-item label="表注释">
              <el-input v-model="manualTable.comment" placeholder="例如: 用户表" />
            </el-form-item>

            <el-divider>字段列表</el-divider>
            <div style="margin-bottom: 16px">
              <el-button type="primary" :icon="Plus" @click="openColumnDialog()">添加字段</el-button>
            </div>
            <el-table :data="manualTable.columns" style="width: 100%">
              <el-table-column prop="name" label="字段名" />
              <el-table-column prop="type" label="类型" />
              <el-table-column prop="length" label="长度" />
              <el-table-column prop="nullable" label="可空">
                <template #default="{ row }">
                  <el-tag :type="row.nullable ? 'info' : 'success'">{{ row.nullable ? '是' : '否' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="primaryKey" label="主键">
                <template #default="{ row }">
                  <el-tag v-if="row.primaryKey" type="danger">是</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="autoIncrement" label="自增">
                <template #default="{ row }">
                  <el-tag v-if="row.autoIncrement" type="warning">是</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="comment" label="注释" />
              <el-table-column label="操作" width="150">
                <template #default="{ row, $index }">
                  <el-button link type="primary" :icon="Edit" @click="openColumnDialog(row)">编辑</el-button>
                  <el-button link type="danger" :icon="Delete" @click="removeColumn($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-form>
          <template #footer>
            <el-button @click="manualTableDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveManualTable">保存</el-button>
          </template>
        </el-dialog>

        <!-- 字段编辑对话框 -->
        <el-dialog v-model="columnDialogVisible" title="字段配置" width="600px">
          <el-form :model="currentColumn" label-width="120px">
            <el-form-item label="字段名">
              <el-input v-model="currentColumn.name" placeholder="例如: user_name" />
            </el-form-item>
            <el-form-item label="数据类型">
              <el-select v-model="currentColumn.type" style="width: 100%">
                <el-option label="VARCHAR" value="varchar" />
                <el-option label="CHAR" value="char" />
                <el-option label="TEXT" value="text" />
                <el-option label="INT" value="int" />
                <el-option label="BIGINT" value="bigint" />
                <el-option label="SMALLINT" value="smallint" />
                <el-option label="TINYINT" value="tinyint" />
                <el-option label="DECIMAL" value="decimal" />
                <el-option label="FLOAT" value="float" />
                <el-option label="DOUBLE" value="double" />
                <el-option label="DATE" value="date" />
                <el-option label="DATETIME" value="datetime" />
                <el-option label="TIMESTAMP" value="timestamp" />
                <el-option label="BOOLEAN" value="boolean" />
                <el-option label="BIT" value="bit" />
              </el-select>
            </el-form-item>
            <el-form-item label="长度">
              <el-input-number v-model="currentColumn.length" :min="0" style="width: 100%" />
            </el-form-item>
            <el-form-item label="可空">
              <el-switch v-model="currentColumn.nullable" />
            </el-form-item>
            <el-form-item label="主键">
              <el-switch v-model="currentColumn.primaryKey" />
            </el-form-item>
            <el-form-item label="自增">
              <el-switch v-model="currentColumn.autoIncrement" />
            </el-form-item>
            <el-form-item label="默认值">
              <el-input v-model="currentColumn.defaultValue" placeholder="可选" />
            </el-form-item>
            <el-form-item label="注释">
              <el-input v-model="currentColumn.comment" type="textarea" placeholder="字段说明" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="columnDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveColumn">保存</el-button>
          </template>
        </el-dialog>

        <div class="step-actions">
          <el-button type="primary" @click="goNextStep">下一步</el-button>
        </div>
      </div>

      <!-- 步骤3: 模板配置 -->
      <div v-show="activeStep === 2" class="step-content">
        <div class="section-header">
          <h2>代码模板配置</h2>
          <el-button type="primary" :icon="Plus" @click="openTemplateDialog()">新增模板</el-button>
        </div>

        <div class="templates-container">
          <div class="templates-list">
            <el-card
              v-for="template in companyTemplates"
              :key="template.id"
              :class="{ active: currentTemplate?.id === template.id }"
              @click="selectTemplate(template)"
              style="cursor: pointer; margin-bottom: 12px"
            >
              <div>
                <strong>{{ template.name }}</strong>
                <p v-if="template.description" style="color: #666; margin: 8px 0 0 0">{{ template.description }}</p>
                <div style="margin-top: 8px">
                  <el-tag size="small">基础包: {{ template.basePackage }}</el-tag>
                  <el-button
                    link
                    type="primary"
                    :icon="Edit"
                    size="small"
                    style="margin-left: 8px"
                    @click.stop="openTemplateDialog(template)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    link
                    type="danger"
                    :icon="Delete"
                    size="small"
                    @click.stop="deleteTemplate(template)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>

          <div v-if="currentTemplate" class="template-details">
            <h3>文件模板列表</h3>
            <el-table :data="currentTemplate.templates" style="width: 100%">
              <el-table-column prop="name" label="模板名称" />
              <el-table-column prop="type" label="类型" />
              <el-table-column prop="fileNamePattern" label="文件名模式" />
              <el-table-column label="启用">
                <template #default="{ row }">
                  <el-switch v-model="row.enabled" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row, $index }">
                  <el-button link type="danger" :icon="Delete" @click="removeFileTemplate($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button :icon="Plus" @click="addFileTemplate" style="margin-top: 16px">添加文件模板</el-button>
          </div>
        </div>

        <el-dialog v-model="templateDialogVisible" width="880px" top="5vh">
          <template #header>
            <div class="dialog-header">
              <span>公司模板配置</span>
              <el-button
                v-if="!editingTemplate?.id"
                size="small"
                type="primary"
                plain
                :icon="Refresh"
                @click="initializeTemplateFromDefault"
              >
                使用默认模板初始化
              </el-button>
            </div>
          </template>
          <div v-if="currentTemplate" class="template-dialog-body">
            <el-form :model="currentTemplate" label-width="120px" class="template-basic-form">
              <div class="template-basic-grid">
                <el-form-item label="模板名称">
                  <el-input v-model="currentTemplate.name" />
                </el-form-item>
                <el-form-item label="基础包名">
                  <el-input v-model="currentTemplate.basePackage" />
                </el-form-item>
                <el-form-item label="作者">
                  <el-input v-model="currentTemplate.author" />
                </el-form-item>
                <el-form-item label="描述">
                  <el-input v-model="currentTemplate.description" type="textarea" :rows="2" />
                </el-form-item>
              </div>
            </el-form>

            <div class="template-files-panel">
              <div class="template-files-header">
                <div>
                  <div class="template-pane-title">文件模板</div>
                  <div class="template-pane-subtitle">左侧选择模板，右侧编辑详情</div>
                </div>
                <el-button size="small" :icon="Plus" @click="addFileTemplate">添加模板</el-button>
              </div>
              <div class="template-files-body">
                <div class="template-files-list-pane">
                  <el-scrollbar height="420px" class="file-template-scroll">
                    <div v-if="currentTemplate.templates.length > 0" class="file-template-list">
                      <div
                        v-for="(template, index) in currentTemplate.templates"
                        :key="index"
                        class="file-template-list-item"
                        :class="{ active: activeFileTemplateIndex === index }"
                        @click="selectFileTemplate(index)"
                      >
                        <div class="file-template-list-item-content">
                          <div class="file-template-name">{{ template.name || '未命名模板' }}</div>
                          <div class="file-template-meta">
                            <span class="file-template-type">{{ template.type || '未设置类型' }}</span>
                            <span v-if="template.fileNamePattern" class="file-template-pattern">
                              · {{ template.fileNamePattern }}
                            </span>
                          </div>
                        </div>
                        <div class="file-template-list-actions">
                          <el-switch v-model="template.enabled" size="small" @click.stop />
                          <el-button
                            link
                            type="danger"
                            size="small"
                            :icon="Delete"
                            @click.stop="removeFileTemplate(index)"
                          >
                            删除
                          </el-button>
                        </div>
                      </div>
                    </div>
                    <el-empty v-else description="暂无文件模板，点击右上角添加" />
                  </el-scrollbar>
                </div>

                <div v-if="activeFileTemplate" class="template-files-detail">
                  <div class="template-files-detail__header">
                    <div>
                      <div class="template-pane-title">编辑 {{ activeFileTemplate.name || '未命名模板' }}</div>
                      <div class="template-pane-subtitle">
                        {{ activeFileTemplate.fileNamePattern || '未设置文件名模式' }}
                      </div>
                    </div>
                    <div class="file-template-actions">
                      <el-switch v-model="activeFileTemplate.enabled" size="small" />
                      <el-button
                        link
                        type="danger"
                        size="small"
                        :icon="Delete"
                        @click="removeFileTemplate(activeFileTemplateIndex ?? 0)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                  <el-form label-width="100px" class="file-template-form-grid">
                    <el-form-item label="模板名称">
                      <el-input v-model="activeFileTemplate.name" />
                    </el-form-item>
                    <el-form-item label="类型">
                      <el-select v-model="activeFileTemplate.type" style="width: 100%">
                        <el-option label="实体类" value="entity" />
                        <el-option label="Controller" value="controller" />
                        <el-option label="Service接口" value="service" />
                        <el-option label="Service实现" value="serviceImpl" />
                        <el-option label="Mapper接口" value="mapper" />
                        <el-option label="Mapper XML" value="mapperXml" />
                        <el-option label="DTO" value="dto" />
                        <el-option label="VO" value="vo" />
                        <el-option label="自定义" value="custom" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="文件名模式">
                      <el-input
                        v-model="activeFileTemplate.fileNamePattern"
                        placeholder="例如: {{className}}Controller.java"
                      />
                    </el-form-item>
                    <el-form-item label="模板内容" class="file-template-content">
                      <el-input
                        v-model="activeFileTemplate.content"
                        type="textarea"
                        :autosize="{ minRows: 8, maxRows: 14 }"
                        placeholder="使用 {{变量名}} 作为占位符"
                      />
                    </el-form-item>
                  </el-form>
                </div>
                <el-empty v-else description="请选择左侧模板进行编辑" />
              </div>
            </div>
          </div>
          <template #footer>
            <el-button @click="templateDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveTemplate">保存</el-button>
          </template>
        </el-dialog>

        <div class="step-actions">
          <el-button type="primary" @click="goNextStep">下一步</el-button>
        </div>
      </div>

      <!-- 步骤4: 代码生成 -->
      <div v-show="activeStep === 3" class="step-content">
        <div class="section-header">
          <h2>代码生成</h2>
          <div>
            <el-button :icon="View" @click="previewCode">预览代码</el-button>
            <el-button type="primary" :icon="Download" @click="generateCode">生成代码</el-button>
            <el-button
              v-if="generatedCodes.length > 0"
              type="success"
              :icon="Download"
              @click="downloadAllCode"
            >
              下载全部
            </el-button>
          </div>
        </div>

        <div v-if="generatedCodes.length > 0" class="generated-layout">
          <div class="generated-list">
            <el-scrollbar height="520px">
              <div
                v-for="(code, index) in generatedCodes"
                :key="index"
                class="generated-item"
                :class="{ active: generatedActiveIndex === index }"
                @click="selectGeneratedCode(index)"
              >
                <div class="generated-item__info">
                  <div class="generated-item__title">{{ code.fileName }}</div>
                  <div class="generated-item__meta">{{ code.type }}</div>
                </div>
                <div class="generated-item__actions">
                  <el-button link size="small" :icon="Document" @click.stop="copyPreviewCode(code.content)">复制</el-button>
                  <el-button link type="primary" size="small" :icon="Download" @click.stop="downloadCode(code)">
                    下载
                  </el-button>
                </div>
              </div>
            </el-scrollbar>
          </div>

          <div v-if="activeGeneratedCode" class="generated-pane">
            <div class="generated-pane__header">
              <div>
                <div class="generated-pane__title">{{ activeGeneratedCode.fileName }}</div>
                <div class="generated-pane__subtitle">{{ activeGeneratedCode.type }}</div>
              </div>
              <div class="preview-actions">
                <el-button size="small" :icon="Document" @click="copyPreviewCode(activeGeneratedCode.content)">复制</el-button>
                <el-button type="primary" size="small" :icon="Download" @click="downloadCode(activeGeneratedCode)">
                  下载
                </el-button>
              </div>
            </div>
            <el-scrollbar height="520px" class="generated-code-block">
              <pre><code>{{ activeGeneratedCode.content }}</code></pre>
            </el-scrollbar>
          </div>
        </div>

        <el-empty v-else description="请先生成代码" />
      </div>
    </div>

    <!-- 代码预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="代码预览" width="80%" top="5vh">
      <div v-if="codePreview.length > 0" class="preview-layout">
        <div class="preview-file-list">
          <div
            v-for="(preview, index) in codePreview"
            :key="index"
            class="preview-file-item"
            :class="{ active: previewActiveIndex === index }"
            @click="selectPreviewFile(index)"
          >
            <div class="preview-file-name">{{ preview.fileName }}</div>
            <div class="preview-file-meta">{{ preview.templateName }}</div>
          </div>
        </div>

        <div v-if="activePreview" class="preview-pane">
          <div class="preview-pane__header">
            <div>
              <div class="preview-pane__title">{{ activePreview.fileName }}</div>
              <div v-if="activePreview.templateName" class="preview-pane__subtitle">{{ activePreview.templateName }}</div>
            </div>
            <div class="preview-actions">
              <el-button size="small" :icon="Document" @click="copyPreviewCode(activePreview.content)">复制</el-button>
              <el-button type="primary" size="small" :icon="Download" @click="downloadCode(activePreview as any)">
                下载
              </el-button>
            </div>
          </div>
          <el-scrollbar height="520px" class="preview-code-block">
            <pre><code>{{ activePreview.content }}</code></pre>
          </el-scrollbar>
        </div>
        <el-empty v-else description="暂无预览内容" />
      </div>
      <el-empty v-else description="暂无预览内容" />
    </el-dialog>
  </div>
</template>

<style scoped>
.code-generator-container {
  padding: 32px;
  background: 
    radial-gradient(circle at 20% 30%, rgba(99, 102, 241, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.05) 0%, transparent 50%),
    linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  background-attachment: fixed;
  border-radius: 24px;
  min-height: calc(100vh - 200px);
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #0f172a 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.steps {
  margin-bottom: 32px;
}

.content {
  margin-top: 32px;
}

.step-content {
  min-height: 500px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #0f172a 0%, #475569 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.3px;
}

.step-actions {
  margin-top: 16px;
  text-align: right;
}

.table-select-row {
  margin-bottom: 12px;
}

.table-config {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.table-details h3 {
  margin-bottom: 16px;
  color: #0f172a;
}

.templates-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
}

.templates-list .active {
  border-color: rgba(99, 102, 241, 0.4);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.1) 100%);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.template-details h3 {
  margin-bottom: 16px;
  color: #0f172a;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.template-basic-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.template-basic-grid .el-form-item:last-child {
  grid-column: 1 / -1;
}

.template-files-panel {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #f8fafc;
}

.template-files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.template-files-body {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 12px;
}

.template-files-list-pane {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 10px;
}

.file-template-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-template-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid rgba(99, 102, 241, 0.12);
  border-radius: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(248, 250, 252, 0.9) 100%);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.file-template-list-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  transition: width 0.3s ease;
}

.file-template-list-item:hover {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.file-template-list-item:hover::before {
  width: 3px;
}

.file-template-list-item.active {
  border-color: rgba(99, 102, 241, 0.4);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12) 0%, rgba(139, 92, 246, 0.1) 100%);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
}

.file-template-list-item.active::before {
  width: 3px;
}

.file-template-list-item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-template-list-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.template-files-detail {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.template-files-detail__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.template-pane-title {
  font-weight: 700;
  color: #0f172a;
}

.template-pane-subtitle {
  color: #6b7280;
  font-size: 12px;
  margin-top: 2px;
}

.file-template-collapse {
  border: none;
}

.file-template-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.file-template-name {
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-template-meta {
  color: #6b7280;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-template-pattern {
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-template-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-template-form {
  padding: 6px 2px 0;
}

.file-template-form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 10px 14px;
}

.file-template-form-grid .file-template-content {
  grid-column: 1 / -1;
}

.file-template-scroll {
  padding-right: 6px;
}

.preview-layout {
  display: flex;
  gap: 16px;
}

.preview-file-list {
  width: 240px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-right: 1px solid #ebeef5;
  padding-right: 12px;
}

.preview-file-item {
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.preview-file-item:hover {
  border-color: #c7d2fe;
  background: #f8fafc;
}

.preview-file-item.active {
  border-color: #6366f1;
  background: #f0f4ff;
}

.preview-file-name {
  font-weight: 600;
  color: #0f172a;
}

.preview-file-meta {
  color: #6b7280;
  font-size: 12px;
  margin-top: 4px;
}

.preview-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-pane__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-pane__title {
  font-weight: 700;
  color: #0f172a;
}

.preview-pane__subtitle {
  color: #6b7280;
  margin-top: 4px;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-code-block pre {
  margin: 0;
  padding: 16px;
  background: #0f172a;
  color: #e5e7eb;
  border-radius: 8px;
  min-height: 520px;
  overflow-x: auto;
}

.generated-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}

.generated-list {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #f8fafc;
  padding: 8px;
}

.generated-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.generated-item__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.generated-item:hover {
  background: #eef2ff;
  border-color: #c7d2fe;
}

.generated-item.active {
  background: #f0f4ff;
  border-color: #6366f1;
}

.generated-item__title {
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.generated-item__meta {
  color: #6b7280;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.generated-item__actions {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-shrink: 0;
  white-space: nowrap;
}

.generated-pane {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.generated-pane__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.generated-pane__title {
  font-weight: 700;
  color: #0f172a;
}

.generated-pane__subtitle {
  color: #6b7280;
  margin-top: 2px;
}

.generated-code-block pre {
  margin: 0;
  padding: 16px;
  background: #0f172a;
  color: #e5e7eb;
  border-radius: 8px;
  min-height: 520px;
  overflow-x: auto;
}

.generated-codes pre {
  margin: 0;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow-x: auto;
}

.generated-codes code {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .templates-container {
    grid-template-columns: 1fr;
  }
}
</style>

