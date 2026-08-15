import { useRef, useState } from "react"
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { uploadFile } from "@/lib/http-api/media"
import type { UploadStatus } from "@/components/ui/upload-dialog"

const FAKE_DURATION = 4000
const FAKE_STEP_INTERVAL = 100

interface UploadState {
    status: UploadStatus
    progress: number
}

export function useUploadFile() {
    const queryClient = useQueryClient()
    const [isOpen, setIsOpen] = useState(false)
    const [state, setState] = useState<UploadState>({ status: "idle", progress: 0 })
    const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

    function stopFakeProgress() {
        if (intervalRef.current) {
            clearInterval(intervalRef.current)
            intervalRef.current = null
        }
    }

    function startFakeProgress() {
        const steps = FAKE_DURATION / FAKE_STEP_INTERVAL
        let currentStep = 0

        intervalRef.current = setInterval(() => {
            currentStep++
            const linear = currentStep / steps
            const eased = 1 - Math.pow(1 - linear, 2)
            const capped = Math.min(eased * 95, 95)

            setState((prev) => ({ status: prev.status, progress: capped }))

            if (currentStep >= steps) {
                stopFakeProgress()
            }
        }, FAKE_STEP_INTERVAL)
    }

    const mutation = useMutation({
        mutationFn: async (file: File) => {
            setIsOpen(true)
            // reset happens here, right as a new upload starts — not on close
            setState({ status: "uploading", progress: 0 })
            startFakeProgress()

            const [response] = await Promise.all([
                uploadFile(file),
                new Promise((resolve) => setTimeout(resolve, FAKE_DURATION)),
            ])
            return response
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["storage"] })
            stopFakeProgress()
            setState({ status: "complete", progress: 100 })
        },
        onError: () => {
            stopFakeProgress()
            setState({ status: "error", progress: 0 })
        },
    })

    function close() {
        stopFakeProgress()
        setIsOpen(false)
        mutation.reset()
        // no state reset here on purpose: the dialog keeps showing its final
        // (complete/error) content while Radix animates it out. The next
        // upload resets state itself in mutationFn.
    }

    return {
        upload: mutation.mutate,
        isOpen,
        status: state.status,
        progress: state.progress,
        close,
    }
}