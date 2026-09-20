import { Button } from "@/components/ui/button";

export default function RegisterPage() {
    return (
        <div className="flex min-h-svh flex-col items-center justify-center bg-background gap-6 p-6 md:p-10">
            <div className="flex w-full max-w-sm flex-col gap-6 items-center">
                <a href="#" className="flex items-center gap-2 self-center font-medium">
                    Sbonjour Inc.
                </a>
                <p>
                    you cannot register, this cloud is for personnal use
                </p>
                <Button onClick={() => {window.location.href = "/login"}} variant={"secondary"} className={"px-5"}>
                    go to login
                </Button>
            </div>
        </div>
    )
}