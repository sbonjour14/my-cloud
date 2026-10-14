import { useInfiniteQuery } from "@tanstack/react-query";
import { mediaAssetsKeys } from "./mediaAssetsKeys";
import { getScroll } from "@/lib/http-api/media";
import type { ScrollParams } from "@/types";


export function useInfiniteMediaAssets(p: ScrollParams) {
    return  useInfiniteQuery({
        queryKey: mediaAssetsKeys.page(p.cursor),
        queryFn: ({pageParam}) => getScroll(pageParam),
        initialPageParam: p,
        getNextPageParam: (lastPage, _, lastPageParam) => 
            lastPage.nextCursor ?
            ({...lastPageParam, cursor: lastPage.nextCursor}) 
            : undefined
    })
}