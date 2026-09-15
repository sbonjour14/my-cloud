import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export function UnauthorizedPage() {
    const navigate = useNavigate();

    return (
        <div className="flex min-h-svh flex-col items-center justify-center gap-4 text-center px-4">
            <p className="text-sm font-medium text-muted-foreground">401</p>
            <h1 className="text-3xl font-heading font-medium">You're not logged in</h1>
            <p className="text-muted-foreground max-w-sm">
                You need to be logged in to access this page.
            </p>
            <Button onClick={() => navigate("/login")}>Log in</Button>
        </div>
    );
}