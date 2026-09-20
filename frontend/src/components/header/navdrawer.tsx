import { Menu, UploadIcon } from "lucide-react"
import { Drawer, DrawerContent, DrawerFooter, DrawerHeader, DrawerTrigger } from "../ui/drawer"
import { Button } from "../ui/button"
import { cn } from "@/lib/utils"
import { Separator } from "../ui/separator"
import type { ReactNode } from "react"
import { useNavigate } from "react-router-dom"
import { Spinner } from "../ui/spinner"
import { Progress } from "../ui/progress"
import type { UserResponse } from "@/types"
import { useStorageUsage } from "@/hooks/useStorageUsage"


export interface NavDrawerProps {
    className?: string
    user: UserResponse
}

export type NavDrawerContentItem = {
    id: number
    icon: ReactNode,
    label: string,
    goTo: string,
}

const content: NavDrawerContentItem[] = [
    {
        id: 1,
        icon: <UploadIcon size={16} />,
        label: "Uploads",
        goTo: "my-uploads"
    }
];

function formatUnit(value: number, unit: string): string {
    return value < 10 ? `${value.toFixed(1)} ${unit}` : `${value.toFixed(0)} ${unit}`;
}

function renderStorageUsed(used: number): string {
    const KB = 1024;
    const MB = KB * 1024;
    const GB = MB * 1024;
    const TB = GB * 1024;

    if (used < MB) {
        return formatUnit(used / KB, "KB");
    }

    if (used < 0.1 * GB) {
        return formatUnit(used / MB, "MB");
    }

    if (used < 0.1 * TB) {
        return formatUnit(used / GB, "GB");
    }

    return formatUnit(used / TB, "TB");
}

export default function NavDrawer({ className, user }: NavDrawerProps) {
    const LIMIT = 1024 * 1024 * 1024 * 1024;
    const navigate = useNavigate();
    const trigger = <Button variant={"ghost"}> <Menu className="size-6.25!" /></Button>
    const { data, isLoading } = useStorageUsage(user.id);
    const usedRation = ((data ?? 1) / LIMIT) * 100;
    const percentage: number = Math.min(usedRation, 100);

    return (
        <Drawer
            swipeDirection="left"
        >
            <DrawerTrigger render={trigger} />
            <DrawerContent className={cn("w-72 max-w-72 [--drawer-inset:0px] rounded-none rounded-r-2xl", className)}>
                <DrawerHeader className="h-16">
                    <span className="font-bold text-lg">
                        Sbonjour Cloud
                    </span>
                </DrawerHeader>
                <Separator></Separator>
                <div className="flex flex-col gap-5 w-full">
                    {content.map(item => (
                        <div key={item.id} className="cursor-pointer bg-transparent w-full min-w-full flex flex-row justify-start items-center gap-5 pl-5 py-5"
                            onClick={() => navigate(item.goTo)}
                        >
                            {item.icon}
                            <span>{item.label}</span>
                        </div>

                    ))}
                </div>
                <DrawerFooter className="flex justify-center items-center">
                    {
                        isLoading ?
                            <Spinner /> :

                            <>
                                <Progress
                                    value={percentage}
                                    className={cn(
                                        "w-full",
                                        percentage > 80 && "**:data-[slot=progress-indicator]:bg-destructive!"
                                    )}
                                />
                                <span className="text-xs self-start">{renderStorageUsed(data ?? 0)} used of 1TB</span>
                            </>
                    }
                </DrawerFooter>
            </DrawerContent>
        </Drawer>
    )
}