import { useInfiniteMediaAssets } from "@/hooks/mediaAsset/useInfiniteMediaAssets"
import { type ScrollParams } from "@/types"
import { Spinner } from "../ui/spinner";
import MediaAssetCard from "./media-asset-card";
import { useEffect, useRef } from "react";
export default function MediaAssetScroll() {
    const params: ScrollParams = {
        order: "DESC",
        size: 10,
    }
    const sentinelRef = useRef<HTMLDivElement>(null);
    const { data, hasNextPage, fetchNextPage, isFetchingNextPage ,status, error } = useInfiniteMediaAssets(params);

    useEffect(() => {
        const sentinel = sentinelRef.current;
        if(!sentinel) return;
        const observer : IntersectionObserver = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && hasNextPage)
                fetchNextPage();

        }, {rootMargin: "300px"});
        observer.observe(sentinel);
        return () => observer.disconnect();
    }, [hasNextPage, isFetchingNextPage ]);
    return (<>
        {status === "pending"
            ? (<Spinner></Spinner>)
            : status === "error"
                ? <p>{error.message}</p>
                : (<div className="grid grid-cols-2 md:grid-cols-4 gap-2 px-2">
                    {data.pages.flatMap(page => page.items).map(ma => (
                        <MediaAssetCard key={ma.id} ma={ma} />
                    ))}
                </div>)}
                {isFetchingNextPage && <Spinner/>}
            <div ref={sentinelRef} className="h-1 w-full border-amber-500 border"></div>
    </>)
}