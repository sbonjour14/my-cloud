import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { deleteMediaAsset, getMediaAssets } from "@/lib/http-api/media"

export const mediaAssetsKeys = {
    all: ["uploads"] as const,
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
        deleteAsset: mutation.mutate
    }
}

