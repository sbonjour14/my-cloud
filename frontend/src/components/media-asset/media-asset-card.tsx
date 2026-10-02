import type { MediaAssetResponse } from "@/types";
import { Card, CardContent } from "@/components/ui/card";
import { Image as ImageIcon, TrashIcon, Video } from "lucide-react";
import { Button } from "../ui/button";
import { useDeleteMediaAsset } from "@/hooks/mediaAsset";
import axios from "axios";
import type { BaseUIEvent } from "@base-ui/react/types";
import { useState } from "react";

export default function MediaAssetCard({ ma }: { ma: MediaAssetResponse }) {
    const TypeIcon = ma.type === "VIDEO" ? Video : ImageIcon;

    const [isDisable, setIsDisable] = useState<boolean>(false);
    const { deleteAsset } = useDeleteMediaAsset();

    const handleClick = (event: BaseUIEvent<React.MouseEvent<HTMLButtonElement, MouseEvent>>) => {
        setIsDisable(true);
        event.stopPropagation();
        try {
            deleteAsset(ma.id);
        } catch (e) {
            if(axios.isAxiosError(e))
                alert(e.message);
            else 
                alert("An error occured while deleting file")
        } finally {
            setIsDisable(false);
        }
    }


    return (
        <Card className="group w-full min-w-0 cursor-pointer gap-0 rounded-2xl border-0 bg-card p-2 shadow-none ring-0! transition-colors duration-200 hover:bg-accent sm:p-3">
            <CardContent className="flex flex-col gap-2 p-0 sm:gap-3">
                <div className="flex items-start gap-2.5 px-1 py-1.5 sm:py-2">
                    <TypeIcon className="size-5 shrink-0 text-destructive" />
                    <p
                        className="line-clamp-2 min-w-0 flex-1 break-all text-xs font-medium text-card-foreground sm:text-sm"
                        title={ma.filename}
                    >
                        {ma.filename}
                    </p>
                    <Button
                        variant="ghost"
                        size="icon"
                        className="-mr-1 -mt-1 size-7 shrink-0 rounded-full"
                        onClick={(e) => handleClick(e)}
                        disabled={isDisable}
                    >
                        <TrashIcon className="size-4" />
                    </Button>
                </div>

                <div className="aspect-video w-full overflow-hidden rounded-xl bg-background">
                    {ma.hasThumbnail ? (
                        <img
                            src={import.meta.env.VITE_API_URL + ma.thumbnailUrl}
                            alt={ma.filename}
                            loading="lazy"
                            className="size-full object-cover transition-transform duration-300 group-hover:scale-105"
                        />
                    ) : (
                        <div className="flex size-full items-center justify-center">
                            <TypeIcon
                                className="size-1/4 text-muted-foreground/40"
                                strokeWidth={1.25}
                            />
                        </div>
                    )}
                </div>
            </CardContent>
        </Card>
    );
}