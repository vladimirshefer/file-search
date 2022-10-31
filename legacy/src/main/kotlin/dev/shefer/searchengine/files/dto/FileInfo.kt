package dev.shefer.searchengine.files.dto

import java.nio.file.Path

data class FileInfo(
    /**
     * Full file path
     */
    val file: Path,
    /**
     * The size of the file (in bytes)
     */
    val size: Long
)
