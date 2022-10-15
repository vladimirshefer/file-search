package dev.shefer.searchengine.optimize.dto

data class MediaInfo(
    val source: FileInfo?,
    val optimized: FileInfo?,
    val status: MediaStatus = mediaStatus(source, optimized)
) {

    companion object {
        private fun mediaStatus(source: FileInfo?, optimized: FileInfo?) =
            if (source != null) {
                if (optimized != null) {
                    MediaStatus.OPTIMIZED
                } else {
                    MediaStatus.SOURCE_ONLY
                }
            } else {
                if (optimized != null) {
                    MediaStatus.OPTIMIZED_ONLY
                } else {
                    throw IllegalStateException("Cannot be missing both source and optimized")
                }
            }
    }
}
