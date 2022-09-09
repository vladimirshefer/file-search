package dev.shefer.searchengine

import dev.shefer.searchengine.indexing.FileIndexer
import dev.shefer.searchengine.indexing.filter.LowercaseTokenFilter
import dev.shefer.searchengine.indexing.filter.TokenFilter
import dev.shefer.searchengine.indexing.tokenizer.StandardTokenizer
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

fun main(args: Array<String>) {
    val context = runApplication<SearchEngineApplication>(*args)

    val fileSystemScanner = FileIndexer()

    val tokenFilters: List<TokenFilter> = listOf(LowercaseTokenFilter())

    val sink: (t: Token) -> Unit = { tl ->
        val token = tokenFilters.fold(tl.token) {t, tf -> tf.filter(t)}
        INDEX.putIfAbsent(token, ArrayList())
        INDEX[token]!!.add(tl)
    }

    val directoryToScan = File(".")
    indexDirectory(directoryToScan, fileSystemScanner, sink)

    println(INDEX)
}

private fun indexDirectory(
    directoryToScan: File,
    fileSystemScanner: FileIndexer,
    sink: (t: Token) -> Unit
) {
    for (listFile in directoryToScan.listFiles()) {
        if (listFile.isFile && EXTENSION_WHITELIST.any { listFile.name.endsWith(it) }) {
            println(listFile.absolutePath)
            fileSystemScanner.indexFile(listFile, StandardTokenizer(TOKEN_DELIM), sink)
        }
        if (listFile.isDirectory) {
            indexDirectory(listFile, fileSystemScanner, sink);
        }
    }
}
