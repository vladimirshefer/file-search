package dev.shefer.searchengine

import dev.shefer.searchengine.engine.repository.InMemoryTokenRepository
import dev.shefer.searchengine.engine.repository.TokenRepository
import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.indexing.filter.LowercaseTokenFilter
import dev.shefer.searchengine.indexing.filter.TokenFilter
import dev.shefer.searchengine.indexing.tokenizer.Tokenizer
import dev.shefer.searchengine.indexing.tokenizer.TrigramTokenizer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class SearchEngineApplication

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

    val analyzer = Analyzer(
        { TrigramTokenizer() },
        listOf(
            LowercaseTokenFilter()
        )
    )


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

    val searchResults = search("fun search", analyzer)

    println(searchResults)
}

fun search(s: String, analyzer: Analyzer): List<LineLocation> {
    val queryTokens = analyzer.analyze(s)

    val searchCandidates = tokenRepository.findLinesByToken(queryTokens[0])
    val result = ArrayList<LineLocation>()
    for (searchCandidate in searchCandidates) {
        var allExist = true;
        for (queryToken in queryTokens) {
            val checkExists = tokenRepository.checkExists(searchCandidate, queryToken)
            if (!checkExists) {
                allExist = false
                break
            }
        }
        if (allExist) result.add(searchCandidate)
    }
    return result
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

