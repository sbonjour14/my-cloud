import { CloudUploadIcon } from "lucide-react";

interface LogoProps {
    size?: number;
}

export function Logo({ size = 40}: LogoProps) {
    return (
        <CloudUploadIcon size={size}/>
    );
}