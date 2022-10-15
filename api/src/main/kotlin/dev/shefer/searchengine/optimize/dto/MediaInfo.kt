package dev.shefer.searchengine.optimize.dto

data class MediaInfo(
    val source: FileInfo?,
    val optimized: FileInfo?
) {
    val status: MediaStatus = source
        ?.let {
            optimized
                ?.let { MediaStatus.OPTIMIZED }
                ?: MediaStatus.SOURCE_ONLY
        }
        ?: optimized
            ?.let { MediaStatus.OPTIMIZED_ONLY }
        ?: throw IllegalStateException("Cannot be missing both source and optimized")
}
