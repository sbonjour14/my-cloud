import { type UploadSession  } from "@/provider";
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
		<Item className={cn(className)} variant={"outline"}>
			<ItemMedia variant={"icon"}>
				<UploadCloudIcon />
			</ItemMedia>
			<ItemTitle className="flex flex-row justify-between">
				<p>
					{item.name}	
				</p>
				<p>
					{`${(item.uploadedSize*100/item.totalSize).toFixed(0)}%`}
				</p>
			</ItemTitle>
			<ItemActions>
				{(item.status === "PAUSED" || item.status === "LOADING") &&
				<Button onClick={() => onResume(item)} variant={"ghost"}>
					<Play size={10}/>
				</Button>
				}
				{item.status === "UPLOADING" &&
				<Button onClick={() => onPause(item)} variant={"ghost"}>
					<Pause size={10}/>
				</Button>
				}
				{item.status === "COMPLETE" &&
				<Button onClick={() => onCancel(item)} variant={"ghost"}>
					<X size={10}/>
				</Button>

				}
			</ItemActions>
		</Item>
	</>
	)
});
