import { calculateChecksum } from "@/utils/upload";
import { api } from "../../axios";
import { uploadsessionsUrl } from ".";

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

export async function uploadSessionInit(file: File): Promise<UploadInitResponse> {
    const body: UploadInitRequest = {
        filename: file.name,
        mediaType: file.type,
        totalSize: file.size,
        checksum: await calculateChecksum(file),
    };

    const res = await api.post<UploadInitResponse>(
        uploadsessionsUrl + "/init",
        body
    );

    return res.data;
}