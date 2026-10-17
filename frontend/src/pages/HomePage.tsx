import UploadButton from "@/components/buttons/uploadButton";
import Header from "@/components/header/header";
import HomeGallery from "@/components/media-asset/HomeGallery";

export function HomePage() {
	return (
		<div className="w-full min-h-screen">
			<Header />
			<main>
				<HomeGallery/>
			</main>
			<UploadButton className="fixed right-5 bottom-10 h-18 w-18 rounded-2xl" />
		</div>
	);
}