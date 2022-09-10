package dev.shefer.searchengine.search

import dev.shefer.searchengine.Analyzer
import dev.shefer.searchengine.LineLocation
import dev.shefer.searchengine.analyze
import dev.shefer.searchengine.engine.service.TokenService
import dev.shefer.searchengine.tokenRepository
import org.springframework.stereotype.Service

@Service
class SearchServiceImpl(
    private val tokenService: TokenService,
    private val analyzer: Analyzer,
) : SearchService {

    override fun search(query: String): List<LineLocation> {
        val queryTokens = analyzer.analyze(query)

        val searchCandidates = tokenRepository.findLinesByToken(queryTokens[0])
        val result = ArrayList<LineLocation>()
        for (searchCandidate in searchCandidates) {
            var allExist = true
            for (queryToken in queryTokens) {
                val checkExists = tokenRepository.checkExists(searchCandidate, queryToken)
                if (!checkExists) {
                    allExist = false
                    break
                }
            }
            if (allExist) result.add(searchCandidate)
        }
        return result
    }
}
