import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

export function HomePage() {
	const navigate = useNavigate();
	return (
		<div className="flex-col gap-5 justify-center mt-10">
			<Button variant={"default"}
				onClick={() => navigate("/test")}
			>
				test
			</Button>
			<div>
				home page
			</div>
		</div>
	)
}
