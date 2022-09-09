package dev.shefer.searchengine.indexing

import dev.shefer.searchengine.TOKEN_DELIM
import dev.shefer.searchengine.Token
import dev.shefer.searchengine.indexing.tokenizer.Tokenizer
import java.io.File
import java.util.*
import java.util.function.Consumer

class FileIndexer {

    fun indexFile(file: File, tokenizer: Tokenizer, sink: (t: Token) -> Unit) {
        val filePath = file.absolutePath
        val reader = file.reader()
        var read = reader.read()
        var lineNum = 0
        var tokenIndex = 0
        while (read != -1) {
            val char = read.toChar()
            if (char == '\n') {
                lineNum ++;
                tokenIndex = 0
            }
            val token = tokenizer.next(char)
            if (token != null) {
                sink(Token(token, filePath, lineNum, tokenIndex))
                tokenIndex ++
            }
            read = reader.read()
        }
    }

    fun indexFile(file: File, sink: (t: Token) -> Unit) {
        var offset = 0
        val lines = file.readLines()
        lines.forEachIndexed { idx, line ->
            indexString(line, idx, file.absolutePath, Consumer<Token>(sink))
            offset += line.length
        }
    }

    fun indexString(line: String, lineNum: Int, filePath: String, sink: Consumer<Token>) {
        val tokenizer = StringTokenizer(line, TOKEN_DELIM)
        var i = 0
        while (tokenizer.hasMoreTokens()) {
            val token = tokenizer.nextToken()
            sink.accept(Token(token, filePath, lineNum, i))
            i++
        }
    }
}
