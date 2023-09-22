package dev.shefer.searchengine.engine.tokenizer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrigramTokenizerTest {

    @Test
    fun test() {
        val tokenizer: Tokenizer = TrigramTokenizer()
        val actual = ArrayList<String?>()
        "Hello, World!".forEach { actual.add(tokenizer.next(it)) }

        val expected = listOf(null, null, "Hel", "ell", "llo", "lo,", "o, ", ", W", " Wo", "Wor", "orl", "rld", "ld!")
        assertEquals(expected, actual)
    }

}
