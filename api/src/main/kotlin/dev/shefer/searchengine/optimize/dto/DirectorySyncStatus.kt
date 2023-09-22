package dev.shefer.searchengine.optimize.dto

enum class DirectorySyncStatus {

    /**
     * Status is undefined or unknown yet.
     * Usually used for new directories which were not scanned yet.
     */
    NONE,

    /**
     * Directory has no media in files subtree ot directory does not exist.
     * */
    EMPTY,

    /**
     * Only source files exist. No optimized media exist.
     */
    OPTIMIZATION_NOT_STARTED,

    /**
     * Optimized media is a subset of source media.
     */
    OPTIMIZATION_PARTIALLY_COMPLETED,

    /**
     * The set of optimized media equals the set of sources media.
     */
    FULLY_OPTIMIZED,

    /**
     * Source media is a subset of optimized media.
     */
    SOURCES_PARTIALLY_REMOVED,

    /**
     * Only optimized media exists, not source media exist.
     */
    SOURCES_REMOVED,

    /**
     * Has some source media to be optimized and some optimized media without sources.
     */
    CONTRAVERSIAL
}
