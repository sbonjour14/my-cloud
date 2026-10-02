export const mediaAssetsKeys = {
    all: ["uploads", "all"] as const,
    pageAll: (page: number) => ["uploads", "all", page] as const,
    pageImage: (page: number) => ["uploads", "IMAGE" ,page] as const,
    pageVideo: (page: number) => ["uploads", "VIDEO" , page] as const,
    count: ["uploads", "all","count"] as const
}