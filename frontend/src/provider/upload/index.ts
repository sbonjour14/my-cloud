import type { UploadSessionStatus } from "@/types";
import { createContext, useContext } from "react";

export type UploadSession = {
    id: string;
    uploadId?: string,
    name: string;
    status: UploadSessionStatus | "CANCELED" | "LOADING";
    uploadedSize: number;
    totalSize: number;
    abortController: AbortController;
}
export const UploadContext = createContext<{
    upload: (file: File) => void;
    pause: (item: UploadSession) => void;
    cancel: (item: UploadSession) => void;
    resume: (item: UploadSession) => void;
    close: (item: UploadSession) => void;
    uploads: UploadSession[];

} | null>(null);

export function useUpload() {
	const ctx = useContext(UploadContext);
	if (!ctx) throw new Error("useUpload hors UploadProvider");
	return ctx;
}
