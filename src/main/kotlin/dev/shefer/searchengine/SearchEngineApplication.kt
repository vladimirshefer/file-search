package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.IndexSettings
import dev.shefer.searchengine.engine.filter.LowercaseTokenFilter
import dev.shefer.searchengine.engine.tokenizer.TrigramTokenizer

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
    val indexProgress = searchEngine.rebuildIndex()
    while (indexProgress.report() < 0.5) {
        println(indexProgress.report())
        Thread.sleep(50)
    }
    indexProgress.join()
    searchEngine.search("Rec")

    searchEngine.saveIndex()

}
