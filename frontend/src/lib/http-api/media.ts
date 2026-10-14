import type { ScrollParams, MediaAssetResponse, Scroll } from "@/types";
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


export async function getMediaAssets(page: number, size: number, order: "ASC" | "DESC", type?:"IMAGE"|"VIDEO") : Promise<MediaAssetResponse[]> {
	const {data: response } = await api.get("/mediaAssets", {
		params: {
			page,
			size,
			order,
			type
		}
	});
	return response as MediaAssetResponse[];
}

export async function deleteMediaAsset(id: string) : Promise<void> {
	await api.delete("/mediaAssets/" + id);
}


export async function getCount() : Promise<number> {
	const {data: response} = await api.get("/mediaAssets/count")
	return response;
}



export async function getScroll(p: ScrollParams) : Promise<Scroll<MediaAssetResponse>> {
	const {data} = await api.get<Scroll<MediaAssetResponse>>("/mediaAssetsList", {params: p});
	return data;

}