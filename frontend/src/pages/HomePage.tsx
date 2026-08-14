import { Link } from "react-router-dom";

export function HomePage() {
    return (
        <div className="bg-muted w-full min-h-screen">
            <Link to={"/register"}> go to register</Link>
            <Link to={"/login"}> go to login</Link>
        </div>
    )
}