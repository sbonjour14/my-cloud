import type { MediaAssetResponse } from "@/types";
import { api } from "../axios";



export async function getStorageUsed() : Promise<number> {
	const {data:response} = await api.get("/mediaAssets/size")
	return response;
}

export async function uploadFile(file: File): Promise<MediaAssetResponse> {
	const formData = new FormData()
	formData.append("file", file)

	const {data: response} = await api.post("/mediaAssets", formData, {
		headers: { "Content-Type": "multipart/form-data" },
	})

	return response as MediaAssetResponse;
}


export async function getMediaAssets() : Promise<MediaAssetResponse[]> {
	const {data: response } = await api.get("/mediaAssets");
	return response as MediaAssetResponse[];
}

export async function deleteMediaAsset(id: string) : Promise<void> {
	await api.delete("/mediaAssets/" + id);
}
