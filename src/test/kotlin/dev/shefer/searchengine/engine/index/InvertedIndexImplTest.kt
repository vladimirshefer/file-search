package dev.shefer.searchengine.engine.index

import dev.shefer.searchengine.engine.dto.FileLocation
import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class InvertedIndexImplTest {

    @Test
    internal fun test() {
        val baseDir = Path.of("/_s")
        val index: InvertedIndex = InvertedIndexImpl(baseDir, baseDir)

        val token11 = t("token1", TokenLocation(l("/file1.txt", baseDir, 13), 55))
        val token21 = t("token2", TokenLocation(l("/file2.txt", baseDir, 19), 74))
        val token22 = t("token2", TokenLocation(l("/file4.txt", baseDir, 18), 23))

        assertFalse(index.checkExists(token11))
        assertFalse(index.checkExists(token21))
        assertEquals(emptyList<TokenLocation>(), index.findTokenLocations("token1"))

        index.registerToken(token11)
        assertTrue(index.checkExists(token11))
        assertFalse(index.checkExists(token21))
        assertFalse(index.checkExists(token22))
        assertEquals(listOf(token11.tokenLocation), index.findTokenLocations("token1"))
        assertEquals(emptyList<TokenLocation>(), index.findTokenLocations("token2"))

        index.registerToken(token21)
        assertTrue(index.checkExists(token11))
        assertTrue(index.checkExists(token21))
        assertFalse(index.checkExists(token22))
        assertEquals(listOf(token11.tokenLocation), index.findTokenLocations("token1"))
        assertEquals(listOf(token21.tokenLocation), index.findTokenLocations("token2"))

        index.registerToken(token22)
        assertTrue(index.checkExists(token11))
        assertTrue(index.checkExists(token21))
        assertTrue(index.checkExists(token22))
        assertEquals(listOf(token11.tokenLocation), index.findTokenLocations("token1"))
        assertEquals(setOf(token21.tokenLocation, token22.tokenLocation), index.findTokenLocations("token2").toSet())

        index.drop()
        assertFalse(index.checkExists(token11))
        assertFalse(index.checkExists(token21))
        assertFalse(index.checkExists(token22))
    }

    private fun t(token: String, tokenLocation: TokenLocation): Token {
        return Token(token, tokenLocation)
    }

    private fun l(path: String, sourcePath: Path, lineIndex: Int): LineLocation {
        return LineLocation(FileLocation(Path.of(path), sourcePath), lineIndex)
    }
}
