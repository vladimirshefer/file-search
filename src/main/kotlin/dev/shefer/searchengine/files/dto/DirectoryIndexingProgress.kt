package dev.shefer.searchengine.files.dto

import dev.shefer.searchengine.engine.util.Progress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong


data class DirectoryIndexingProgress(
    private val directoryInfo: DirectoryInfo
) : Progress {

    private val fileProgresses: MutableList<CompletableFuture<Unit>> = ArrayList()

    private val totalSize = directoryInfo.totalSize
    private var indexed: AtomicLong = AtomicLong()

    private var canceled = false

    override fun report(): Double {
        return indexed.get().toDouble() / totalSize
    }

    override fun cancel() {
        canceled = true
        for (fileProgress in fileProgresses) {
            fileProgress.cancel(true)
        }
    }

    override fun join() {
        for (fileProgress in fileProgresses) {
            fileProgress.join()
        }
    }

    fun fileIndexingStarted(completion: CompletableFuture<Unit>) {
        fileProgresses.add(completion)
        if (canceled) completion.cancel(true)
    }

    fun fileIndexingFinished(fileInfo: FileInfo) {
        indexed.addAndGet(fileInfo.size)
    }
}
