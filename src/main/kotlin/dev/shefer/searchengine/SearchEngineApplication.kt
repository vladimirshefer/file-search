package dev.shefer.searchengine

import dev.shefer.searchengine.engine.config.Analyzer
import dev.shefer.searchengine.engine.config.IndexSettings

fun main() {
    val indexSettings = IndexSettings(
        sourceDir = "./src/main",
        dataDir = "./index_data",
        analyzer = Analyzer.DEFAULT // could also try TRIGRAM_CASESENSITIVE
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
