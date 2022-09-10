package dev.shefer.searchengine.engine.service

import dev.shefer.searchengine.Token
import dev.shefer.searchengine.engine.dto.LineLocation

interface TokenService {

    fun registerToken(token: Token)

    fun findLinesByToken(
        token: String
    ): List<LineLocation>

    fun checkExists(
        searchCandidate: LineLocation,
        queryToken: String
    ): Boolean
}
