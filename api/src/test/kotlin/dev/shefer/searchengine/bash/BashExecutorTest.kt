package dev.shefer.searchengine.bash

import dev.shefer.searchengine.bash.BashExecutor.Companion.FULLHD_PIXELS
import dev.shefer.searchengine.bash.dto.Resolution
import dev.shefer.test_internal.TestFilesUtil.assertFilesEquals
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.testFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException

class BashExecutorTest {

    @Test
    fun testToJpg() {
        withTempDirectory { testDir ->
            /* GIVEN */
            placeTestFile(testDir, "4K.webp")

            /* WHEN */
            val sourceImg = testDir.resolve("4K.webp")
            BashExecutor.toJpg(sourceImg)

            /* THEN */
            val expectedImg = testFile("4K.webp.toJpg.jpg")
            val actualImg = testDir.resolve("4K.jpg")
            assertFilesEquals(expectedImg, actualImg)
        }
    }

    @Test
    fun testToJpgNotFound() {
        withTempDirectory { testDir ->
            /* GIVEN */
            // no files

            /* WHEN, THEN */
            val sourceImg = testDir.resolve("notExisting")
            assertThrows(FileNotFoundException::class.java) {
                BashExecutor.toJpg(sourceImg)
            }
        }
    }

    @Test
    fun testResizeDown() {
        withTempDirectory { testDir ->
            /* GIVEN */
            placeTestFile(testDir, "4K.webp.toJpg.jpg")

            /* WHEN */
            val sourceImg = testDir.resolve("4K.webp.toJpg.jpg")
            BashExecutor.resizeDown(sourceImg, FULLHD_PIXELS)

            /* THEN */
            val expectedImg = testFile("4K.webp.toJpg.jpg.resizeDown_FULLHD.jpg")
            val actualImg = testDir.resolve("4K.webp.toJpg.jpg")
            assertFilesEquals(expectedImg, actualImg)
        }
    }

    @Test
    fun testVideoResolution__4K_30FPS() {
        withTempDirectory { testDir ->
            /* GIVEN */
            placeTestFile(testDir, "4K_30FPS.mp4")

            /* WHEN */
            val sourceImg = testDir.resolve("4K_30FPS.mp4")
            val actual: Resolution = BashExecutor.videoResolution(sourceImg)

            /* THEN */
            assertEquals(Resolution(3840, 2160), actual)
        }
    }

    @Test
    fun testVideoResolution__FULLHD_60FPS() {
        withTempDirectory { testDir ->
            /* GIVEN */
            placeTestFile(testDir, "FULLHD_60FPS.mp4")

            /* WHEN */
            val sourceImg = testDir.resolve("FULLHD_60FPS.mp4")
            val actual: Resolution = BashExecutor.videoResolution(sourceImg)

            /* THEN */
            assertEquals(Resolution(1920, 1080), actual)
        }
    }

}
