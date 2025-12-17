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

export interface ManagedUser {
  id: string
  username: string
  name: string
  email?: string
  roles: string[]
  createdAt?: string
  updatedAt?: string
}

export interface UserCreatePayload {
  username: string
  password?: string
  name: string
  email?: string
  roles: string[]
}

