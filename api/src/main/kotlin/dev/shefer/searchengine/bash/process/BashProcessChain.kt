package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

class BashProcessChain(
    private val chain: List<BashProcess>
) : BashProcess {
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
        get() {
            chain
                .find { it.status in listOf(ProcessStatus.ERROR, ProcessStatus.CANCELED) }
                ?.also { return it.status }

            chain.find { it.status == ProcessStatus.IN_PROGRESS }
                ?.also { return it.status }

            chain.find { it.status == ProcessStatus.PENDING }
                ?.also { return it.status }

            return ProcessStatus.IN_PROGRESS
        }

    override fun start(): BashProcess {
        TODO("Not yet implemented")
    }

    override fun cancel() {
        chain.forEach(BashProcess::cancel)
    }

    override fun join(timeoutMs: Long?): BashProcess = apply {
        chain.forEach(BashProcess::join)
    }

    override fun onComplete(action: (Process) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun update() {
        chain.forEach(BashProcess::update)
    }
}
