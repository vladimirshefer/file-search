package dev.shefer.searchengine.engine.service

import dev.shefer.searchengine.LineLocation
import dev.shefer.searchengine.Token

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
