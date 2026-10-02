import { deleteMediaAsset } from "@/lib/http-api/media";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { mediaAssetsKeys } from "./mediaAssetsKeys";

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