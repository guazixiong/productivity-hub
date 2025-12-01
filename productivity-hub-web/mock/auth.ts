import Mock from 'mockjs'
import type { LoginPayload, AuthResponse } from '@/types/auth'

const users = [
  {
    id: 'u_admin',
    username: 'admin',
    password: 'admin123',
    name: '超级管理员',
    roles: ['admin'],
    email: 'admin@productivity-hub.io',
  },
  {
    id: 'u_ops',
    username: 'ops',
    password: 'ops123',
    name: '运营同学',
    roles: ['operator'],
    email: 'ops@productivity-hub.io',
  },
]

Mock.mock('/api/auth/login', 'post', ({ body }) => {
  const payload = JSON.parse(body) as LoginPayload
  const matched = users.find((user) => user.username === payload.username && user.password === payload.password)

  if (!matched) {
    return { code: 401, message: '账号或密码错误', data: null }
  }

  const response: AuthResponse = {
    token: `mock-token-${matched.id}`,
    refreshToken: `mock-refresh-${matched.id}`,
    user: {
      id: matched.id,
      name: matched.name,
      roles: matched.roles,
      email: matched.email,
    },
  }

  return {
    code: 0,
    message: 'OK',
    data: response,
  }
})

