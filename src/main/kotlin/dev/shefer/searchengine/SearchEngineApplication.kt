package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.IndexSettings
import dev.shefer.searchengine.engine.filter.LowercaseTokenFilter
import dev.shefer.searchengine.engine.tokenizer.TrigramTokenizer
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class SearchEngineApplication {
}

const val TOKEN_DELIM = " ,!@#$%^&*()_-=+./\\?<>\"'{}\t\n"
val EXTENSION_WHITELIST = listOf(".kt", ".kts", ".gitignore", ".txt")

fun main(args: Array<String>) {
//    val context = runApplication<SearchEngineApplication>(*args)
    val indexSettings = IndexSettings(
        "/home/vshefer/Desktop",
        "./index_data",
        Analyzer(
            { TrigramTokenizer() },
            listOf(
                LowercaseTokenFilter()
            )
        )
    )

    val searchEngine = SearchEngine(indexSettings)
    searchEngine.rebuildIndex()

    searchEngine.search("Rec")

    println("END!")
}

