import { FormRegister } from "@/components/form";
import { Separator } from "@/components/ui/separator";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export function RegisterPage() {
    const navigate = useNavigate();
    return (
    <>
        <main className="flex justify-center flex-col items-center gap-4">
            <FormRegister/>
            <Separator orientation="horizontal"/>
            <Button onClick={() => navigate("/login")} >go to log in</Button>
        </main>
    </>
    )
}