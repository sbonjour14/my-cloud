
import { api } from "@/lib/axios";
import type { UploadSessionResponse} from "@/types";
import { uploadsessionsUrl } from ".";


export async function getUploadSession(id: string) : Promise<UploadSessionResponse>{
    const res = await api.get(uploadsessionsUrl + "/" + id);
    return res.data;
}