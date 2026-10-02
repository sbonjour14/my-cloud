import { Button } from "@/components/ui/button";
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty";
import { Spinner } from "@/components/ui/spinner";
import { useMediaAssets, useMediaAssetsCount } from "@/hooks/mediaAsset";
import { useUploadFile } from "@/hooks/useUploadFile";
import { logoutUser } from "@/lib/http-api/auth";
import { useCallback, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeftIcon, CloudIcon, Home } from "lucide-react";
import { useUser } from "@/hooks/useUser";
import { useStorageUsage } from "@/hooks/useStorageUsage";
import { useUpload, type UploadSession } from "@/provider/upload";
import { UploadItem } from "@/components/upload/uploadItem";
import MediaAssetCard from "@/components/media-asset/media-asset-card";

export function TestPage() {
    const navigate = useNavigate();
    const { upload } = useUploadFile();
    const inputRef = useRef<HTMLInputElement>(null);
    const inputRef2 = useRef<HTMLInputElement>(null);

    const [page, setPage] = useState(0);
    const pageSize = 5;
    const { upload: uploadFile, cancel, pause, resume, uploads } = useUpload();

    const onPause = useCallback((item: UploadSession) => { pause(item); }, [pause]);
    const onResume = useCallback((item: UploadSession) => { resume(item); }, [resume]);
    const onCancel = useCallback((item: UploadSession) => { cancel(item); }, [cancel]);

    const { data: me, isLoading: meLoading } = useUser();
    const { data: mediaAssets, isLoading: mediaLoading } = useMediaAssets(page, pageSize, "DESC");
    const { data: storageUsed, isLoading: storageLoading } = useStorageUsage(me?.id);
    const { data: mediaAssetsCount, isLoading: mediaCountLoading } = useMediaAssetsCount();

    const isLoading = meLoading || mediaLoading || storageLoading || mediaCountLoading;

    const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const fichier = event.target.files?.[0];
        if (!fichier) return;
        upload(fichier);
        event.target.value = "";
    };

    const handleFileChange2 = (event: React.ChangeEvent<HTMLInputElement>) => {
        const fichier = event.target.files?.[0];
        if (!fichier) return;
        uploadFile(fichier);
        event.target.value = "";
    };

    return (
        <div className="w-full min-h-screen mt-5 flex flex-col gap-5 items-center">
            <div className="flex flex-row gap-2 items-center">
                <Link to={"/register"}> go to register</Link>
                <Link to={"/login"}> go to login</Link>
                <input
                    ref={inputRef}
                    className="hidden"
                    type="file"
                    onChange={handleFileChange}
                />
                <Button variant={"ghost"} onClick={() => navigate("/")}>
                    <Home />
                </Button>
                <input
                    ref={inputRef2}
                    className="hidden"
                    type="file"
                    onChange={handleFileChange2}
                />
                <Button onClick={async () => { await logoutUser(); navigate("/login") }}>
                    log out
                </Button>

                <Button onClick={() => inputRef2.current?.click()}>
                    upload file with useUpload (chunks)
                </Button>
            </div>

            <div>user : {me?.displayName}</div>
            <div>user id : {me?.id}</div>
            <div>storage : {((storageUsed ?? 0) / 1000000).toFixed(2)}MB</div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
                {uploads.map((uploadItem: UploadSession) => (
                    <UploadItem
                        key={uploadItem.id}
                        item={uploadItem}
                        onCancel={onCancel}
                        onPause={onPause}
                        onResume={onResume}
                    />
                ))}
            </div>

            {isLoading ? (
                <Spinner />
            ) : !mediaAssets || mediaAssets.length === 0 ? (
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
            ) : (
                <>
                    <div className="grid grid-cols-[repeat(auto-fill,minmax(9rem,1fr))] gap-3">
                        {mediaAssets.map((ma) => <MediaAssetCard key={ma.id} ma={ma} />)}
                    </div>

                    <div className="flex gap-2 items-center mt-5">
                        <Button onClick={() => setPage(page - 1)} disabled={page <= 0}>
                            <ArrowLeftIcon />
                        </Button>
                        <p>{page + 1}</p>
                        <Button
                            onClick={() => setPage(page + 1)}
                            disabled={(page + 1) * pageSize >= (mediaAssetsCount ?? 0)}
                        >
                            <ArrowLeftIcon className="rotate-180" />
                        </Button>
                    </div>

                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => inputRef.current?.click()}
                    >
                        Upload more
                    </Button>
                </>
            )}
        </div>
    );
}