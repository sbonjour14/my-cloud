import UploadButton from "@/components/buttons/uploadButton";
import Header from "@/components/header/header";

export function HomePage() {
	return (
		<div className="w-full min-h-screen">
			<Header />
			<UploadButton className="absolute right-5 bottom-10 h-18 w-18 rounded-2xl" />
			<main>
			</main>
		</div>
	);
}