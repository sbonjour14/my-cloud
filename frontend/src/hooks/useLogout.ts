import { useQueryClient } from "@tanstack/react-query"

export function useLogout() {
    const queryClient = useQueryClient()

    return async () => {
        localStorage.removeItem("my-cloud-token")

        queryClient.clear()
    }
}