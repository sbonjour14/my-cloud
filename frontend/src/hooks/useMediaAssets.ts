import { useQuery } from "@tanstack/react-query"
import { getMediaAssets } from "@/lib/http-api/media"

export const mediaAssetsKeys = {
    all: ["storage", "all"] as const,
}

export function useMediaAssets() {
    return useQuery({
        queryFn: getMediaAssets,
        queryKey: mediaAssetsKeys.all
    })
}


