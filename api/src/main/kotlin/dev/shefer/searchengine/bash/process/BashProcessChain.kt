package dev.shefer.searchengine.bash.process

import com.fasterxml.jackson.annotation.JsonInclude
import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus
import kotlin.math.max

class BashProcessChain private constructor(
    val chain: List<BashProcess>
) : BashProcess {

    @Volatile
    private var currentProcessIndex = -1

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

    @get:JsonInclude
    override val status: ProcessStatus
        get() = if (currentProcessIndex == -1) ProcessStatus.PENDING else chain[currentProcessIndex].status

    @Synchronized
    override fun start(): BashProcess {
        if (currentProcessIndex == -1) {
            start(0)
        }
        return this
    }

    @Synchronized
    private fun start(index: Int) {
        if (index !in chain.indices) return
        currentProcessIndex = max(currentProcessIndex, index)
        chain[index].start().onComplete { start(index + 1) }
    }

    override fun cancel() {
        chain.forEach(BashProcess::cancel)
    }

    override fun join(timeoutMs: Long?): BashProcess = apply {
        chain.forEach(BashProcess::join)
        currentProcessIndex = chain.size - 1
    }

    override fun onComplete(action: () -> Unit) {
        if (this.chain.isEmpty()) {
            action()
        } else {
            chain.last().onComplete(action)
        }
    }

    override fun update() {
        chain.forEach(BashProcess::update)
    }

    companion object {
        fun of(chain: List<BashProcess>) =
            if (chain.isEmpty()) {
                MockBashProcess(ProcessStatus.SUCCESS)
            } else {
                BashProcessChain(chain)
            }
    }
}
