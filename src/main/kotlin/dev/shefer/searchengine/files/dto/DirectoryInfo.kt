package dev.shefer.searchengine.files.dto

data class DirectoryInfo(
    /**
     * List of the files in this directory to be indexed.
     */
    val files: List<FileInfo>,
    /**
     * Returns the size sum for all listed files (in bytes).
     */
    val totalSize: Long
)
