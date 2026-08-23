import { me } from "@/lib/http-api/user";
import type { UserResponse } from "@/types";
import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";

export function ProtectedRoute() {
    const [isLoading, setIsloading] = useState<boolean>(true);
    const [isValid, setisValid] = useState<boolean>(false);

    useEffect(() => {
        async function checkUser() {
            const token = localStorage.getItem("my-cloud-token");
            if (!token) {
                setisValid(false);
                setIsloading(false);
                return;
            }

            const user: UserResponse = await me();
            if(!user) {
                setisValid(false);
            } else {
                setisValid(true);
            }
            setIsloading(false);
        }

        checkUser();

    }, []);
    if(isLoading)
        return <p>loading</p>;

    if(!isValid)
        return <Navigate to={"/login"} replace/>

    return <Outlet/>
    
}