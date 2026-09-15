import { Button } from "@/components/ui/button";
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Spinner } from "@/components/ui/spinner";
import { useDeleteMediaAsset, useMediaAssets } from "@/hooks/useMediaAssets";
import { useUploadFile } from "@/hooks/useUploadFile";
import { logoutUser } from "@/lib/http-api/auth";
import { useCallback, useRef} from "react";
import { Link, useNavigate } from "react-router-dom";
import { CloudIcon } from "lucide-react";
import { useUser } from "@/hooks/useUser";
import { useStorageUsage } from "@/hooks/useStorageUsage";
import { Image } from "lucide-react";
import { useUpload, type UploadSession } from "@/provider/";
import { UploadItem } from "@/components/upload/uploadItem";

export function TestPage() {
    const navigate = useNavigate();
    const { upload } = useUploadFile();
    const inputRef = useRef<HTMLInputElement>(null);
    const inputRef2 = useRef<HTMLInputElement>(null);

    const {upload: uploadFile, cancel, pause, resume, uploads} = useUpload();

    const onPause = useCallback((item: UploadSession) => {console.log("PAUSE: ", item); pause(item); }, []);
    const onResume = useCallback((item: UploadSession) => {console.log("RESUME: ", item); resume(item); }, []);
    const onCancel = useCallback((item : UploadSession) => {console.log("CANCEL: ", item); cancel(item); }, []);

    const {data: me, isLoading: meLoading} = useUser();
    const {data: mediaAssets, isLoading: mediaLoading} = useMediaAssets();
    const {data: storageUsed, isLoading: storageLoading} = useStorageUsage(me?.id);

    const {deleteAsset}= useDeleteMediaAsset();


    const isLoading = meLoading && mediaLoading && storageLoading;

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const fichier = event.target.files?.[0];
        if (!fichier) return;
        upload(fichier);
        event.target.value = "";
    };

    const handleFileChange2 = (event: React.ChangeEvent<HTMLInputElement>) => {
        console.log("handleFileChange2");
        const fichier = event.target.files?.[0];
        if (!fichier) return;
        uploadFile(fichier);
        event.target.value = "";
    }

    function handleDeleteMediaAsset(id: string, filename: string) {
        try {
            console.log("deleting: ",filename);
            const item = uploads.find(i => i.name === filename);
            if(item)cancel(item);
            deleteAsset(id);
        } catch (error) {
            console.error("Error deleting media asset:", error);
        }
    }

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
            <input
                ref={inputRef2}
                className="hidden"
                type="file"
                onChange={handleFileChange2}
            />
            <Button onClick={async () => {await logoutUser(); navigate("/login")}}>
                log out
            </Button>


            <Button onClick={() => inputRef2.current?.click()}>
                upload file with useUpload (chunks)
            </Button>

            <div>
                user : {me?.displayName}
            </div>

            <div>
                user id : {me?.id}
            </div>


            <div>
                storage : {((storageUsed?? 0) / 1000000).toFixed(2)}MB
            </div>

            <div className= "grid grid-cols-2 gap-3">
                {
                    uploads.map((uploadItem: UploadSession) => <UploadItem key={uploadItem.id} item={uploadItem} onCancel={onCancel} onPause={onPause} onResume={onResume}/>)
                }
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
                    <>
                        <div className="mt-5 grid grid-cols-2 gap-3">
                            {mediaAssets.map((ma, idx) => (
                                uploads.find(i => i.name === ma.filename && !(i.status === "COMPLETE" && i.uploadedSize === i.totalSize)) ? null :
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
                                    <div className="text-sm text-muted-foreground">{ma.filename}</div>
                                    <Button
                                        variant="destructive"
                                        size="sm"
                                        onClick={() => handleDeleteMediaAsset(ma.id, ma.filename)}
                                    >delete</Button>
                                </div>
                            ))}
                        </div>
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => inputRef.current?.click()}
                            >
                                Upload more
                            </Button>

                    </>
            }

        </div>
    )
}
