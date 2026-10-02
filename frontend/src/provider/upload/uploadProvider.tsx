import { useQueryClient, useMutation } from "@tanstack/react-query";
import { useEffect, useRef, useState, type ReactNode } from "react";
import { getChunkSizeof, getFileType, getNonUploadedRanges } from "@/utils/upload";
import axios from "axios";
import { uploadSessionInit } from "@/lib/http-api/upload/uploadSessionInit";
import type { ByteRange, UploadSessionResponse } from "@/types";
import { getUploadSession } from "@/lib/http-api/upload/getUploadSession";
import { uploadChunk } from "@/lib/http-api/upload/uploadChunks";
import { UploadContext, type UploadSession } from "@/provider/upload";

export function UploadProvider({ children }: { children: ReactNode }) {
    const queryClient = useQueryClient();
    const [uploads, setUploads] = useState<UploadSession[]>([]);
    const uploadedSizes = useRef<Map<string, number>>(new Map());
    const files = useRef<Map<string, File>>(new Map());
    const hasActiveUpload = uploads.some(item => item.status === "UPLOADING");

    useEffect(() => {
        if (!hasActiveUpload) return;

        const intervalId = setInterval(() => {
            setUploads(prev => prev.map(item => {
                const current = uploadedSizes.current.get(item.id);
                if (current !== undefined && current !== item.uploadedSize && item.status !== "COMPLETE") {
                    return { ...item, uploadedSize: current };
                }
                return item;
            }));
        }, 1000);

        return () => clearInterval(intervalId);
    }, [hasActiveUpload]);

    async function uploadChunks(id: string, uploadId: string, file: File, signal: AbortSignal, ranges: ByteRange[]) {
        const size = file.size;
        let start: number; let end: number;
        let i = 0;

        const notUploadedRanges = getNonUploadedRanges(ranges, size, getChunkSizeof(file));

        while (!signal.aborted) {

            if (i >= notUploadedRanges.length) {
                break;
            }

            start = notUploadedRanges[i].byteStart;
            end = notUploadedRanges[i].byteEnd;

            try {
                await uploadChunk(uploadId, file, start, end, signal);

                const newSize: number = uploadedSizes.current.get(id)! + (end - start + 1);
                uploadedSizes.current.set(id, newSize);
                i++;

            } catch (error) {
                console.error(`Error uploading chunk ${i + 1} for file ${file.name}: bytes ${start}-${end}`, error);
                if (!signal.aborted) {
                    
                    setUploads(prev => prev.map(item => {
                        if (item.id === id)
                            return {...item, status: "PAUSED"}
                        return item
                    }));
                    
                    return;
                }
            }
        }
        if (signal.aborted) {
            return;
        }
        setUploads(prev => prev.map(item => {
            if (item.id == id) {
                return { ...item, uploadedSize: item.totalSize, status: "COMPLETE" };
            }
            return item;
        }))
        uploadedSizes.current.set(id, size);
        console.log("done!");
    }

    async function init(file: File, abortController: AbortController) {
        const newItem: UploadSession = {
            id: crypto.randomUUID(),
            name: file.name,
            status: "LOADING",
            totalSize: file.size,
            uploadedSize: 0,
            abortController: abortController,
            type: getFileType(file)
        };
        // add new upload item to list for ui
        uploadedSizes.current.set(newItem.id, 0);
        setUploads(prev => [...prev, newItem]);
        try {
            const data = await uploadSessionInit(file);

            console.log("file already exists: ", data.fileAlreadyExists, " url: ", data.url);

            if (data.fileAlreadyExists) {
                setUploads(prev => prev.map(item => {
                    if (item.id === newItem.id) {
                        let itemm: UploadSession = {...newItem, status: "COMPLETE", uploadedSize: file.size};
                        console.log(itemm);
                        return itemm;
                    }
                    return item
                }));
                return;
            }
            const uploadId = data.url.split("/").pop()!;
            // save file before starting upload of chunks
            files.current.set(newItem.id, file);

            const uploadSession: UploadSessionResponse = await getUploadSession(uploadId);

            setUploads(prev => prev.map(item => {
                if (item.id === newItem.id)
                    return { ...item, uploadId: uploadId, status: "UPLOADING", uploadedSize: uploadSession.uploadedSize };
                return item;
            }));
            uploadedSizes.current.set(newItem.id, uploadSession.uploadedSize);

            if (uploadSession.status === "UPLOADING") {
                await uploadChunks(newItem.id, uploadId, file, abortController.signal, uploadSession.uploadedRanges)
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
    const mutationInit = useMutation({
        mutationFn: (file: File) => {
            return init(file, new AbortController());
        },
        onSuccess: (_, file) => {
            queryClient.invalidateQueries({ queryKey: ["uploads", getFileType(file)]});
        },
    });

    const mutationResume = useMutation({
        mutationFn: async (item : UploadSession) => {
            const file: File | undefined = files.current.get(item.id);
            if (!file) {
                alert("can't find the file, please select it and try again");
                item.abortController.abort();
                files.current.delete(item.id);
                return;
            }
            const ac = new AbortController();
            setUploads(prev => prev.map(i => {
                if (i.id === item.id) {
                    return { ...i, status: "UPLOADING", abortController: ac };
                }
                return i;
            }));
            try {
                const uploadSession = await getUploadSession(item.uploadId!);
                uploadedSizes.current.set(item.id, uploadSession.uploadedSize);

                return await uploadChunks(item.id, item.uploadId!, file, ac.signal, uploadSession.uploadedRanges);
            } catch (error) {
                if (axios.isAxiosError(error) && error.response?.status === 404) {
                    const res = await uploadSessionInit(file);
                    if(res.fileAlreadyExists) {
                        uploadedSizes.current.set(item.id, item.totalSize)
                        setUploads(prev => prev.map(i => i.id === item.id ? ({...i, status: "COMPLETE", uploadedSize: item.totalSize}) : i))
                    }
                }
            }
        },
        onSuccess: (_, item) => queryClient.invalidateQueries({ queryKey: ["uploads", item.type] })

    })


    return (
        <UploadContext.Provider
            value={{
                upload: (file: File) => {
                    mutationInit.mutate(file)
                },
                pause: (item: UploadSession) => {
                    setUploads(prev => prev.map(i => {
                        if (i.id === item.id) {
                            i.abortController.abort();
                            return { ...i, status: "PAUSED" };
                        }
                        return i;
                    })
                    );

                },
                cancel: (item : UploadSession) => { 
                    item.abortController.abort();
                    files.current.delete(item.id);
                    setUploads(prev => prev.filter(i => i.id !== item.id));
                },
                resume: (item: UploadSession) => {
                    mutationResume.mutate(item);
                },

                close: (id: UploadSession) => {
                    console.log("close: ", id);
                },
                uploads: uploads,
            }}
        >
            {children}
        </UploadContext.Provider>
    );
}
