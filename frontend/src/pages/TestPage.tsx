import { Button } from "@/components/ui/button";
import { UploadDialog } from "@/components/ui/upload-dialog";
import { useUploadFile } from "@/hooks/useUploadFile";
import { useRef } from "react";
import { Link } from "react-router-dom";

export function TestPage() {
    const { upload, isOpen, status, progress, close } = useUploadFile();
    const inputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const fichier = event.target.files?.[0];
        if (!fichier) return;
        upload(fichier);
        event.target.value = "";
    };

    return (
        <div className="w-full min-h-screen flex flex-col gap-5 items-center">
            <Link to={"/register"}> go to register</Link>
            <Link to={"/login"}> go to login</Link>
            <input
                ref={inputRef}
                className="hidden"
                type="file"
                onChange={handleFileChange}
            />
            <Button onClick={() => inputRef.current?.click()}>
                chose a file
            </Button>

            <Button onClick={() => {localStorage.removeItem("my-cloud-token"); window.location.reload()}}>
                log out
            </Button>
            <UploadDialog
                open={isOpen}
                progress={progress}
                status={status}
                onClose={close}
            />
        </div>
    )
}