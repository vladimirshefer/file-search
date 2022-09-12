package dev.shefer.searchengine.engine.service

import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation
import dev.shefer.searchengine.engine.repository.TokenRepository

class TokenServiceImpl(
    private val tokenRepository: TokenRepository
) : TokenService {

    override fun registerToken(
        token: Token
    ) {
        tokenRepository.registerToken(
            token.token,
            token.tokenLocation.lineLocation.fileLocation.directoryPath,
            token.tokenLocation.lineLocation.fileLocation.fileName,
            token.tokenLocation.lineLocation.lineIndex,
            token.tokenLocation.tokenIndex
        )
    }

    override fun findLinesByToken(token: String): List<TokenLocation> {
        return tokenRepository.findLinesByToken(token)
    }

    override fun checkExists(token: Token): Boolean {
        return tokenRepository.checkExists(token)
    }

    override fun flush(directory: String) {
        tokenRepository.save(directory)
    }

}
