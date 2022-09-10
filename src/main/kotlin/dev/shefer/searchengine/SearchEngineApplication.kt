package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.TokenLocation
import dev.shefer.searchengine.engine.filter.LowercaseTokenFilter
import dev.shefer.searchengine.engine.repository.InMemoryTokenRepository
import dev.shefer.searchengine.engine.service.TokenService
import dev.shefer.searchengine.engine.tokenizer.TrigramTokenizer
import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.search.SearchService
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

data class Token(
    val token: String,
    val tokenLocation: TokenLocation
) {
    constructor(token: String, directoryPath: String, fileName: String, lineId: Int, tokenIndex: Int)
            : this(
        token,
        TokenLocation(
            LineLocation(
                FileLocation(directoryPath, fileName),
                lineId
            ),
            tokenIndex
        )
    )
}

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
    val directoryToScan = File(".")
    fileSystemScanner.indexRecursively(directoryToScan, analyzer, sink)

    val searchResults = searchService.search("fun search")

    println(searchResults)
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

