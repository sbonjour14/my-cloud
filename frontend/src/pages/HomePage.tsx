import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useUser } from "@/hooks/useUser";
import { useNavigate } from "react-router-dom";

export function HomePage() {
	const navigate = useNavigate();
	const {data: user, isLoading} = useUser();
	if(isLoading)
		return (
			<div className="flex justify-between items-center">
				<Spinner/>
			</div>
		)
	return (
		<div className="flex-col gap-5 justify-center mt-10">
			{user?.role === "ADMIN" && <Button variant={"default"}
				onClick={() => navigate("/test")}
			>
				test
			</Button>}
			<div>
				home page
			</div>
		</div>
	)
}
