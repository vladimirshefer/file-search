package dev.shefer.searchengine.engine.service

import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation

interface TokenService {

    fun registerToken(token: Token)

    fun findLinesByToken(
        token: String
    ): List<TokenLocation>

    fun checkExists(token: Token): Boolean

    fun flush(directory: String)
}
