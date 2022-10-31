package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

class PreparedBashProcess(
    private val processBuilder: () -> ProcessBuilder
) : BashProcess {

    constructor(processBuilder: ProcessBuilder) : this({ processBuilder })

    private val onCompleteList = ArrayList<() -> Unit>()

    @Volatile
    private var activatedBashProcess: ActivatedBashProcess? = null

    @Volatile
    var isCanceledBeforeStart = false

    override val errorOutput: String = activatedBashProcess?.errorOutput ?: ""

    override val output: String = activatedBashProcess?.output ?: ""

    override var status: ProcessStatus =
        activatedBashProcess?.status ?: if (isCanceledBeforeStart) ProcessStatus.CANCELED else ProcessStatus.PENDING

    override fun cancel() {
        synchronized(this) {
            activatedBashProcess?.cancel()
                ?: run { isCanceledBeforeStart = true }
        }
    }

    @Synchronized
    override fun start(): BashProcess {
        activatedBashProcess?.also { return it }

        if (isCanceledBeforeStart) {
            return this
        }

        val process = processBuilder()

        return ActivatedBashProcess(process.start())
            .also {
                onCompleteList.forEach(it::onComplete)
                it.onComplete { this.status = it.status }
                activatedBashProcess = it
            }
    }

    override fun join(timeoutMs: Long?): BashProcess = start().join()

    override fun onComplete(action: () -> Unit) = synchronized(this) {
        val p = activatedBashProcess
        if (p != null) {
            p.onComplete(action)
        } else {
            onCompleteList.add(action)
        }
        Unit
    }

    override fun update() {
        activatedBashProcess?.update()
    }
}
