package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.filterToken
import dev.shefer.searchengine.engine.dto.IndexSettings
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.repository.SearchIndexImpl
import dev.shefer.searchengine.engine.util.Progress
import dev.shefer.searchengine.files.FileAccessor
import dev.shefer.searchengine.files.FileIndexer
import dev.shefer.searchengine.search.TrigramIndexedSearchService
import dev.shefer.searchengine.search.dto.SearchResult
import java.io.File

class SearchEngine(
    private val indexSettings: IndexSettings
) {

    private val fileIndexer = FileIndexer(indexSettings)
    private val searchIndex = SearchIndexImpl(indexSettings)
    val searchService = TrigramIndexedSearchService(searchIndex, indexSettings.analyzer)

    fun rebuildIndex(): Progress {
        File(indexSettings.data).mkdirs()

        val sink: (t: Token) -> Unit = { tl ->
            indexSettings.analyzer
                .filterToken(tl.token)
                ?.also { token ->
                    searchIndex.registerToken(Token(token, tl.tokenLocation))
                }
        }

        val directoryProgress = fileIndexer.indexDirectoryAsync(sink)
        println("Indexing submitted. CurrentProgress = ${directoryProgress.report()}")
        return directoryProgress
    }

    fun saveIndex() {
        searchIndex.save()
    }

    fun loadIndex() {
        searchIndex.load()
    }

    fun dropIndex() {
        searchIndex.drop()
    }

    fun search(searchQuery: String) {
        val lineLocations = searchService.search(searchQuery)
        for (lineLocation in lineLocations) {
            val originalLine = FileAccessor.getLine(lineLocation)
            val startIndex = originalLine.lowercase().indexOf(searchQuery.lowercase())
            if (startIndex < 0) continue
            val searchResult = SearchResult(lineLocation, searchQuery, originalLine, startIndex)
            println(searchResult)
        }
    }

}
