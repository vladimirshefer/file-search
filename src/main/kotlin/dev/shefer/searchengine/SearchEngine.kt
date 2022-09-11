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
import dev.shefer.searchengine.util.console.ConsoleUtil
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
//        fileSystemScanner.indexRecursively(directoryToScan, indexSettings.analyzer, sink)

        val directoryProgress = fileSystemScanner.indexDirectoryAsync(directoryToScan, indexSettings.analyzer, sink)
        println("Indexing submitted. CurrentProgress = ${directoryProgress.report()}")
        tokenService.flush(indexSettings.data)
        return directoryProgress
    }

    private fun resetIndex() {
        TODO()
        File(indexSettings.data).delete()
    }

    fun search(searchQuery: String) {
        val searchResults = searchService.search(searchQuery)

        for (searchResult in searchResults) {
            val originalLine = FileAccessor().getLine(searchResult)
            val startIndex = originalLine.lowercase().indexOf(searchQuery.lowercase())

            if (startIndex < 0) continue

            println(
                "Entry at file " +
                        searchResult.fileLocation.directoryPath +
                        "/" + searchResult.fileLocation.fileName +
                        ":" + searchResult.lineIndex +
                        "\n" + originalLine.substring(0, startIndex) +
                        ConsoleUtil.ANSI_BLUE +
                        originalLine.substring(startIndex, startIndex + searchQuery.length) +
                        ConsoleUtil.ANSI_RESET +
                        originalLine.substring(startIndex + searchQuery.length)
            )
        }
    }

}
