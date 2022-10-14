package dev.shefer.searchengine.bash

import dev.shefer.searchengine.bash.BashExecutor.Companion.FULLHD_PIXELS
import dev.shefer.searchengine.bash.dto.Resolution
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import org.springframework.util.ResourceUtils
import java.awt.Desktop
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readBytes

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

    private fun placeTestFile(dir: Path, testFileName: String) {
        val testFile = testFile(testFileName)
        Files.copy(testFile, dir.resolve(testFile.fileName))
    }

    private fun withTempDirectory(action: (dir: Path) -> Unit) {
        val OPEN_DIRECTORY_ON_START = false

        val dir: Path = Files.createTempDirectory(this.javaClass.simpleName)
        try {
            if (OPEN_DIRECTORY_ON_START) {
                Desktop.getDesktop().open(dir.toFile())
            }
            action(dir)
        } finally {
            if (dir.toFile().exists()) {
                FileSystemUtils.deleteRecursively(dir)
            }
        }
    }

    private fun assertFilesEquals(expectedImg: Path, actualImg: Path) {
        assertArrayEquals(expectedImg.readBytes(), actualImg.readBytes())
    }

    private fun resource(path: String): Path {
        return ResourceUtils.getFile(path).toPath()
    }

    private fun testFile(name: String): Path {
        return resource("./src/test/resources/test_files/$name").normalize()
    }
}
