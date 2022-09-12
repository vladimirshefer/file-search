package dev.shefer.searchengine.files.dto

import java.nio.file.Path

data class FileInfo(
    val file: Path,
    val size: Long
)
