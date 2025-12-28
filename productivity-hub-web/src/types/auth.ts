export interface LoginPayload {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
}

export interface UserInfo {
  id: string
  name: string
  roles: string[]
  email?: string
  avatar?: string
  bio?: string
  phone?: string
  gender?: string
  birthday?: string
  address?: string
  company?: string
  position?: string
  website?: string
  createdAt?: string
  updatedAt?: string
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

export interface UserProfileUpdatePayload {
  name?: string
  email?: string
  avatar?: string
  bio?: string
  phone?: string
  gender?: string
  birthday?: string
  address?: string
  company?: string
  position?: string
  website?: string
}

