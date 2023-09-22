package dev.shefer.searchengine.util

import dev.shefer.searchengine.util.AverageHashImageHash.getPerceptualHash
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class AverageHashImageHashTest {
    @Test
    fun testHash() {
        withTempDirectory { testDir ->
            val image4kWebp = placeTestFile(testDir, "4K.webp", "4k.webp")
            val image4k = placeTestFile(testDir, "4K.webp.toJpg.jpg", "4k.jpg")
            val imageFullHD = placeTestFile(testDir, "4K.webp.toJpg.jpg", "fullhd.jpg")
            val image4kWebpHash = getPerceptualHash(image4kWebp)
            val image4kHash = getPerceptualHash(image4k)
            val imageFullHDHash = getPerceptualHash(imageFullHD)
            val expectedHash: Long = -72903118193404912L
            Assertions.assertEquals(expectedHash, image4kWebpHash)
            Assertions.assertEquals(expectedHash, image4kHash)
            Assertions.assertEquals(expectedHash, imageFullHDHash)
        }
    }
}
