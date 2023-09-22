package dev.shefer.searchengine.plugin.thumbnails

import dev.shefer.searchengine.optimize.MediaOptimizer
import dev.shefer.searchengine.util.ImageFileUtil.isImage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Component
class ImageThumbnailsGenerator(
    private val mediaOptimizer: MediaOptimizer
) : ThumbnailsGenerator {
    override fun canRun(sourceAbsolutePath: Path): Boolean {
        return sourceAbsolutePath.isImage
    }

    override fun run(sourceAbsolutePath: Path, thumbnailAbsolutePath: Path) {
        if (!thumbnailAbsolutePath.exists()) {
            runCatching { createThumbnail(sourceAbsolutePath, thumbnailAbsolutePath) }
                .onFailure { LOG.error("Could not create thumbnail", it) }
        }
    }

    private fun createThumbnail(sourceAbsolutePath: Path, thumbnailAbsolutePath: Path) {
        val sourceImage = sourceAbsolutePath
            .also {
                if (!it.exists()) {
                    throw FileNotFoundException("No image")
                }
            }
        thumbnailAbsolutePath.parent.createDirectories()
        mediaOptimizer.createThumbnail(sourceImage, thumbnailAbsolutePath)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
