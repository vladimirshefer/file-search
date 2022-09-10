package dev.shefer.searchengine

import dev.shefer.searchengine.engine.repository.InMemoryTokenRepository
import dev.shefer.searchengine.engine.repository.TokenRepository
import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.indexing.filter.LowercaseTokenFilter
import dev.shefer.searchengine.indexing.filter.TokenFilter
import dev.shefer.searchengine.indexing.tokenizer.Tokenizer
import dev.shefer.searchengine.indexing.tokenizer.TrigramTokenizer
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

    val directoryPath: String
        get() = tokenLocation.lineLocation.fileLocation.directoryPath
    val fileName: String
        get() = tokenLocation.lineLocation.fileLocation.fileName

    val lineIndex: Int
        get() = tokenLocation.lineLocation.lineIndex

    val tokenIndex: Int
        get() = tokenLocation.tokenIndex
}

data class TokenLocation(
    val lineLocation: LineLocation,
    val tokenIndex: Int,
)

data class LineLocation(
    val fileLocation: FileLocation,
    val lineIndex: Int
)

data class FileLocation(
    val directoryPath: String,
    val fileName: String
)

class Analyzer(
    val tokenizer: () -> Tokenizer,
    val tokenFilters: List<TokenFilter>,
)

val tokenRepository: TokenRepository = InMemoryTokenRepository()

fun main(args: Array<String>) {
    val context = runApplication<SearchEngineApplication>(*args)

    val fileSystemScanner = FileIndexer()

    val analyzer = context.getBean(Analyzer::class.java)
    val searchService = context.getBean(SearchService::class.java)

    val sink: (t: Token) -> Unit = { tl ->
        val token = analyzer.tokenFilters.fold(tl.token) { t, tf -> tf.filter(t) }
        tokenRepository.registerToken(
            token,
            tl.directoryPath,
            tl.fileName,
            tl.lineIndex,
            tl.tokenIndex
        )
    }

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
            ?.let { token ->
                tokenFilters.fold(token) { t, tf ->
                    tf.filter(t)
                }
            }
            ?.let { token -> result.add(token) }
    }
    return result
}

