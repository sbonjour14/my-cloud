export function formatUnit(value: number, unit: string): string {
    return value < 10 ? `${value.toFixed(1)} ${unit}` : `${value.toFixed(0)} ${unit}`;
}

export function renderStorageUsed(used: number): string {
    console.log(used);
    const KB = 1024;
    const MB = KB * 1024;
    const GB = MB * 1024;
    const TB = GB * 1024;

    if (used < MB) {
        return formatUnit(used / KB, "KB");
    }

    if (used < 0.1 * GB) {
        return formatUnit(used / MB, "MB");
    }

    if (used < 0.1 * TB) {
        return formatUnit(used / GB, "GB");
    }

    return formatUnit(used / TB, "TB");
}