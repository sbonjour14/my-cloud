import { LoginForm } from "@/components/form/LoginForm";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useNavigate } from "react-router-dom";

export function LoginPage() {
    const navigate = useNavigate();
    return (
        <>
            <div className="flex flex-col items-center justify-center min-h-screen py-2 gap-4">
                    <LoginForm />
                    <Separator></Separator>
                    <Button onClick={() => navigate("/register")}> go to register</Button>
                    <Button onClick={() => localStorage.clear()}>Clear Local Storage</Button>
                    <Button onClick={() => console.log(localStorage.getItem("my-cloud-token"))}>Log Token</Button>
            </div>
            <div className="flex flex-col items-center justify-center min-h-screen py-2 gap-4">
                <Button onClick={() => navigate("/")}> go to home</Button>
            </div>
        </>
    )
}