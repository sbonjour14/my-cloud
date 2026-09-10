import { useQuery } from "@tanstack/react-query"
import { getMediaAssets } from "@/lib/http-api/media"

export const mediaAssetsKeys = {
    all: ["uploads", "all"] as const,
}

export function useMediaAssets() {
    return useQuery({
        queryFn: getMediaAssets,
        queryKey: mediaAssetsKeys.all,
        refetchInterval: (query) => {
            const data = query.state.data;
            const hasPending = data?.some((asset) => !asset.hasThumbnail);
            return hasPending ? 2000 : false;
        }
    })
}


