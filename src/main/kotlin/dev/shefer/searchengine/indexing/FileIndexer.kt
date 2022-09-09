package dev.shefer.searchengine.indexing

import dev.shefer.searchengine.Analyzer
import dev.shefer.searchengine.EXTENSION_WHITELIST
import dev.shefer.searchengine.Token
import java.io.File

class FileIndexer {
    fun indexRecursively(
        fileOrDirectory: File,
        analyzer: Analyzer,
        sink: (t: Token) -> Unit
    ) {
        if (fileOrDirectory.isFile) {
            if (EXTENSION_WHITELIST.any { fileOrDirectory.name.endsWith(it) }) {
                indexFile(fileOrDirectory, analyzer, sink)
            }
        }

        if (fileOrDirectory.isDirectory) {
            for (listFile in fileOrDirectory.listFiles()) {
                indexRecursively(listFile, analyzer, sink);
            }
        }
    }

    private fun indexFile(file: File, analyzer: Analyzer, sink: (t: Token) -> Unit) {
        val tokenizer = analyzer.tokenizer()
        val directoryPath = file.parent
        val fileName = file.name
        val reader = file.reader()
        var read = reader.read()
        var lineNum = 0
        var tokenIndex = 0
        while (read != -1) {
            val char = read.toChar()
            if (char == '\n') {
                lineNum++
                tokenIndex = 0
            }
            val token = tokenizer.next(char)
            if (token != null) {
                sink(Token(token, directoryPath, fileName, lineNum, tokenIndex))
                tokenIndex++
            }
            read = reader.read()
        }
    }
}
