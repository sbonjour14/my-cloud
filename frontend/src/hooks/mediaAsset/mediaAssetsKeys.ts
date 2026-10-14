import type { FileType } from "@/types";

export const mediaAssetsKeys = {
    all: ["uploads", "all"] as const,
    pageAll: (page: number) => ["uploads", "all", page] as const,
    pageImage: (page: number) => ["uploads", "IMAGE" ,page] as const,
    images: ["uploads", "IMAGE"] as const,
    videos: ["uploads", "VIDEO"] as const,
    pageVideo: (page: number) => ["uploads", "VIDEO" , page] as const,
    count: ["uploads", "all","count"] as const,
    page: (cursor?: string, type ?:FileType) => 
        type == undefined 
            ? ["uploads", "all", cursor] as const
            : ["uploads", type, cursor] as const,
    
}