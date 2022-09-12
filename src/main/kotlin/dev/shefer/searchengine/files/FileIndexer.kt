package dev.shefer.searchengine.files

import dev.shefer.searchengine.engine.config.IndexSettings
import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation
import dev.shefer.searchengine.engine.util.Progress
import dev.shefer.searchengine.files.dto.DirectoryIndexingProgress
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.reader

class FileIndexer(
    private val indexSettings: IndexSettings
) {

    fun indexDirectoryAsync(sink: (t: Token) -> Unit): Progress {
        val filesList = FileAccessor.getDirectoryInfo(indexSettings.sourcePath)
        val directoryIndexingProgress = DirectoryIndexingProgress(filesList)

        for (fileInfo in filesList.files) {
            val submit: CompletableFuture<Unit> = CompletableFuture.supplyAsync {
                indexFile(fileInfo.file, sink)
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
    private fun indexFile(file: Path, sink: (t: Token) -> Unit): Boolean {
        val tokenizer = indexSettings.analyzer.tokenizer()

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
                    val fileLocation = FileLocation(indexSettings.sourcePath.relativize(file), indexSettings.sourcePath)
                    val lineLocation = LineLocation(fileLocation, lineNum)
                    val tokenLocation = TokenLocation(lineLocation, tokenIndex)
                    val token1 = Token(token, tokenLocation)
                    sink(token1)
                    tokenIndex++
                }

                read = reader.read()
            }
        }
        return true
    }

}
