import { Link } from "react-router-dom";

export function HomePage() {
    return (
        <>
            <Link to={"/register"}> go to register</Link>
            <Link to={"/login"}> go to login</Link>
        </>
    )
}