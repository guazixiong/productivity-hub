import Mock from 'mockjs'
import './auth'
import './config'
import './messages'
import './agents'

Mock.setup({ timeout: '500-1200' })

console.info('[mock] Mock 服务已启动')

