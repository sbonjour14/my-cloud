import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export function NotFoundPage() {
    const navigate = useNavigate();

    return (
        <div className="flex min-h-svh flex-col items-center justify-center gap-4 text-center px-4 bg-muted">
            <p className="text-sm font-medium text-muted-foreground">404</p>
            <h1 className="text-3xl font-heading font-medium">Page not found</h1>
            <p className="text-muted-foreground max-w-sm">
                The page you're looking for doesn't exist or has been moved.
            </p>
            <Button onClick={() => navigate("/")}>Back to home</Button>
        </div>
    );
}