import { api } from "../axios";



export async function getStorageUsed() : Promise<number> {
    const {data:response} = await api.get("/mediaAssets/size")
    return response;
} 

export async function uploadFile(file: File): Promise<void> {
  const formData = new FormData()
  formData.append("file", file)

  await api.post("/mediaAssets", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  })
}