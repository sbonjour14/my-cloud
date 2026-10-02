import { useQuery } from "@tanstack/react-query";
import { mediaAssetsKeys } from "./mediaAssetsKeys";
import { getCount } from "@/lib/http-api/media";

export function useMediaAssetsCount() {
    return useQuery({
        queryKey: mediaAssetsKeys.count,
        queryFn: async () => {
            const count = await getCount();
            return count 
        }
    })
}