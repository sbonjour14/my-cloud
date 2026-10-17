import type { MediaAssetSummaryResponse } from "@/types";
import { Card, CardContent } from "@/components/ui/card";
import { Image as ImageIcon, TrashIcon, Video } from "lucide-react";
import { Button } from "../ui/button";
import { useDeleteMediaAsset } from "@/hooks/mediaAsset";
import axios from "axios";
import type { BaseUIEvent } from "@base-ui/react/types";
import { useState } from "react";

export default function MediaAssetCard({ ma }: { ma: MediaAssetSummaryResponse }) {
    const TypeIcon = ma.type === "VIDEO" ? Video : ImageIcon;

    const [isDisable, setIsDisable] = useState<boolean>(false);
    const { deleteAsset } = useDeleteMediaAsset();

    const getThumbnailUrl = () => {
        return import.meta.env.VITE_API_URL + '/media/' + ma.id + '/thumbnail';
    }

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
        <Card className="group w-full min-w-0 cursor-pointer gap-0 p-0! rounded-none" >
            <CardContent className="p-0! ring-0! border-0!">

                <div className="aspect-square w-full overflow-hidden bg-background relative">
                <div className="flex items-start gap-2.5 px-1 py-1.5 sm:h-16 sm:py-2 bg-transparent absolute top-0 left-0 z-30">
                    <TypeIcon className="size-5 shrink-0 text-destructive" />
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
                    {ma.hasThumbnail ? (
                        <img
                            src={getThumbnailUrl()}
                            alt={"image"}
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