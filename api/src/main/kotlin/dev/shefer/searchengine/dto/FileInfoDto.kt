package dev.shefer.searchengine.dto

import dev.shefer.searchengine.optimize.dto.MediaStatus

data class FileInfoDto(
    val name: String,
    val size: Long,
    val status: MediaStatus
)
