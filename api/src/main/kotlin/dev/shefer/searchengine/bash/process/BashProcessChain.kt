package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

class BashProcessChain(
    private val chain: List<BashProcess>
) : BashProcess {

    @Volatile
    private var currentProcessIndex = -1;

    override val errorOutput: String
        get() = StringBuilder()
            .also { sb ->
                chain.forEach {
                    sb.append(it.errorOutput)
                }
            }
            .toString()

    override val output: String
        get() = StringBuilder()
            .also { sb ->
                chain.forEach {
                    sb.append(it.output)
                }
            }
            .toString()

    override val status: ProcessStatus
        get() = if (currentProcessIndex == -1) ProcessStatus.PENDING else chain[currentProcessIndex].status

    override fun start(): BashProcess {
        synchronized(this) {
            if (currentProcessIndex == -1) {
                start(0)
            }
        }
        return this
    }

    private fun start(index: Int) {
        if (index !in chain.indices) return
        chain[index].start().onComplete { start(index + 1) }
    }

    override fun cancel() {
        chain.forEach(BashProcess::cancel)
    }

    override fun join(timeoutMs: Long?): BashProcess = apply {
        chain.forEach(BashProcess::join)
    }

    override fun onComplete(action: () -> Unit) {
        chain.last().onComplete(action)
    }

    override fun update() {
        chain.forEach(BashProcess::update)
    }
}
