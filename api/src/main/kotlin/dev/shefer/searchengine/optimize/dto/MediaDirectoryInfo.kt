package dev.shefer.searchengine.optimize.dto

data class MediaDirectoryInfo(
    val name: String,
    val path: String,
    val status: DirectorySyncStatus,
    val files: List<MediaInfo>,
    val directories: List<MediaDirectoryInfo>
)
