import { Button } from "@/components/ui/button";
import { useUploadFile } from "@/hooks/useUploadFile";
import { logoutUser } from "@/lib/http-api/auth";
import { useRef } from "react";
import { Link, useNavigate } from "react-router-dom";

export function TestPage() {
    const navigate = useNavigate();
    const { upload } = useUploadFile();
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

            <Button onClick={async () => {await logoutUser(); navigate("/login")}}>
                log out
            </Button>
        </div>
    )
}