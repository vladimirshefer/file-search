package dev.shefer.searchengine.plugin.thumbnails

import dev.shefer.searchengine.bash.BashExecutor
import dev.shefer.searchengine.bash.process.assertSuccess
import dev.shefer.searchengine.util.ContentTypeUtil.isVideo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.exists

@Component
class VideoThumbnailsGenerator : ThumbnailsGenerator {
    override fun canRun(sourceAbsolutePath: Path): Boolean {
        return sourceAbsolutePath.isVideo
    }

    override fun run(sourceAbsolutePath: Path, thumbnailAbsolutePath: Path) {
        if (!thumbnailAbsolutePath.exists()) {
            runCatching { createThumbnail(sourceAbsolutePath, thumbnailAbsolutePath) }
                .onFailure { LOG.error("Could not create thumbnail", it) }
        }
    }

    private fun createThumbnail(sourceAbsolutePath: Path, thumbnailAbsolutePath: Path) {
        BashExecutor.videoFrame(sourceAbsolutePath, thumbnailAbsolutePath).join().assertSuccess()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
