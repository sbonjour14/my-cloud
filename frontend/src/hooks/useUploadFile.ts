import { useMutation, useQueryClient } from "@tanstack/react-query"
import { uploadFile } from "@/lib/http-api/media"
import { toast } from "@/components/ui/toast"
import axios from "axios"



export function useUploadFile() {
    const queryClient = useQueryClient()

    const mutation = useMutation({
        mutationFn: async (file: File) => {

            return await toast.promise(uploadFile(file),
                {
                    loading: `uploading ${file.name}`,
                    success: `${file.name} is safe ! `,
                    error: (err) => {
                        if(axios.isAxiosError(err))
                            return err.response?.data?.message || `error uploading ${file.name}`
                        return `error uploading ${file.name}`
                    }
                }
            )
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["storage"] })
        },
    })

    function close() {
        mutation.reset()
    }

    return {
        upload: mutation.mutate,
        close,
    }
}