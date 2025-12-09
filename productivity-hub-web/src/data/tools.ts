import type { Component } from 'vue'
import {
  Food,
  Document,
  Clock,
  Timer,
  Key,
  CollectionTag,
  EditPen,
  Operation,
  Calendar,
  ShoppingCartFull,
  Picture,
  Connection,
  Switch,
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
    description: 'AI架构师成长路线可视化',
    icon: Document,
    path: '/tools/blueprint',
    keywords: ['蓝图', 'AI', '成长'],
  },
  {
    id: 'git-toolbox',
    name: 'Git工具箱速查',
    description: '提交规范与不同阶段常用命令',
    icon: Document,
    path: '/tools/git-toolbox',
    keywords: ['git', '提交规范', '命令', '工具箱'],
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
    id: 'json',
    name: 'JSON格式化',
    description: '格式化、验证、压缩JSON数据',
    icon: Document,
    path: '/tools/json',
    keywords: ['格式化', 'json'],
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
    id: 'screen-clock',
    name: '屏幕时钟',
    description: '全屏数码时钟，会议直播/倒计时必备',
    icon: Timer,
    path: '/tools/clock',
    keywords: ['时钟', '倒计时'],
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
    id: 'cron-helper',
    name: 'Cron表达式',
    description: '解析表达式并预览未来执行时间',
    icon: CollectionTag,
    path: '/tools/cron',
    keywords: ['调度', 'cron'],
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
    id: 'workday',
    name: '工作日计算',
    description: '计算指定日期范围内的工作日',
    icon: Calendar,
    path: '/tools/workday',
    keywords: ['日历', '工作日'],
  },
  {
    id: 'cursor-shop',
    name: 'Cursor 邮箱自助小店库存',
    description: '查看自助小店实时库存',
    icon: ShoppingCartFull,
    path: '/tools/cursor-shop',
    keywords: ['库存', 'Cursor', '小店'],
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
