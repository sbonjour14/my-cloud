import { useQuery } from "@tanstack/react-query"
import { getMediaAssets } from "@/lib/http-api/media"
import { mediaAssetsKeys } from "./mediaAssetsKeys";

function getKey(page:number, type: "IMAGE" | "VIDEO" | undefined) {
    if(type === "IMAGE")
        return mediaAssetsKeys.pageImage(page);
    if(type === "VIDEO")
        return mediaAssetsKeys.pageVideo(page);
    return mediaAssetsKeys.pageAll(page);
}

export function useMediaAssets(page: number = 0, size: number = 5, order: "ASC" | "DESC" = "DESC", type?: "IMAGE" | "VIDEO") {
    return useQuery({
        queryFn: () => getMediaAssets(page, size, order, type ),
        queryKey: getKey(page, type),
        refetchInterval: (query) => {
            const data = query.state.data;
            const hasPending = data?.some((asset) => !asset.hasThumbnail);
            return hasPending ? 2000 : false;
        }
    })
}


