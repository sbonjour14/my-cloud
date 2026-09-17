import { RegisterForm } from "@/components/form/RegisterForm";

export default function RegisterPage() {
    return (
        <div className="flex min-h-svh flex-col items-center justify-center bg-background gap-6 p-6 md:p-10">
            <div className="flex w-full max-w-sm flex-col gap-6">
                <a href="#" className="flex items-center gap-2 self-center font-medium">
                    Sbonjour Inc.
                </a>
                <RegisterForm/>
            </div>
        </div>
    )
}