package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.optimize.dto.MediaStatus
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path

class MediaOptimizationManagerTest {

    @Test
    fun testGetFileInfo_OPTIMIZED() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            placeTestFile(sourcesRoot, "4K.webp", "file1.webp")
            val optimizedRoot = dir.resolve("optimized")
            placeTestFile(optimizedRoot, "4K.webp", "file1.webp")
            val mom = MediaOptimizationManager(sourcesRoot, optimizedRoot)
            val mediaInfo = mom.getMediaInfo(Path.of("file1.webp"))
            Assertions.assertEquals(MediaStatus.OPTIMIZED, mediaInfo.status)
        }
    }

    @Test
    fun testGetFileInfo_OPTIMIZED_ONLY() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            val optimizedRoot = dir.resolve("optimized")
            placeTestFile(optimizedRoot, "4K.webp", "file1.webp")
            val mom = MediaOptimizationManager(sourcesRoot, optimizedRoot)
            val mediaInfo = mom.getMediaInfo(Path.of("file1.webp"))
            Assertions.assertEquals(MediaStatus.OPTIMIZED_ONLY, mediaInfo.status)
        }
    }

    @Test
    fun testGetFileInfo_SOURCE_ONLY() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            placeTestFile(sourcesRoot, "4K.webp", "file1.webp")
            val optimizedRoot = dir.resolve("optimized")
            val mom = MediaOptimizationManager(sourcesRoot, optimizedRoot)
            val mediaInfo = mom.getMediaInfo(Path.of("file1.webp"))
            Assertions.assertEquals(MediaStatus.SOURCE_ONLY, mediaInfo.status)
        }
    }

    @Test
    fun testGetFileInfo_not_existing() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            val optimizedRoot = dir.resolve("optimized")
            val mom = MediaOptimizationManager(sourcesRoot, optimizedRoot)

            assertThrows<RuntimeException> {
                mom.getMediaInfo(Path.of("notExisting.webp"))
            }
        }
    }
}
