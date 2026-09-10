import { useQuery } from "@tanstack/react-query"
import { getStorageUsed } from "@/lib/http-api/media"

export const storageKeys = {
  usage: (userId?: string) => ["uploads", "usage", userId] as const,
}

export function useStorageUsage(userId?: string) {
  return useQuery({
    queryKey: storageKeys.usage(userId),
    queryFn: getStorageUsed,
    enabled: !!userId
  })
}