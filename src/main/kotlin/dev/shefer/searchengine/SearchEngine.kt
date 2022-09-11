package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.filterToken
import dev.shefer.searchengine.engine.dto.IndexSettings
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.repository.InMemoryTokenRepository
import dev.shefer.searchengine.engine.service.TokenServiceImpl
import dev.shefer.searchengine.engine.util.Progress
import dev.shefer.searchengine.fs.FileAccessor
import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.search.SearchServiceImpl
import dev.shefer.searchengine.search.dto.SearchResult
import java.io.File

class SearchEngine(
    private val indexSettings: IndexSettings
) {

    private val fileSystemScanner = FileIndexer()
    private val tokenRepository = InMemoryTokenRepository()
    private val tokenService = TokenServiceImpl(tokenRepository)
    val searchService = SearchServiceImpl(tokenService, indexSettings.analyzer)

    fun rebuildIndex(): Progress {
        File(indexSettings.data).mkdirs()

        val sink: (t: Token) -> Unit = { tl ->
            indexSettings.analyzer
                .filterToken(tl.token)
                ?.also { token ->
                    tokenService.registerToken(Token(token, tl.tokenLocation))
                }
        }

        val directoryToScan = File(indexSettings.source)

        val directoryProgress = fileSystemScanner.indexDirectoryAsync(directoryToScan, indexSettings.analyzer, sink)
        println("Indexing submitted. CurrentProgress = ${directoryProgress.report()}")
        return directoryProgress
    }

    fun saveIndex() {
        tokenRepository.save(indexSettings.data)
    }

    fun loadIndex() {
        tokenRepository.load(indexSettings.data)
    }

    fun dropIndex() {
        tokenRepository.drop(indexSettings.data)
    }

    fun search(searchQuery: String) {
        val lineLocations = searchService.search(searchQuery)
        for (lineLocation in lineLocations) {
            val originalLine = FileAccessor().getLine(lineLocation)
            val startIndex = originalLine.lowercase().indexOf(searchQuery.lowercase())
            if (startIndex < 0) continue
            val searchResult = SearchResult(lineLocation, searchQuery, originalLine, startIndex)
            println(searchResult)
        }
    }

}
