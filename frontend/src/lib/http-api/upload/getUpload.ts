
import { api } from "@/lib/axios";
import type { UploadSessionResponse} from "@/types";


export async function getUpload(id: string) : Promise<UploadSessionResponse>{
    const res = await api.get("/uploads/" + id);
    return res.data;
}