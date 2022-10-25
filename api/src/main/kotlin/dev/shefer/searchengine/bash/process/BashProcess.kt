package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

interface BashProcess {
    val errorOutput: String
    val output: String
    val status: ProcessStatus
    fun start(): BashProcess
    fun cancel()

    /**
     * Blocks current thread until process exit.
     * If process is not started, then starts the process.
     */
    fun join(timeoutMs: Long? = null): BashProcess
    fun onComplete(action: (Process) -> Unit)

    /**
     * Synchronizes the status and checks for processUpdates.
     */
    fun update()

    companion object {
        enum class ProcessStatus {
            PENDING,
            IN_PROGRESS,
            SUCCESS,
            ERROR,
            CANCELED,
        }
    }
}

fun <BashProcessLike : BashProcess> BashProcessLike.assertSuccess(): BashProcessLike = apply {
    if (this@assertSuccess.status != ProcessStatus.SUCCESS) {
        throw RuntimeException("Could not execute bash command. Status: ${this@assertSuccess.status}. Error: ${this@assertSuccess.errorOutput}")
    }
}
