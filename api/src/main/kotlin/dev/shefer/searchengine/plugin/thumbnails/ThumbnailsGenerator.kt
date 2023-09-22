package dev.shefer.searchengine.plugin.thumbnails

import java.nio.file.Path

interface ThumbnailsGenerator {
    fun canRun(sourceAbsolutePath: Path): Boolean

    fun run(sourceAbsolutePath: Path, thumbnailAbsolutePath: Path)
}
