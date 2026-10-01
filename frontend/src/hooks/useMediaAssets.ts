import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { deleteMediaAsset, getCount, getMediaAssets } from "@/lib/http-api/media"

export const mediaAssetsKeys = {
    all: ["uploads"] as const,
    page: (page: number) => ["uploads", page] as const,
    count: ["uploads", "count"] as const
}

export function useMediaAssets(page: number = 0, size: number = 5, order: "ASC" | "DESC" = "DESC") {
    return useQuery({
        queryFn: () => getMediaAssets(page, size, order ),
        queryKey: mediaAssetsKeys.page(page),
        refetchInterval: (query) => {
            const data = query.state.data;
            const hasPending = data?.some((asset) => !asset.hasThumbnail);
            return hasPending ? 2000 : false;
        }
    })
}

export function useDeleteMediaAsset() {
    const queryClient = useQueryClient();
    const mutation = useMutation({
        mutationFn: (id: string) => deleteMediaAsset(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: mediaAssetsKeys.all });
            mutation.reset();
        },
    });

    return {
        deleteAsset: mutation.mutate,
        isPending: mutation.isPending,
    }
}


export function useMediaAssetsCount() {
    return useQuery({
        queryKey: mediaAssetsKeys.count,
        queryFn: async () => {
            const count = await getCount();
            return count 
        }
    })
}