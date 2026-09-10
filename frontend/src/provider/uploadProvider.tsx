import { api } from "@/lib/axios";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { createContext, useContext, useRef, type ReactNode } from "react";
import { createSHA256 } from "hash-wasm";
import axios from "axios";

const chunkSize: number = 1024*1024*2;

type ByteRange = {
    startByte : number;
    endByte : number;
}

// return the ranges that are not yet uploaded
// ranges : the ranges that are already uploaded
function getStart(ranges: ByteRange[], totalSize: number) : ByteRange[] {
    const res : ByteRange[]= [];
    let i = 0;
    let start = 0;
    let end = start + chunkSize - 1;
    while (i < ranges.length) {
        const range = ranges[i];
        if(end < range.startByte) {
            res.push({startByte: start, endByte: end});
            start = end + 1;
            end = start + chunkSize - 1;
        } else {
            start = range.endByte + 1;
            end = start + chunkSize - 1;
            i++;
        }
    }
    while(start < totalSize) {
        res.push({startByte: start, endByte: Math.min(end, totalSize - 1)});
        start = end + 1;
        end = start + chunkSize - 1;
    }
    return res;
}

async function sha256Stream(file: File, chunkSize: number): Promise<string> {
  const hasher = await createSHA256();
  hasher.init();

  for (let offset = 0; offset < file.size; offset += chunkSize) {
    const chunk = file.slice(offset, offset + chunkSize);
    const buffer = await chunk.arrayBuffer();
    hasher.update(new Uint8Array(buffer));
  }

  return hasher.digest("hex");
}


async function uploadChunks(file: File, signal : AbortSignal, ranges: ByteRange[], url: string) {
    console.log("uploading: ", file.name)

    let size = file.size;
    let start: number; let end: number;
    let notUploadedRanges = getStart(ranges, size);
    let i = 0;
    while(!signal.aborted) {
        console.log("uploading chunk ", i, " of ", notUploadedRanges.length);
        if(i >= notUploadedRanges.length) {
            break;
        }
        start = notUploadedRanges[i].startByte;
        end = notUploadedRanges[i].endByte;
        i++;
        let chunk: Blob = file.slice(start, Math.min(end+1, size), file.type);
        let formdata: FormData = new FormData();
        formdata.append("chunk", chunk);
        try {
            const result = await api.post(url, formdata, {signal,  headers: {"Content-Range": `bytes ${start}-${end}/${size}`}});
            if (result.status === 200)
                console.log("chunk success")
            else if (result.status === 201) {
                console.log("file uploaded")
            }

        } catch (error) {
            if (axios.isAxiosError(error)) {
                alert("upload failed, please retry");
                return;
            }
        }
    }
    if(signal.aborted) {
        console.log("upload aborted");
        return;
    }
    console.log("done!");
}


async function init(file: File, signal: AbortSignal) {
    const checksum: string = await sha256Stream(file, chunkSize);
    try {
        const res = await api.post("/uploads/init", {
            filename: file.name,
            mediaType: file.type,
            totalSize: file.size,
            checksum: checksum
        })
        const data: { fileAlreadyExists: boolean, url: string } = res.data;
        console.log("file already exists: ", data.fileAlreadyExists, " url: ", data.url);
        if (data.fileAlreadyExists) {
            alert(data.url);
            return;
        }
        const res2 = await api.get(data.url);
        const data2: { status: string, totalSize: number, uploadedSize: number, uploadedRanges: ByteRange[] } = res2.data;
        if (res2.status === 200 && data2.status === "UPLOADING") {
            await uploadChunks(file, signal, data2.uploadedRanges, data.url)
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
    pause: () => void;
    cancel: () => void;
} | null>(null);


export function UploadProvider({children}: {children: ReactNode}) {
    const queryClient = useQueryClient();
    const abortControllerRef = useRef<AbortController | null>(null);

    const mutation = useMutation({
        mutationFn: (file: File) => {
            abortControllerRef.current = new AbortController();
            return init(file, abortControllerRef.current.signal);
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["uploads"] }),
    });
    return (
        <UploadContext.Provider
            value={{
                upload: (file) => mutation.mutate(file),
                pause: () => {abortControllerRef.current?.abort()},
                cancel: () => {abortControllerRef.current?.abort(); mutation.reset()},
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

