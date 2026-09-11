import { calculateChecksum } from "@/utils/upload";
import { api } from "../../axios";

export type UploadInitRequest = {
    filename: string,
    mediaType: string,
    totalSize: number,
    checksum: string
}
export type UploadInitResponse = {
    fileAlreadyExists: boolean,
    url: string
}

export async function uploadInit(file: File): Promise<UploadInitResponse> {
    const body: UploadInitRequest = {
        filename: file.name,
        mediaType: file.type,
        totalSize: file.size,
        checksum: await calculateChecksum(file),
    };

    const res = await api.post<UploadInitResponse>(
        "/uploads/init",
        body
    );

    return res.data;
}