package dev.shefer.searchengine.engine.service

import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.Token

interface TokenService {

    fun registerToken(token: Token)

    fun findLinesByToken(
        token: String
    ): List<LineLocation>

    fun checkExists(
        searchCandidate: LineLocation,
        queryToken: String
    ): Boolean

    fun flush(directory: String)
}
