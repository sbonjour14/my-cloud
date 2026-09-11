import { api } from "@/lib/axios";
import type { AxiosResponse } from "axios";

export async function uploadChunk(uploadId: string, file: File, start: number, end: number, signal : AbortSignal) : Promise<AxiosResponse<any, FormData, {}, any>> {

    const chunk = file.slice(start, end+1, file.type);

    const formData = new FormData();
    formData.append("chunk", chunk);

    const result = await api.post(`/uploads/${uploadId}`, formData, {signal,  headers: {"Content-Range": `bytes ${start}-${end}/${file.size}`}});
    return result;
}