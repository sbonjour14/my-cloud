import { LoginForm } from "@/components/form/LoginForm";
import { Logo } from "@/components/ui/logo";

export default function LoginPage() {
  return (
    <div className="relative flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10 bg-background">
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