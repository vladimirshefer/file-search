package dev.shefer.searchengine.bash.process

import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BashProcessChainTest {

    @Test
    fun test1() {
        val chain = BashProcessChain.of(
            listOf(
                MockBashProcess(ProcessStatus.PENDING),
                MockBashProcess(ProcessStatus.PENDING),
                MockBashProcess(ProcessStatus.SUCCESS),
            )
        )

        assertEquals(chain.status, ProcessStatus.PENDING)
        chain.start()
        assertEquals(chain.status, ProcessStatus.SUCCESS)
    }

}
