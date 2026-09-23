import { useUpload } from "@/provider/upload";
import { Button } from "../ui/button";
import { UploadIcon } from "lucide-react";
import { useRef } from "react";

export default function UploadButton({className}: {className?:string}) {
    const {upload} = useUpload();
    const inputref = useRef<HTMLInputElement>(null);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const fichier = event.target.files?.[0];
        if (!fichier) return;
        upload(fichier);
        event.target.value = "";

    };
    
    
    return(
        <>
            <input ref={inputref} type="file" onChange={handleFileChange} className="hidden" />

            <Button variant={"default"} className={className} onClick={() => inputref.current?.click()} >
                <UploadIcon className="size-6!"/>
            </Button>
        </>
    ) 
}