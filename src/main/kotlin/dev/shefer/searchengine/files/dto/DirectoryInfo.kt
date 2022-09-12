package dev.shefer.searchengine.files.dto

data class DirectoryInfo(
    val files: List<FileInfo>,
    val totalSize: Long
)
