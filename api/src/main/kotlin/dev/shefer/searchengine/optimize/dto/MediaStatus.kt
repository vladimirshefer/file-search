package dev.shefer.searchengine.optimize.dto

enum class MediaStatus {
    /**
     * Only source file, no optimized file.
     */
    SOURCE_ONLY,

    /**
     * Both source and optimized files exist.
     */
    OPTIMIZED,

    /**
     * Optimized file only. Source file does not exist.
     */
    OPTIMIZED_ONLY
}
