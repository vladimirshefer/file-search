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
        val rebuildIndex = searchEngine.rebuildIndex()
        rebuildIndex.join()
        searchEngine.saveIndex()
        verifyIndexFiles()

        verifySearch("hello, hello")
        verifySearch("Your heart's been aching, but you're too shy to say it (say it)")
        verifySearch("gonna let you down")
        verifySearch("We all live in a yellow submarine")
        verifySearch("gonna make")
        verifySearch("gonna")
    }

}
