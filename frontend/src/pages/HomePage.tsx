import UploadButton from "@/components/buttons/uploadButton";
import Header from "@/components/header/header";
import MediaAssetScroll from "@/components/media-asset/media-asset-scroll";

export function HomePage() {
	return (
		<div className="w-full min-h-screen">
			<Header />
			<main>
				<MediaAssetScroll/>
			</main>
			<UploadButton className="fixed right-5 bottom-10 h-18 w-18 rounded-2xl" />
		</div>
	);
}