import type { ByteRange } from "@/types";
import { createSHA256 } from "hash-wasm";

export const CHUNK_SIZE_IMAGE = 1024 * 1024 * 2;
export const CHUNK_SIZE_VIDEO = 1024 * 1024 * 20;


export function getChunkSizeof(file: File) {
    return file.type.startsWith("video") ? CHUNK_SIZE_VIDEO : CHUNK_SIZE_IMAGE;
}

export async function calculateChecksum(file: File){
  const hasher = await createSHA256();
  hasher.init();
  const chunkSize = file.type.startsWith("video") ? CHUNK_SIZE_VIDEO : CHUNK_SIZE_IMAGE;

  for (let offset = 0; offset < file.size; offset += chunkSize) {
    const chunk = file.slice(offset, offset + chunkSize);
    const buffer = await chunk.arrayBuffer();
    hasher.update(new Uint8Array(buffer));
  }

  return hasher.digest("hex");
}

export function getNonUploadedRanges(ranges: ByteRange[], totalSize: number, chunkSize: number) : ByteRange[] {
    const res : ByteRange[]= [];
    let i = 0;
    let start = 0;
    let end = start + chunkSize - 1;
    while (i < ranges.length) {
        const range = ranges[i];
        if(end < range.byteStart) {
            res.push({byteStart: start, byteEnd: end});
            start = end + 1;
            end = start + chunkSize - 1;
        } else {
            start = range.byteEnd + 1;
            end = start + chunkSize - 1;
            i++;
        }
    }
    while(start < totalSize) {
        res.push({byteStart: start, byteEnd: Math.min(end, totalSize - 1)});
        start = end + 1;
        end = start + chunkSize - 1;
    }
    return res;
}