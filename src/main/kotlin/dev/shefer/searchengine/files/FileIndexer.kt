package dev.shefer.searchengine.files

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.util.Progress
import dev.shefer.searchengine.files.dto.DirectoryIndexingProgress
import java.io.File
import java.util.concurrent.CompletableFuture

class FileIndexer {

    fun indexDirectoryAsync(
        directory: File,
        analyzer: Analyzer,
        sink: (t: Token) -> Unit
    ): Progress {
        val filesList = FileAccessor.getDirectoryInfo(directory)
        val directoryIndexingProgress = DirectoryIndexingProgress(filesList)

        for (fileInfo in filesList.files) {
            val submit: CompletableFuture<Unit> = CompletableFuture.supplyAsync {
                indexFile(fileInfo.file.toFile(), analyzer, sink)
                directoryIndexingProgress.fileIndexingFinished(fileInfo)
            }
            directoryIndexingProgress.fileIndexingStarted(submit)
        }

        return directoryIndexingProgress
    }

    /**
     * Return true if file has been successfully indexed.
     * Return false if indexing has been cancelled.
     */
    private fun indexFile(file: File, analyzer: Analyzer, sink: (t: Token) -> Unit): Boolean {
        val tokenizer = analyzer.tokenizer()

        val directoryPath = file.parent
        val fileName = file.name

        file.reader().use { reader ->
            var read = reader.read()

            var lineNum = 0
            var tokenIndex = 0

            while (read != -1) {

                if (Thread.currentThread().isInterrupted) {
                    Thread.interrupted()
                    return false
                }

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
        return true
    }

}
