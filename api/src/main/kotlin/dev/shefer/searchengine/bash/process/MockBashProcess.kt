package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus

class MockBashProcess(
    override val status: ProcessStatus
) : BashProcess {

    override val errorOutput: String = ""
    override val output: String = ""

    override fun start(): BashProcess {
        return this
    }

    override fun cancel() {
    }

    override fun join(timeoutMs: Long?): BashProcess {
        return this
    }

    override fun onComplete(action: () -> Unit) {
        action()
    }

    override fun update() {
    }
}
