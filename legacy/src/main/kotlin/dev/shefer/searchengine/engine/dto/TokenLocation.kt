package dev.shefer.searchengine.engine.dto

data class TokenLocation(
    val lineLocation: LineLocation,
    val tokenIndex: Int,
)
