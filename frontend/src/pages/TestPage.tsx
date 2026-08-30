import { Button } from "@/components/ui/button";
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Spinner } from "@/components/ui/spinner";
import { useMediaAssets } from "@/hooks/useMediaAssets";
import { useUploadFile } from "@/hooks/useUploadFile";
import { logoutUser } from "@/lib/http-api/auth";
import { useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { CloudIcon } from "lucide-react";
import { useUser } from "@/hooks/useUser";
import { useStorageUsage } from "@/hooks/useStorageUsage";
import { Image } from "lucide-react";

export function TestPage() {
    const navigate = useNavigate();
    const { upload } = useUploadFile();
    const inputRef = useRef<HTMLInputElement>(null);

    const {data: me, isLoading: meLoading} = useUser();
    const {data: mediaAssets, isLoading: mediaLoading} = useMediaAssets();
    const {data: storageUsed, isLoading: storageLoading} = useStorageUsage(me?.id);


    const isLoading = meLoading && mediaLoading && storageLoading;

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

            <div>
                storage : {((storageUsed?? 0) / 1000000).toFixed(2)}MB
            </div>

            {
                isLoading ? 
                    <Spinner/> :

                    !mediaAssets || mediaAssets.length == 0 ?
                        <Empty className="border border-dashed">
                            <EmptyHeader>
                                <EmptyMedia variant="icon">
                                    <CloudIcon />
                                </EmptyMedia>
                                <EmptyTitle>Cloud Storage Empty</EmptyTitle>
                                <EmptyDescription>
                                    Upload files to your cloud storage to access them anywhere.
                                </EmptyDescription>
                            </EmptyHeader>
                            <EmptyContent>
                                <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => inputRef.current?.click()}
                                >
                                    Upload Files
                                </Button>
                            </EmptyContent>
                        </Empty>


                    :
                        <div className="mt-5 grid grid-cols-2 gap-3">
                            {mediaAssets.map((ma, idx) => (
                                <div key={idx}>
                                    {
                                        ma.hasThumbnail ?
                                        <img
                                            src={import.meta.env.VITE_API_URL + ma.thumbnailUrl}
                                            className="h-40 w-40 object-cover"
                                        />
                                        :
                                            <div className="h-40 w-40 rounded-lg bg-muted animate-pulse flex items-center justify-center">
                                                <Image className="h-10 w-10 text-muted-foreground/40" strokeWidth={1.5} />
                                            </div>
                                    }
                                </div>
                            ))}
                        </div>
            }

        </div>
    )
}