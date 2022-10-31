package dev.shefer.searchengine.engine.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LowercaseTokenFilterTest {

    private val filter = LowercaseTokenFilter()

    @Test
    fun test() {
        assertEquals("@#$%^&*(", filter.filter("@#$%^&*("))
        assertEquals("abacabadabacaba", filter.filter("ABACABADABACABA"))
        assertEquals("    ", filter.filter("    "))
        assertEquals("import org.junit.jupiter.api.test", filter.filter("import org.junit.jupiter.api.Test"))
        assertEquals("the\nlines\nare\nhere", filter.filter("the\nlines\nare\nhere"))
    }
}
