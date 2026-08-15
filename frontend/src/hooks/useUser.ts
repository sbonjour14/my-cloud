import { useQuery } from "@tanstack/react-query"
import { me } from "@/lib/http-api/user"

export const userKeys = {
  me: ["user", "me"] as const,
}

export function useUser() {
  return useQuery({
    queryKey: userKeys.me,
    queryFn: me,
  })
}