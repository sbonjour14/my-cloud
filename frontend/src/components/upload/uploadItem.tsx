import { type UploadSession  } from "@/provider/upload";
import { Item, ItemActions, ItemMedia, ItemTitle } from "../ui/item";
import { Pause, Play, UploadCloudIcon, X } from "lucide-react";
import { memo } from "react";
import { cn } from "@/lib/utils";
import { Button } from "../ui/button";


export interface UploadItemProps  {
	item: UploadSession,
	className?: string,
	onPause: (item: UploadSession) => void,
	onResume: (item: UploadSession) => void,
	onCancel: (item: UploadSession) => void,
}

export const UploadItem = memo(({ item, className, onPause, onResume, onCancel }: UploadItemProps) => {
	return (
	<>
		<Item className={cn(className, "flex flex-row items-center justify-between gap-2")} variant={"outline"}>
			<ItemMedia variant={"icon"}>
				<UploadCloudIcon />
			</ItemMedia>

			<ItemTitle className="flex flex-row items-center justify-between gap-2 flex-1 min-w-0">
				<p className="truncate min-w-0">
					{item.name}
				</p>
				<p className="shrink-0 text-muted-foreground">
					{`${(item.uploadedSize * 100 / item.totalSize).toFixed(0)}%`}
				</p>
			</ItemTitle>

			<ItemActions className="flex flex-row items-center gap-1 shrink-0">
				{(item.status === "PAUSED" || item.status === "LOADING") &&
					<Button onClick={() => onResume(item)} variant={"ghost"}>
						<Play size={10} />
					</Button>
				}
				{item.status === "UPLOADING" &&
					<Button onClick={() => onPause(item)} variant={"ghost"}>
						<Pause size={10} />
					</Button>
				}
				<Button onClick={() => onCancel(item)} variant={"ghost"}>
					<X size={10} />
				</Button>
			</ItemActions>
		</Item>
	</>
	)
});
