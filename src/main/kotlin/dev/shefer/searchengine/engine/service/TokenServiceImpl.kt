package dev.shefer.searchengine.engine.service

import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.Token
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

    override fun findLinesByToken(token: String): List<LineLocation> {
        return tokenRepository.findLinesByToken(token)
    }

    override fun checkExists(searchCandidate: LineLocation, queryToken: String): Boolean {
        return tokenRepository.checkExists(searchCandidate, queryToken)
    }

    override fun flush(directory: String) {
        tokenRepository.flush(directory)
    }

}
