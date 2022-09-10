package dev.shefer.searchengine

import dev.shefer.searchengine.engine.analysis.Analyzer
import dev.shefer.searchengine.engine.filter.LowercaseTokenFilter
import dev.shefer.searchengine.engine.tokenizer.TrigramTokenizer
import org.junit.jupiter.api.Test

class E2e1Test : E2eBaseTest() {

    override fun analyzer(): Analyzer {
        return Analyzer({ TrigramTokenizer() }, listOf(LowercaseTokenFilter()))
    }

    @Test
    fun name() {
        searchEngine.rebuildIndex()
        verify()
    }

}
