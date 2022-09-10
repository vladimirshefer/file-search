package dev.shefer.searchengine.engine.repository

import dev.shefer.searchengine.engine.dto.LineLocation

interface TokenRepository {

    fun registerToken(
        token: String,
        directoryPath: String,
        filename: String,
        lineNumber: Int,
        linePosition: Int
    )

    fun findLinesByToken(
        token: String
    ): List<LineLocation>

    fun checkExists(
        searchCandidate: LineLocation,
        queryToken: String
    ): Boolean

}
