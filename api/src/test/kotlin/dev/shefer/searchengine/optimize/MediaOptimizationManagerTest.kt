package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.optimize.dto.DirectorySyncStatus.CONTRAVERSIAL
import dev.shefer.searchengine.optimize.dto.FileInfo
import dev.shefer.searchengine.optimize.dto.MediaInfo
import dev.shefer.searchengine.optimize.dto.MediaStatus
import dev.shefer.searchengine.optimize.dto.MediaStatus.OPTIMIZED
import dev.shefer.searchengine.optimize.dto.MediaStatus.OPTIMIZED_ONLY
import dev.shefer.searchengine.optimize.dto.MediaStatus.SOURCE_ONLY
import dev.shefer.searchengine.optimize.exceptions.IllegalFileAccessException
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.nio.file.Path

class MediaOptimizationManagerTest {

    @Test
    fun testGetFileInfo_OPTIMIZED() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            placeTestFile(sourcesRoot, "4K.webp", "file1.webp")
            val optimizedRoot = dir.resolve("optimized")
            placeTestFile(optimizedRoot, "4K.webp", "file1.webp")
            val mom = MediaOptimizationManager(mock(), sourcesRoot, optimizedRoot, mock())
            val mediaInfo = mom.getMediaInfo(Path.of("file1.webp"))
            assertEquals(OPTIMIZED, mediaInfo.status)
        }
    }

    private inline fun <reified T> mock(): T {
        return Mockito.mock(T::class.java) as T
    }

    @Test
    fun testGetFileInfo_OPTIMIZED_ONLY() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            val optimizedRoot = dir.resolve("optimized")
            placeTestFile(optimizedRoot, "4K.webp", "file1.webp")
            val mom = MediaOptimizationManager(mock(), sourcesRoot, optimizedRoot, mock())
            val mediaInfo = mom.getMediaInfo(Path.of("file1.webp"))
            assertEquals(MediaStatus.OPTIMIZED_ONLY, mediaInfo.status)
        }
    }

    @Test
    fun testGetFileInfo_SOURCE_ONLY() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            placeTestFile(sourcesRoot, "4K.webp", "file1.webp")
            val optimizedRoot = dir.resolve("optimized")
            val mom = MediaOptimizationManager(mock(), sourcesRoot, optimizedRoot, mock())
            val mediaInfo = mom.getMediaInfo(Path.of("file1.webp"))
            assertEquals(SOURCE_ONLY, mediaInfo.status)
        }
    }

    @Test
    fun testGetFileInfo_not_existing() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            val optimizedRoot = dir.resolve("optimized")
            val mom = MediaOptimizationManager(mock(), sourcesRoot, optimizedRoot, mock())

            assertThrows<RuntimeException> {
                mom.getMediaInfo(Path.of("notExisting.webp"))
            }
        }
    }

//    @Test // TODO enable back.
    fun testGetMediaDirectoryInfo() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            val optimizedRoot = dir.resolve("optimized")
            val mom = MediaOptimizationManager(mock(), sourcesRoot, optimizedRoot, mock())

            placeTestFile(sourcesRoot, "4K.webp", "sourceOnly.webp")
            placeTestFile(sourcesRoot, "4K.webp", "optimized.webp")
            placeTestFile(optimizedRoot, "4K.webp.toJpg.jpg", "optimized.webp.jpg")
            placeTestFile(optimizedRoot, "4K.webp.toJpg.jpg", "optimizedOnly.webp.jpg")

            val actual = mom.getMediaDirectoryInfo(Path.of(""))

            assertEquals("", actual.name)
            assertEquals("", actual.path)
            assertEquals(CONTRAVERSIAL, actual.status)
            assertEquals(listOf(
                MediaInfo(
                    FileInfo("optimized.webp", 1297636, "webp"),
                    FileInfo("optimized.webp.jpg", 2288471, "jpg"),
                    OPTIMIZED
                ),
                MediaInfo(
                    FileInfo("sourceOnly.webp", 1297636, "webp"),
                    null,
                    SOURCE_ONLY
                ),
                MediaInfo(
                    null,
                    FileInfo("optimizedOnly.webp.jpg", 2288471, "jpg"),
                    OPTIMIZED_ONLY
                ),
            ), actual.files)
            assertEquals(emptyList<Any>(), actual.directories)
        }
    }

    @Test
    fun testGetMediaDirectoryInfo_parent_path() {
        withTempDirectory { dir ->
            val sourcesRoot = dir.resolve("sources")
            val optimizedRoot = dir.resolve("optimized")
            val mom = MediaOptimizationManager(mock(), sourcesRoot, optimizedRoot, mock())

            assertThrows<IllegalFileAccessException> {
                mom.getMediaDirectoryInfo(Path.of(".."))
            }
            assertThrows<IllegalFileAccessException> {
                mom.getMediaDirectoryInfo(Path.of("/"))
            }
            assertThrows<IllegalFileAccessException> {
                mom.getMediaDirectoryInfo(Path.of("/val/log"))
            }
        }
    }
}
