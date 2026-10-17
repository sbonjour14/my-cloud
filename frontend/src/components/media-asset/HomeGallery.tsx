import { useInfiniteMediaAssets } from "@/hooks/mediaAsset/useInfiniteMediaAssets"
import { type MediaAssetSummaryResponse, type ScrollParams } from "@/types"
import { Spinner } from "../ui/spinner";
import MediaAssetCard from "./media-asset-card";
import { useEffect, useMemo, useRef } from "react";
import { cn } from "@/lib/utils";

type GalleryRow =
    | { type: "header"; key: string; label: string }
    | { type: "photos"; key: string; items: MediaAssetSummaryResponse[], isLast: boolean };

const formatter = new Intl.DateTimeFormat("en-US", {
    month: "long",
    year: "numeric",
    timeZone: "UTC",
});

const dateToLabel = (key: string) => formatter.format(new Date(`${key}-01T00:00:00Z`));

function formatData_flush(formattedData: GalleryRow[], currentPhotos: MediaAssetSummaryResponse[], isLast: boolean) {
    if (currentPhotos.length !== 0) {
        formattedData.push({ type: "photos", key: currentPhotos[0].id, items: [...currentPhotos], isLast: isLast });
    }
}

function formatData_addHeader(formattedData: GalleryRow[], key: string) {
    formattedData.push({ type: "header", key: key, label: dateToLabel(key) })
}


const formatData = (data: MediaAssetSummaryResponse[] | undefined, n: number) => {

    if(data == undefined ||  data.length === 0) return [];
    let currentDate : string = "";
    const formattedData : GalleryRow[] = [];

    let currentPhotos : MediaAssetSummaryResponse[] = []
    for(const ma of data) {
        const date = ma.takenAt.substring(0, 7);

        if(date !== currentDate) {
            formatData_flush(formattedData, currentPhotos, true);
            formatData_addHeader(formattedData, date);
            currentPhotos = [ma];
            currentDate = date;

        } else if(currentPhotos.length === n) {
            formatData_flush(formattedData, currentPhotos, false);
            currentPhotos = [ma];
        }
        else {
            currentPhotos.push(ma);
        }
    }
    formatData_flush(formattedData, currentPhotos, false);
    console.log(formattedData);
    
    return formattedData;
}


export default function HomeGallery() {
    const params: ScrollParams = {
        order: "DESC",
        size: 10,
    }
    const sentinelRef = useRef<HTMLDivElement>(null);
    const { data, hasNextPage, fetchNextPage, isFetchingNextPage ,status, error } = useInfiniteMediaAssets(params);
    const column = 3;

    const formattedData = useMemo(() => formatData(data?.pages.flatMap(page => page.items), column), [data, column]);

    useEffect(() => {
        const sentinel = sentinelRef.current;
        if(!sentinel) return;
        const observer : IntersectionObserver = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && hasNextPage)
                fetchNextPage();

        }, {rootMargin: "300px"});
        observer.observe(sentinel);
        return () => observer.disconnect();
    }, [hasNextPage, isFetchingNextPage]);



    return (
    <>
        {status === "pending"
            ? (<Spinner></Spinner>)
            : status === "error"
                ? <p>{error.message}</p>
                : (
                    <div className="flex w-full flex-col">
                        {formattedData.map(row => {
                            if(row.type === "header") 
                                return <div key={row.key}>{row.label}</div>
                            
                            return (
                                <div className={cn("grid grid-cols-[repeat(3,180px)]", row.isLast ? "mb-10":"")}  key={row.key}>
                                    
                                    {row.items.map(item => 
                                        <MediaAssetCard key={item.id} ma={item}/>
                                    )}
                                </div>
                        )})}
                    </div>
                )}
        {isFetchingNextPage && <Spinner />}
        <div ref={sentinelRef} className="h-0.5 w-full"></div>
    </>)
}
