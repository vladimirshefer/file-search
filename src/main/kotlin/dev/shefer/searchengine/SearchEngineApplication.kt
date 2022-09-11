package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.IndexSettings
import dev.shefer.searchengine.engine.filter.LowercaseTokenFilter
import dev.shefer.searchengine.engine.tokenizer.TrigramTokenizer

val EXTENSION_WHITELIST = listOf(".kt", ".kts", ".gitignore", ".txt", ".properties", ".bat")

fun main() {
    val indexSettings = IndexSettings(
        source = "./src/main",
        data = "./index_data",
        analyzer = Analyzer(
            tokenizer = { TrigramTokenizer() },
            tokenFilters = listOf(
                /* Makes search case insensitive.
                   Remove this token filter to make
                   search case sensitive. */
                LowercaseTokenFilter()
            )
        )
    )

    val searchEngine = SearchEngine(indexSettings)
    searchEngine.loadIndex()

    searchEngine.search("Rec")

    searchEngine.dropIndex()
    val indexProgress = searchEngine.rebuildIndex()
    indexProgress.join()
    searchEngine.search("Rec")

    searchEngine.saveIndex()
}

