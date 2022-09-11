package dev.shefer.searchengine.indexing

import dev.shefer.searchengine.EXTENSION_WHITELIST
import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.util.Progress
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.name

class FileIndexer {

    data class FileInfo(
        val file: Path,
        val size: Long
    )

    data class DirectoryInfo(
        val files: List<FileInfo>,
        val totalSize: Long
    )

    data class FileIndexingProgress(
        val fileInfo: FileInfo,
        val readFuture: CompletableFuture<out Any> // TODO set correct generic
    ) : Progress {

        private var progress = 0.0

        init {
            readFuture.whenCompleteAsync { _, _ ->
                progress = 1.0
            }
        }

        override fun report(): Double {
            return progress
        }

        override fun cancel() {
            readFuture.cancel(true)
        }

    }

    data class DirectoryIndexingProgress(
        val directoryInfo: DirectoryInfo,
        val fileProgresses: List<FileIndexingProgress>
    ) : Progress {

        private val totalSize = directoryInfo.totalSize
        private var indexed: AtomicLong = AtomicLong()

        init {
            for (fileProgress in fileProgresses) {
                fileProgress.readFuture.whenCompleteAsync { _, _ ->
                    indexed.addAndGet(fileProgress.fileInfo.size)
                    println("File ${fileProgress.fileInfo.file} indexed")
                }
            }
        }

        override fun report(): Double {
            return indexed.get().toDouble() / totalSize
        }

        override fun cancel() {
            for (fileProgress in fileProgresses) {
                fileProgress.readFuture.cancel(true)
            }
        }

    }

    fun indexDirectoryAsync(
        directory: File,
        analyzer: Analyzer,
        sink: (t: Token) -> Unit
    ): Progress {
        val filesList = getDirectoryInfo(directory)
        val futures = ArrayList<FileIndexingProgress>()
        for (fileInfo in filesList.files) {
            val submit: CompletableFuture<Boolean> = CompletableFuture.supplyAsync {
                indexFile(fileInfo.file.toFile(), analyzer, sink)
            }
            futures.add(FileIndexingProgress(fileInfo, submit))
        }

        return DirectoryIndexingProgress(filesList, futures)
    }

    fun getDirectoryInfo(
        fileOrDirectory: File
    ): DirectoryInfo {
        val fileList = ArrayList<FileInfo>()
        var totalSize = 0L
        Files.walkFileTree(fileOrDirectory.toPath().normalize(), object : SimpleFileVisitor<Path?>() {
            override fun visitFileFailed(file: Path?, e: IOException?): FileVisitResult {
                System.err.printf("Visiting failed for %s\n", file)
                return FileVisitResult.SKIP_SUBTREE
            }

            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                val result = super.visitFile(file, attrs)
                if (file == null) return result

                val fileSize = attrs?.size() ?: 0

                if (EXTENSION_WHITELIST.any { file.name.endsWith(it) }) {
                    totalSize += fileSize
                    fileList.add(FileInfo(file, fileSize))
                }

                return result
            }
        })

        return DirectoryInfo(fileList, totalSize)
    }

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
            val listFiles = fileOrDirectory.listFiles() ?: return
            for (listFile in listFiles) {
                indexRecursively(listFile, analyzer, sink)
            }
        }
    }

    /**
     * Return true if file has been successfully indexed.
     * Return false if indexing has been cancelled.
     */
    private fun indexFile(file: File, analyzer: Analyzer, sink: (t: Token) -> Unit): Boolean {
        val tokenizer = analyzer.tokenizer()

        val directoryPath = file.parent
        val fileName = file.name

        val reader: InputStreamReader = file.reader()
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
        return true
    }
}
