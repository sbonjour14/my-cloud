import { useEffect, useState } from "react";
import { getUploads, type Upload } from "@/lib/http-api/upload/getUpload";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { renderStorageUsed } from "@/utils/formatBytes";
export default function UploadsPage() {
    const [uploads, setUploads] = useState<Upload[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const getData = async () => {
            const data = await getUploads();
            setUploads(data);
            setIsLoading(false);
        };

        setIsLoading(true);
        getData();
    }, []);

    return (
        <div className="p-6">
            <Button variant={"ghost"} className={"absolute top-5 left-5"} onClick={() => navigate(-1)}>
                <ArrowLeft/>
            </Button>
            <h1 className="text-2xl font-bold mb-6">My uploads</h1>

            {isLoading ? (
                <p className="text-muted-foreground">loading... <Spinner/></p>
            ) : uploads.length === 0 ? (
                <p className="text-muted-foreground">
                    you did not upload anything
                </p>
            ) : (
                <div className="grid gap-3">
                    {uploads.map((upload) => (
                        <div
                            key={upload.id}
                            className="flex items-center justify-between rounded-lg border p-4"
                        >
                            <div>
                                <p className="font-medium">
                                    {upload.filename}
                                </p>
                                <p className="text-sm text-muted-foreground">
                                    {renderStorageUsed(upload.totalSize)}
                                </p>
                            </div>

                            <p className="text-sm text-muted-foreground">
                                {new Date(upload.createdAt).toLocaleDateString()}
                            </p>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
