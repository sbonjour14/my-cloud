import { api } from "@/lib/axios";
import { uploadsUrl } from ".";

export type Upload = {id: string, filename: string, totalSize:number, createdAt: string}
export async function getUploads() : Promise<Upload[]> {
    const res = await api.get(uploadsUrl);
    return res.data;
}