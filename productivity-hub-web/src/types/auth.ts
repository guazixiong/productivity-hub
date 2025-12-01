export interface LoginPayload {
  username: string
  password: string
  captcha?: string
}

export interface UserInfo {
  id: string
  name: string
  roles: string[]
  email?: string
}

export interface AuthTokens {
  token: string
  refreshToken: string
}

export interface AuthResponse extends AuthTokens {
  user: UserInfo
}

