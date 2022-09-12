package dev.shefer.searchengine

import dev.shefer.searchengine.engine.config.IndexSettings
import dev.shefer.searchengine.engine.config.filterToken
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.index.InvertedIndexImpl
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
    private val invertedIndex = InvertedIndexImpl(indexSettings.dataPath)
    private val searchService = TrigramIndexedSearchService(invertedIndex, indexSettings.analyzer)

    fun rebuildIndex(): Progress {
        File(indexSettings.data).mkdirs()

        val sink: (t: Token) -> Unit = { tl ->
            indexSettings.analyzer
                .filterToken(tl.token)
                ?.also { token ->
                    invertedIndex.registerToken(Token(token, tl.tokenLocation))
                }
        }

        val directoryProgress = fileIndexer.indexDirectoryAsync(sink)
        println("Indexing submitted. CurrentProgress = ${directoryProgress.report()}")
        return directoryProgress
    }

    fun saveIndex() {
        invertedIndex.save()
    }

    fun loadIndex() {
        invertedIndex.load()
    }

    fun dropIndex() {
        invertedIndex.drop()
    }

    fun search(searchQuery: String) {
        val lineLocations = searchService.search(searchQuery)
        for (lineLocation in lineLocations) {
            val path = indexSettings.sourcePath.relativize(lineLocation.fileLocation.path)
            val originalLine = FileAccessor.getLine(path, lineLocation.lineIndex)
            val startIndex = originalLine.lowercase().indexOf(searchQuery.lowercase())
            if (startIndex < 0) continue
            val searchResult = SearchResult(lineLocation, searchQuery, originalLine, startIndex)
            println(searchResult)
        }
    }

}
