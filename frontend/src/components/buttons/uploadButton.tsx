import { useUpload } from "@/provider/upload";
import { Button } from "../ui/button";
import { UploadIcon } from "lucide-react";
import { useRef } from "react";

export default function UploadButton({className}: {className?:string}) {
    const {upload} = useUpload();
    const inputref = useRef<HTMLInputElement>(null);

    
    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const files = event.target.files;
        if (!files || files.length === 0) return;

        for (const file of files) {
            upload(file);
        }

        event.target.value = "";
    };
    
    return(
        <>
            <input ref={inputref} type="file" multiple onChange={handleFileChange} className="hidden" />

            <Button variant={"default"} className={className} onClick={() => inputref.current?.click()} >
                <UploadIcon className="size-6!"/>
            </Button>
        </>
    ) 
}