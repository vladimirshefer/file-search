package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
// 1. get status PENDING | IN PROGRESS | SUCCESS | ERROR | CANCELED
// 2. cancel
// 3. current output
// 4. current error output
 **/
class ActivatedBashProcess(
    private val process: Process
) : BashProcess {

    private var statusCode: Int? = null

    private val processFuture: CompletableFuture<Process>

    private val error: StringBuilder = StringBuilder()

    private val stdout: StringBuilder = StringBuilder()

    override val errorOutput: String
        get() {
            update()
            return error.toString()
        }

    override val output: String
        get() {
            update()
            return stdout.toString()
        }

    override var status: ProcessStatus = ProcessStatus.PENDING
        private set

    override fun start() = this

    override fun cancel() {
        if (process.isAlive) process.destroy()
    }

    override fun join(timeoutMs: Long?): ActivatedBashProcess = apply {
        if (timeoutMs == null) {
            process.waitFor()
        } else {
            process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
        }
        update()
    }

    override fun onComplete(action: (Process) -> Unit) {
        processFuture.thenAccept(action)
    }

    init {
        processFuture = process.onExit().thenApply { it.also { update() } }
        update()
    }

    override fun update() {
        synchronized(process) {
            status = process.processStatus()
            if (!process.isAlive) {
                statusCode = process.exitValue()
            }
            error.append(process.errorReader().readText())
            stdout.append(process.inputReader().readText())
        }
    }

    companion object {

        private fun Process.processStatus(): ProcessStatus {
            if (isAlive) {
                return ProcessStatus.IN_PROGRESS
            }

            return when (exitValue()) {
                0 -> ProcessStatus.SUCCESS
                143, 137 -> ProcessStatus.CANCELED
                else -> ProcessStatus.ERROR
            }
        }
    }
}
