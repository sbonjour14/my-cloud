import type { UserResponse } from "@/types"
import { logoutUser } from "@/lib/http-api/auth"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from "./ui/dropdown-menu"
import { Avatar, AvatarFallback } from "./ui/avatar"
import { LogOutIcon, SettingsIcon, UserIcon } from "lucide-react"
import { cn } from "@/lib/utils"

export interface AvatarActionProps {
    className?: string,
    user: UserResponse,
}

export default function AvatarAction({className, user} : AvatarActionProps) {
    const handleLogout = () => {
        logoutUser();
        window.location.href = "/login";
    }

    const avatar =  <Avatar className={"cursor-pointer "}>
                        <AvatarFallback className={"text-background bg-foreground"}>
                            {user.displayName.charAt(0)}
                        </AvatarFallback>
                    </Avatar>

    return (
        <DropdownMenu>
            <DropdownMenuTrigger render={<button>{avatar}</button>}/>
            <DropdownMenuContent className={cn(className)}>
                <DropdownMenuItem className={"cursor-pointer"}>
                    <UserIcon/>
                    Profile
                </DropdownMenuItem>
                <DropdownMenuItem className={"cursor-pointer"}>
                    <SettingsIcon/>
                    Settings 
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem variant="destructive" onClick={handleLogout} className={"cursor-pointer"}>
                    <LogOutIcon/>
                    Log Out
                </DropdownMenuItem>
            </DropdownMenuContent>
        </DropdownMenu>
    )
}