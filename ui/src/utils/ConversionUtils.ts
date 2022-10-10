class ConversionUtils {
    static getReadableSize(sizeInBytes: number): string {
        function round(num: number) {
            return Math.round(num * 100) / 100
        }

        if (sizeInBytes < 1_000) {
            return sizeInBytes + "B";
        }

        if (sizeInBytes < 1_000_000) {
            return round(sizeInBytes / 1000.0) + " kb";
        }

        if (sizeInBytes < 1_000_000_000) {
            return round(sizeInBytes / 1_000_000.0) + " mb";
        }

        return round(sizeInBytes / 1_000_000_000.0) + " gb";
    }
}

export default ConversionUtils
