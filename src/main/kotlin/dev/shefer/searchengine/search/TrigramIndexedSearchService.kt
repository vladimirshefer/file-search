package dev.shefer.searchengine.search

import dev.shefer.searchengine.engine.config.Analyzer
import dev.shefer.searchengine.engine.config.analyze
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation
import dev.shefer.searchengine.engine.index.InvertedIndex

/**
 * Searches for text using trigram inverted index
 */
class TrigramIndexedSearchService(
    private val index: InvertedIndex,
    private val analyzer: Analyzer,
) : SearchService {

    override fun search(query: String): List<LineLocation> {
        val queryTokens = analyzer.analyze(query)
            .also { if (it.isEmpty()) return emptyList() }

        val searchCandidates = index.findTokenLocations(queryTokens[0])

        return searchCandidates
            .filter { checkSearchCandidate(it, queryTokens) }
            .map { it.lineLocation }
    }

    private fun checkSearchCandidate(
        tokenLocation: TokenLocation,
        queryTokens: List<String>
    ): Boolean {
        var allExist = true

        /*
        since we use trigrams, then checking every trigram is redundant.
        we could check only every third trigram and the last one for trailing.
        example: line: [abcdefghiklmnop], query: [defghikl]
        then we gonna check [[def],[ghi],[ikl]], where "ikl" is trailing trigram
        */
        for (i in 3 until queryTokens.size step 3) {
            val tokenExists = checkTokenExists(tokenLocation, queryTokens, i)
            if (!tokenExists) {
                allExist = false
                break
            }
        }
        val lastExists = checkTokenExists(tokenLocation, queryTokens, queryTokens.lastIndex)
        if (!lastExists) {
            allExist = false
        }
        return allExist
    }

    private fun checkTokenExists(tokenLocation: TokenLocation, queryTokens: List<String>, i: Int): Boolean {
        return checkTokenExists(queryTokens[i], tokenLocation, i)
    }

    private fun checkTokenExists(queryToken: String, tokenLocation: TokenLocation, indexShift: Int): Boolean {
        return index.checkExists(
            Token(
                queryToken,
                TokenLocation(
                    tokenLocation.lineLocation,
                    tokenLocation.tokenIndex + indexShift
                )
            )
        )
    }
}
