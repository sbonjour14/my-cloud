import { RegisterForm } from "@/components/form/RegisterForm";
import { Logo } from "@/components/ui/logo";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function RegisterPage() {
    const navigate = useNavigate();
    return (
        <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-muted p-6 md:p-10">
            <div className="absolute left-0 top-0 p-2 text-foreground cursor-pointer"
                onClick={() => navigate(-1)}>
                <ArrowLeft />
            </div>
            <div className="flex w-full max-w-sm flex-col gap-6">
                <a href="#" className="flex items-center gap-2 self-center font-medium">
                    <div className="flex size-6 items-center justify-center rounded-md bg-primary text-primary-foreground">
                        <Logo />
                    </div>
                    Sbonjour Inc.
                </a>
                <RegisterForm/>
            </div>
        </div>
    )
}