import { me } from "@/lib/http-api/user";
import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";

export function ProtectedRoute() {
    const [isLoading, setIsloading] = useState<boolean>(true);
    const [isValid, setisValid] = useState<boolean>(false);

    useEffect(() => {
        async function checkUser() {
            try {
                await me();
                setisValid(true);
            } catch {
                setisValid(false);
            } finally {
                setIsloading(false);
            }

        }

        checkUser();

    }, []);

    if(isLoading)
        return null;

    if(!isValid)
        return <Navigate to={"/401"} replace/>

    return <Outlet/>
    
}