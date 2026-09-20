import { useUser } from "@/hooks/useUser";
import AvatarAction from "../avatar-action";
import NavDrawer from "./navdrawer";

export default function Header() {
    const {data: user, isLoading} = useUser();

    if(isLoading || !user) return null;
    return (
        <div className=" sticky top-0 flex flex-row justify-between items-center h-16 sm:h-20 w-full px-4 border-b border-border mb-5">
            <NavDrawer className="h-screen" user={user}/>
            <p>HOME</p>
            {/* hamburger menu */}
            {/* search bar */}
            <AvatarAction user={user}/>
        </div>
    )
}