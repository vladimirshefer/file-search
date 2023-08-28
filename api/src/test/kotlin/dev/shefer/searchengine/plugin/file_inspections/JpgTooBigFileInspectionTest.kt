package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.plugin.file_inspections.InspectionFixResult.InspectionFixStatus.FIXED
import dev.shefer.searchengine.plugin.file_inspections.InspectionFixResult.InspectionFixStatus.NOT_REQUIRED
import dev.shefer.searchengine.util.ImageFileUtil.imageResolution
import dev.shefer.test_internal.TestFilesUtil.assertFilesEquals
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.testFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class JpgTooBigFileInspectionTest {
    @Test
    fun testFix__Success() {
        withTempDirectory { testDir ->
            val sourceFileName = "image.jpg"
            val image = placeTestFile(testDir, "4K.webp.toJpg.jpg", sourceFileName)
            val fix = JpgTooBigFileInspection(1000000).tryFix(testDir.resolve(sourceFileName))
            assertNotNull(fix)
            assertEquals(FIXED, fix.status)
            assertEquals(1333, image.imageResolution.width)
            assertEquals(750, image.imageResolution.height)
        }
    }

    @Test
    fun testFix__NotRequired__NotJpg() {
        withTempDirectory { testDir ->
            val sourceFileName = "video.mp4"
            placeTestFile(testDir, "4K_30FPS.mp4", sourceFileName)
            val fix = JpgTooBigFileInspection(1000000).tryFix(testDir.resolve(sourceFileName))
            assertNotNull(fix)
            assertEquals(NOT_REQUIRED, fix.status)
        }
    }

    @Test
    fun testFix__NotRequired__AlreadySmall() {
        withTempDirectory { testDir ->
            val sourceFileName = "image.jpg"
            val image = placeTestFile(testDir, "4K.webp.toJpg.jpg", sourceFileName)
            val testImageSource = testFile("4K.webp.toJpg.jpg")
            val fix = JpgTooBigFileInspection(100_000_000).tryFix(testDir.resolve(sourceFileName))
            assertNotNull(fix)
            assertEquals(NOT_REQUIRED, fix.status)
            assertFilesEquals(testImageSource, image) // make sure image is not rescaled
        }
    }
}
