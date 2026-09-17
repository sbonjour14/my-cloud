import { Button } from "@/components/ui/button";
import { Menu } from "lucide-react";
import { useUser } from "@/hooks/useUser";
import AvatarAction from "./avatar-action";

export default function Header() {
    const {data: user, isLoading} = useUser();

    if(isLoading || !user) return null;
    return (
        <div className=" sticky top-0 flex flex-row justify-between items-center h-16 sm:h-20 w-full px-4 border-b border-border mb-5">
            <Button>
                <Menu size={24}/>
            </Button>
            <p>HOME</p>
            {/* hamburger menu */}
            {/* search bar */}
            <AvatarAction user={user}/>
        </div>
    )
}