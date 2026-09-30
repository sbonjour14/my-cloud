import { type UploadSession } from "@/provider/upload";
import { Item, ItemActions, ItemMedia, ItemTitle } from "../ui/item";
import { Pause, Play, UploadCloudIcon, X } from "lucide-react";
import { memo } from "react";
import { cn } from "@/lib/utils";
import { Button } from "../ui/button";
import { Spinner } from "../ui/spinner";


export interface UploadItemProps {
	item: UploadSession,
	className?: string,
	onPause: (item: UploadSession) => void,
	onResume: (item: UploadSession) => void,
	onCancel: (item: UploadSession) => void,
}

export const UploadItem = memo(({ item, className, onPause, onResume, onCancel }: UploadItemProps) => {
	return (
		<>
			<Item className={cn(className, " w-60 flex flex-row items-center gap-2")} variant={"outline"}>
				<ItemMedia variant={"icon"}>
					<UploadCloudIcon />
				</ItemMedia>
					<ItemTitle className="flex flex-row gap-2 flex-1">
						<p className="truncate min-w-0 self-center">
							{item.name}
						</p>
					</ItemTitle>
					<ItemActions className="flex flex-row items-center">
						<p className="w-8.5 shrink-0 text-muted-foreground">
							{`${(item.uploadedSize * 100 / item.totalSize).toFixed(0)}%`}
						</p>
						{(item.status === "LOADING") &&
							<Button disabled variant={"ghost"} className={"p-0 m-0"}>
								<Spinner />
							</Button>
						}
						{(item.status === "PAUSED") &&
							<Button onClick={() => onResume(item)} variant={"ghost"} className={"p-0 m-0"} >
								<Play size={10} />
							</Button>
						}
						{item.status === "UPLOADING" &&
							<Button onClick={() => onPause(item)} variant={"ghost"} className={"p-0 m-0"}>
								<Pause size={10} />
							</Button>
						}
						<Button onClick={() => onCancel(item)} variant={"ghost"} className={"p-0 m-0"}>
							<X size={10} />
						</Button>
					</ItemActions>
			</Item>
		</>
	)
});
