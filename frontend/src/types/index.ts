export type RegisterFormBody = {
  displayName: string,
  email: string,
  password: string,
}

export type LoginFormBody = {
  email : string,
  password : string
}

export type Role = "USER" | "ADMIN";

export type RegisterResponse = {
  id : string,
  email : string,
  displayName : string,
  createdAt : string,
  role : Role,
}

export type UserResponse = {
  id: string,
  email: string,
  displayName: string,
  role: Role,
  createdAt: string
}