package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.filter.LowercaseTokenFilter
import dev.shefer.searchengine.engine.repository.InMemoryTokenRepository
import dev.shefer.searchengine.engine.service.TokenService
import dev.shefer.searchengine.engine.tokenizer.TrigramTokenizer
import dev.shefer.searchengine.fs.FileAccessor
import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.search.SearchService
import dev.shefer.searchengine.util.console.ConsoleUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.File

@SpringBootApplication
class SearchEngineApplication {
    @Bean
    fun analyzer(): Analyzer {
        return Analyzer(
            { TrigramTokenizer() },
            listOf(
                LowercaseTokenFilter()
            )
        )
    }

    @Bean
    fun inMemoryTokenRepository() = InMemoryTokenRepository()
}

const val TOKEN_DELIM = " ,!@#$%^&*()_-=+./\\?<>\"'{}\t\n"
val EXTENSION_WHITELIST = listOf(".kt", ".kts", ".gitignore")

fun main(args: Array<String>) {
    val context = runApplication<SearchEngineApplication>(*args)

    val analyzer = context.getBean(Analyzer::class.java)
    val searchService = context.getBean(SearchService::class.java)
    val tokenService = context.getBean(TokenService::class.java)

    val sink: (t: Token) -> Unit = { tl ->
        analyzer
            .filterToken(tl.token)
            ?.also { token ->
                tokenService.registerToken(Token(token, tl.tokenLocation))
            }
    }

    val fileSystemScanner = FileIndexer()
    val directoryToScan = File("/home/vshefer/Desktop")
    fileSystemScanner.indexRecursively(directoryToScan, analyzer, sink)

    val searchQuery = "Rec"
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

    println("END!")
}

fun Analyzer.analyze(s: String): List<String> {
    val tokenizer1 = tokenizer()
    val result = ArrayList<String>()
    for (c in s) {
        tokenizer1
            .next(c)
            ?.let { token -> filterToken(token) }
            ?.let { token -> result.add(token) }
    }
    return result
}

fun Analyzer.filterToken(token: String?): String? {
    return tokenFilters.fold(token) { t, tf ->
        t?.let { it: String -> tf.filter(it) }
    }
}

