package dev.shefer.searchengine

import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.indexing.filter.LowercaseTokenFilter
import dev.shefer.searchengine.indexing.filter.TokenFilter
import dev.shefer.searchengine.indexing.tokenizer.StandardTokenizer
import dev.shefer.searchengine.indexing.tokenizer.Tokenizer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class SearchEngineApplication

const val TOKEN_DELIM = " ,!@#$%^&*()_-=+./\\?<>\"'{}\t\n"
val EXTENSION_WHITELIST = listOf(".kt", ".kts", ".gitignore")
val INDEX = HashMap<String, MutableList<Token>>()

data class Token(
    val token: String,
    val filePath: String,
    val lineIndex: Int,
    val tokenIndex: Int
)

class Analyzer(
    val tokenizer: () -> Tokenizer,
    val tokenFilters: List<TokenFilter>,
)

fun main(args: Array<String>) {
    val context = runApplication<SearchEngineApplication>(*args)

    val fileSystemScanner = FileIndexer()

    val tokenFilters: List<TokenFilter> = listOf(LowercaseTokenFilter())

    val analyzer = Analyzer({ StandardTokenizer(TOKEN_DELIM) }, tokenFilters)

    val sink: (t: Token) -> Unit = { tl ->
        val token = analyzer.tokenFilters.fold(tl.token) { t, tf -> tf.filter(t) }
        INDEX.putIfAbsent(token, ArrayList())
        INDEX[token]!!.add(tl)
    }

    val directoryToScan = File(".")
    fileSystemScanner.indexRecursively(directoryToScan, analyzer, sink)

    println(INDEX)

    search("val sink", analyzer)
}

fun search(s: String, analyzer: Analyzer) {
    val queryTokens = analyzer.analyze(s)

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

