import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
} from "@/components/ui/dialog"
import { Progress, ProgressLabel, ProgressValue } from "@/components/ui/progress"

export type UploadStatus = "idle" | "uploading" | "complete" | "error"

export interface UploadDialogProps {
    open: boolean
    progress: number // 0-100
    status: UploadStatus
    onClose: () => void
}

export function UploadDialog({ open, progress, status, onClose }: UploadDialogProps) {
    const canClose = status === "complete" || status === "error"

    return (
        <Dialog open={open} onOpenChange={(next) => !next && canClose && onClose()}>
            <DialogContent className="sm:max-w-sm">
                <DialogHeader>
                    <DialogTitle>
                        {status === "error"
                            ? "Upload failed"
                            : status === "complete"
                                ? "Upload complete"
                                : "Uploading your file"}
                    </DialogTitle>
                    <DialogDescription>
                        {status === "error"
                            ? "Something went wrong, please try again."
                            : status === "complete"
                                ? "Your file is safe and sound."
                                : "Hang tight, your cat is on it."}
                    </DialogDescription>
                </DialogHeader>

                <div className="relative h-10 mb-1">
                    {status === "uploading" && (
                        <div
                            className="absolute top-0 h-20 w-20 overflow-hidden rounded-md transition-all duration-150 ease-linear"
                            style={{ left: `calc(${progress}% - ${progress * 0.5}px)` }}
                        >
                            <img
                                src="https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExNTgxd2Nib2l3aDJncXBwYmU3dXk3Yno0ZW82cmt2Y3Z6eXZqZ2oycCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9cw/kBrB4AzRAzdHqGC4kg/giphy.gif"
                                alt=""
                                className="h-full w-full object-cover"
                                style={{
                                    objectPosition: "50% 30%",
                                    transform: "scaleX(-1)",
                                }}
                            />
                        </div>
                    )}
                    {status === "complete" && (
                        <img
                            src="https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExamYzdGd0Z3F0anBtbzMzbjRybDJhZGJjZzV0Mmt0ZXRnOXdjNHhuayZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9cw/ggpoVsIg0LwtHfTBEY/giphy.gif"
                            alt=""
                            className="absolute -top-5 left-1/2 -translate-x-1/2 h-20 w-20"
                        />
                    )}
                    {status === "error" && (
                        <img
                            src="https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExc2JuZW80cDVsZ3hxdDBhY2k4YWxxbXB3dmtibzFnd3NzMHl4ZWdhcCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9cw/uWzRXTQRoQzxDO9W0p/giphy.gif"
                            alt=""
                            className="absolute -top-5 left-1/2 -translate-x-1/2 h-20 w-20"
                        />
                    )}
                </div>

                {status !== "error" && (
                    <Progress value={progress} className="w-full">
                        <ProgressLabel>Upload progress</ProgressLabel>
                        <ProgressValue />
                    </Progress>
                )}
            </DialogContent>
        </Dialog>
    )
}