import { Button } from "@base-ui/react";
import { Avatar, AvatarFallback} from "./ui/avatar";
import { Menu } from "lucide-react";
import { useUser } from "@/hooks/useUser";

export default function Header() {
    const {data: user} = useUser();
    return (
        <div className=" sticky top-0 flex flex-row justify-between items-center h-16 sm:h-20 w-full px-4 border-b border-border mb-5">
            <Button>
                <Menu size={24}/>
            </Button>
            <p>HOME</p>
            {/* hamburger menu */}
            {/* search bar */}
            <Avatar >
                <AvatarFallback className={"bg-foreground text-background font-bold"}>{user?.displayName.charAt(0)}</AvatarFallback>
            </Avatar>
        </div>
    )
}