import { api } from "../axios";



export async function getUserTotalStorageUsed() : Promise<number> {
    const {data:response} = await api.get("/mediaAssets/size")
    return response;
} 