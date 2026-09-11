import { LoginForm } from "@/components/form/LoginForm";
import { Button } from "@/components/ui/button";
import { Logo } from "@/components/ui/logo";
import { HomeIcon } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  const navigate = useNavigate();
  return (
    <div className="relative flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10 bg-background">
      <Button className={"absolute left-0 top-0 mt-5 ml-5"} variant={"ghost"}
        onClick={() => navigate("/")}
      >
        <HomeIcon />
      </Button>
      <div className="flex w-full max-w-sm flex-col gap-6">
        <a href="#" className="flex items-center gap-2 self-center font-medium">
          <div className="flex size-6 items-center justify-center rounded-md text-primary-foreground">
            <Logo />
          </div>
          Sbonjour Inc.
        </a>
        <LoginForm />
      </div>
    </div>
  )
}