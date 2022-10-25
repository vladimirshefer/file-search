package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

class LazyBashProcess(
    private val bashProcess: () -> BashProcess
) : BashProcess {

    @Volatile
    private var target: BashProcess? = null

    private val onCompleteList = ArrayList<() -> Unit>()

    @Volatile
    var isCanceledBeforeStart = false

    override val errorOutput: String
        get() = target?.errorOutput ?: ""

    override val output: String
        get() = target?.output ?: ""

    override val status: ProcessStatus
        get() = target?.status ?: ProcessStatus.PENDING

    @Synchronized
    override fun start(): BashProcess {
        val statedProcess = target?.start()
            ?: run {
                if (isCanceledBeforeStart) return MockBashProcess(ProcessStatus.CANCELED)
                bashProcess().start().also {
                    onCompleteList.forEach(it::onComplete)
                }
            }
        target = statedProcess
        return statedProcess
    }

    @Synchronized
    override fun cancel() {
        target?.cancel()
            ?: run { isCanceledBeforeStart = true }
    }

    @Synchronized
    override fun join(timeoutMs: Long?): BashProcess {
        target?.join()?.also { return it }

        if (isCanceledBeforeStart) {
            return MockBashProcess(ProcessStatus.CANCELED)
        }

        return start().join()
    }

    @Synchronized
    override fun onComplete(action: () -> Unit) {
        target?.onComplete(action) ?: onCompleteList.add(action)
    }

    override fun update() {
        target?.update()
    }
}
