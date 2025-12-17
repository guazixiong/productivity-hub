import type { Component } from 'vue'
import {
  Food,
  Document,
  Clock,
  Key,
  CollectionTag,
  EditPen,
  Operation,
  Calendar,
  Picture,
  Connection,
  Switch,
  Promotion,
  FolderOpened,
  Timer,
  Shop,
} from '@element-plus/icons-vue'

export interface ToolMeta {
  id: string
  name: string
  description: string
  icon: Component
  path: string
  keywords?: string[]
}

export const toolList: ToolMeta[] = [
  {
    id: 'blueprint',
    name: 'AI成长蓝图',
    description: 'AI架构师成长路线可视化，完整覆盖基础与卓越能力',
    icon: Promotion,
    path: '/tools/blueprint',
    keywords: ['AI', '成长', '蓝图', '路线图', '架构师'],
  },
  {
    id: 'git-toolbox',
    name: 'Git工具箱速查',
    description: '提交规范与常用命令速览，Git工作流指南',
    icon: FolderOpened,
    path: '/tools/git-toolbox',
    keywords: ['git', '版本控制', '命令', '提交规范'],
  },
  {
    id: 'screen-clock',
    name: '屏幕时钟',
    description: '全屏数字时钟，支持12/24小时制，黑白主题切换',
    icon: Timer,
    path: '/tools/clock',
    keywords: ['时钟', '时间', '全屏', '屏幕'],
  },
  {
    id: 'cursor-shop',
    name: 'Cursor商店库存',
    description: '实时查看Cursor邮箱自助小店商品库存状态和销售情况',
    icon: Shop,
    path: '/tools/cursor-shop',
    keywords: ['cursor', '商店', '库存', '商品'],
  },
  {
    id: 'json',
    name: 'JSON格式化',
    description: '格式化、验证、压缩JSON数据',
    icon: Document,
    path: '/tools/json',
    keywords: ['格式化', 'json'],
  },
  {
    id: 'food',
    name: '今天吃什么',
    description: '美食转盘，帮你决定今天吃什么',
    icon: Food,
    path: '/tools/food',
    keywords: ['美食', '转盘', '吃什么'],
  },
  {
    id: 'cron-helper',
    name: 'Cron表达式',
    description: '解析表达式并预览未来执行时间',
    icon: CollectionTag,
    path: '/tools/cron',
    keywords: ['调度', 'cron'],
  },
  {
    id: 'workday',
    name: '工作日计算',
    description: '计算指定日期范围内的工作日',
    icon: Calendar,
    path: '/tools/workday',
    keywords: ['日历', '工作日'],
  },
  {
    id: 'image-base64',
    name: '图片Base64编码',
    description: '将图片转换为Base64编码字符串',
    icon: Picture,
    path: '/tools/image-base64',
    keywords: ['图片', 'base64', '编码', '转换'],
  },
  {
    id: 'time',
    name: '时间转换',
    description: '时间戳与日期时间相互转换',
    icon: Clock,
    path: '/tools/time',
    keywords: ['时间戳', '转换'],
  },
  {
    id: 'password-gen',
    name: '密码生成器',
    description: '自定义字符集，一键生成强密码',
    icon: Key,
    path: '/tools/password',
    keywords: ['密码', '安全'],
  },
  {
    id: 'regex-tester',
    name: '正则表达式',
    description: '实时匹配与替换，支持多模式',
    icon: EditPen,
    path: '/tools/regex',
    keywords: ['正则', '匹配'],
  },
  {
    id: 'yaml-validator',
    name: 'YAML格式检查',
    description: '校验YAML并提示错误位置，附JSON预览',
    icon: Operation,
    path: '/tools/yaml',
    keywords: ['yaml', '校验'],
  },
  {
    id: 'sql-formatter',
    name: 'SQL格式化',
    description: '格式化、压缩SQL语句，支持关键字大小写转换',
    icon: Connection,
    path: '/tools/sql-formatter',
    keywords: ['sql', '格式化', '数据库'],
  },
  {
    id: 'unit-converter',
    name: '单位换算',
    description: '支持长度、面积、体积、时间、角度、速度、温度、压力、热量、功率等单位换算',
    icon: Switch,
    path: '/tools/unit-converter',
    keywords: ['单位', '换算', '转换', '长度', '面积', '体积', '温度'],
  },
]

export const toolMetaMap = new Map(toolList.map((tool) => [tool.id, tool]))
