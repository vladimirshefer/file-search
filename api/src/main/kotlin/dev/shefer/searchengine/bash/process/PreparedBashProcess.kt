package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

class PreparedBashProcess(
    private val processBuilder: ProcessBuilder
) : BashProcess {

    private val onCompleteList = ArrayList<(Process) -> Unit>()

    private var activatedBashProcess: ActivatedBashProcess? = null

    override val errorOutput: String = activatedBashProcess?.errorOutput ?: ""

    override val output: String = activatedBashProcess?.output ?: ""

    override var status: ProcessStatus =
        activatedBashProcess?.status ?: ProcessStatus.PENDING

    override fun cancel() {
        activatedBashProcess?.cancel()
    }

    override fun start() = synchronized(this) {
        val activatedBashProcess1 = activatedBashProcess
            ?: ActivatedBashProcess(processBuilder.start())
                .also {
                    onCompleteList.forEach(it::onComplete)
                }

        activatedBashProcess = activatedBashProcess1
        activatedBashProcess1
    }

    override fun join(timeoutMs: Long?): ActivatedBashProcess = start().join()

    override fun onComplete(action: (Process) -> Unit) = synchronized(this) {
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
