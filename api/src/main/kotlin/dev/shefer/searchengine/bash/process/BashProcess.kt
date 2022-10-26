package dev.shefer.searchengine.bash.process

import com.fasterxml.jackson.annotation.JsonInclude
import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

/**
 * Wrapper around java Process and ProcessBuilder,
 * which allows to combine and organize multiple Processes.
 */
interface BashProcess {

    /**
     * Just an identifier for this process. Could be any string.
     * Uniqueness is not guaranteed.
     */
    val id: String
        @JsonInclude
        get() = this.javaClass.simpleName + "$" + this.hashCode().toString()

    /**
     * Error output of the process.
     * If process is PENDING, then returns empty line.
     * If process in IN_PROGRESS, then returns current output.
     * If process is finished, then returns full process error log.
     */
    val errorOutput: String

    /**
     * Standard output of the process.
     * If process is PENDING, then returns empty line.
     * If process in IN_PROGRESS, then returns current output.
     * If process is finished, then returns full process standard log.
     */
    val output: String

    /**
     * Current status of the process.
     */
    val status: ProcessStatus

    /**
     * Starts execution.
     * Idempotent. Multiple invocations do not take effect.
     */
    fun start(): BashProcess

    /**
     * Cancels execution if not finished.
     * If finished, then does nothing.
     * If not started, then sets CANCELED status and prevents from start.
     */
    fun cancel()

    /**
     * Processes, which this process consists of.
     */
    val children: List<BashProcess>
        @JsonInclude
        get() = emptyList()

    /**
     * Blocks current thread until process exit.
     * If process is not started, then starts the process and waits until complete.
     * If process is finished, then exits immediately without side effects.
     * If used with timeout parameter and process is not finished yet, then unblocks the thread and does nothing else.
     * Could be called multiple times.
     */
    fun join(timeoutMs: Long? = null): BashProcess

    /**
     * Run action when the process is over.
     * If action is not started, then records this action and executes after completion.
     * If action is already completed, then executes action as a blocking operation.
     * On multiple times called, the order is not guaranteed, but tries to preserve the order.
     */
    fun onComplete(action: () -> Unit)

    /**
     * Updates the state from command line.
     */
    fun update()

    companion object {
        enum class ProcessStatus {
            /**
             * Process not started.
             */
            PENDING,

            /**
             * Process is running.
             */
            IN_PROGRESS,

            /**
             * Process exited with status code 0 (without error).
             */
            SUCCESS,

            /**
             * Process exited with unknown status code.
             */
            ERROR,

            /**
             * Process has been interrupted or canceled or exited with standard bash exit codes (SIG****)
             */
            CANCELED,
        }
    }
}

fun <BashProcessLike : BashProcess> BashProcessLike.assertSuccess(): BashProcessLike = apply {
    if (status != ProcessStatus.SUCCESS) {
        throw RuntimeException("Could not execute bash command. Status: ${status}. Error: ${errorOutput}")
    }
}
