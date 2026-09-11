import { useQueryClient, useMutation } from "@tanstack/react-query";
import { createContext, useContext, useRef, useState, type Dispatch, type ReactNode, type SetStateAction } from "react";
import { getChunkSizeof, getNonUploadedRanges } from "@/utils/upload";
import axios from "axios";
import { uploadInit } from "@/lib/http-api/upload/uploadInit";
import type { ByteRange, UploadSessionResponse, UploadSessionStatus } from "@/types";
import { getUpload } from "@/lib/http-api/upload/getUpload";
import { uploadChunk } from "@/lib/http-api/upload/uploadChunks";

export type UploadItem = {
    id: string;
    uploadId?: string,
    name: string;
    status: UploadSessionStatus | "CANCELED" | "LOADING";
    uploadedSize: number;
    totalSize: number;
    abortController: AbortController;
}


async function uploadChunks(id : string, uploadId: string, file: File, signal : AbortSignal, ranges: ByteRange[], setUploads: Dispatch<SetStateAction<UploadItem[]>>) {
    console.log("uploading: ", file.name)
    let size = file.size;
    let start: number; let end: number;
    let i = 0;

    let notUploadedRanges = getNonUploadedRanges(ranges, size, getChunkSizeof(file));

    while(!signal.aborted) {
        console.log("uploading chunk ", i, " of ", notUploadedRanges.length);
        if(i >= notUploadedRanges.length) {
            break;
        }
        start = notUploadedRanges[i].byteStart;
        end = notUploadedRanges[i].byteEnd;
        i++;
        let chunk: Blob = file.slice(start, Math.min(end+1, size), file.type);
        let formdata: FormData = new FormData();
        formdata.append("chunk", chunk);
        try {
            const result= await uploadChunk(uploadId, file, start, end, signal);
            if (result.status === 200) {
                console.log("chunk success")
            }
            else if (result.status === 201) {
                console.log("file uploaded")
            }
            setUploads(prev => prev.map(item => {
                if (item.id == id) {
                    return { ...item, uploadedSize: item.uploadedSize + chunk.size }
                }
                return item;
            }))

        } catch (error) {
            if (axios.isAxiosError(error) && !signal.aborted) {
                alert("upload failed, please retry");
                return;
            }
        }
    }
    if(signal.aborted) {
        console.log("upload aborted");
        return;
    }
    setUploads(prev => prev.map(item => {
        if(item.id == id) {
            return {...item, uploadedSize: item.totalSize, status: "COMPLETE"};
        }
        return item;
    }))
    console.log("done!");
}


async function init(file: File, abortController: AbortController, setUploads: Dispatch<SetStateAction<UploadItem[]>>, files: Map<String, File>) {
    const newItem: UploadItem = {
        id: crypto.randomUUID(),
        name: file.name,
        status: "LOADING",
        totalSize: file.size,
        uploadedSize: 0,
        abortController: abortController
    };
    // add new upload item to list for ui
    setUploads(prev => [...prev, newItem]);
    try {
        const data = await uploadInit(file);

        console.log("file already exists: ", data.fileAlreadyExists, " url: ", data.url);

        if (data.fileAlreadyExists) {
            return;
        }
        const uploadId = data.url.split("/").pop()!;
        setUploads(prev => prev.map(item => {
            if(item.id === newItem.id)
                return { ...item, uploadId: uploadId, status: "UPLOADING"};
            return item;
        }))
        // save file before starting upload of chunks
        files.set(newItem.id, file);

        const upload: UploadSessionResponse = await getUpload(uploadId);

        if (upload.status === "UPLOADING") {
            await uploadChunks(newItem.id, uploadId,file, abortController.signal, upload.uploadedRanges, setUploads)
        } else {
            alert("upload failed, please retry");
        }
    }
    catch (error) {
        if (axios.isAxiosError(error)) {
            alert("upload failed, please retry");
            return;
        }
    }
}


const UploadContext = createContext<{
    upload: (file: File) => void;
    pause: (id: string) => void;
    cancel: (id: string) => void;
    resume: (id: string) => void;
    close: (id: string) => void;
    uploads: UploadItem[];

} | null>(null);


export function UploadProvider({children}: {children: ReactNode}) {
    const queryClient = useQueryClient();
    const [uploads, setUploads] = useState<UploadItem[]>([]);
    const files  = useRef<Map<String, File>>(new Map());
    const mutation = useMutation({
        mutationFn: (file: File) => {
            return init(file, new AbortController(), setUploads, files.current);
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["uploads"] }),
    });

    return (
        <UploadContext.Provider
            value={{
                upload: (file: File) => {
                    mutation.mutate(file)
                },
                pause: (id: string) => {
                    setUploads(prev => prev.map(item => {
                            if (item.id === id) {
                                item.abortController.abort();
                                return { ...item, status: "PAUSED" };
                            }
                            return item;
                        })
                    );

                },
                cancel: (id: string) =>  { // id or name
                    setUploads(prev => prev.filter(item => {
                        if(item.id === id || item.name === id) {
                            item.abortController.abort();
                            files.current.delete(id);
                            return false;
                        }
                        return true;
                    }))
                },
                resume: (id: string) => {
                    console.log("resume: ", id);                    
                },


                close: (id: string) => {
                    console.log("close: ", id);
                },
                uploads: uploads,
            }}
        >
            {children}
        </UploadContext.Provider>
    );
}



export function useUpload() {
    const ctx = useContext(UploadContext);
    if (!ctx) throw new Error("useUpload hors UploadProvider");
    return ctx;
}

