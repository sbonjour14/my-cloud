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

export type MediaAssetResponse = {
  id: string,
  filename: string,
  url: string
  thumbnailUrl: string,
  createdAt: string,
  hasThumbnail: boolean,
}

export type ByteRange = {
  byteStart: number,
  byteEnd: number
}

export type UploadSessionStatus =  "UPLOADING" | "PAUSED" | "COMPLETE"

export type UploadSessionResponse = {
  status: UploadSessionStatus,
  totalSize: number,
  uploadedSize: number,
  uploadedRanges: ByteRange[]
}