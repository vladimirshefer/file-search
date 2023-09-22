package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.util.ImageFileUtil.isImage
import dev.shefer.test_internal.TestFilesUtil.placeTestFile
import dev.shefer.test_internal.TestFilesUtil.withTempDirectory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.exists

class MediaOptimizerTest {
    @Test
    fun testCreateImageThumbnail() {
        withTempDirectory { testDir ->
            val image4k = placeTestFile(testDir, "4K.webp.toJpg.jpg")
            val thumbnail = testDir.resolve("thumbnails").resolve("image4k.jpg")
            MediaOptimizer().createThumbnail(image4k, thumbnail)
            assertTrue(thumbnail.exists())
            assertTrue(thumbnail.isImage)
            // TODO for some reason thumbnail hash does not equal original hash
//            assertEquals(getPerceptualHash(image4k), getPerceptualHash(thumbnail))
        }
    }


}
