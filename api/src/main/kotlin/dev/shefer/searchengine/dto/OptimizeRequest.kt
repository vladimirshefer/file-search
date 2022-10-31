package dev.shefer.searchengine.dto

data class OptimizeRequest(
    val basePath: String,
    val paths: List<String>
)
