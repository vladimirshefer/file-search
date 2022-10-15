package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.optimize.dto.DirectorySyncStatus
import dev.shefer.searchengine.optimize.dto.FileInfo
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.optimize.dto.MediaInfo
import dev.shefer.searchengine.optimize.dto.MediaStatus
import dev.shefer.searchengine.util.FileUtil
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension

/**
 * Files migrate only from source to optimized root and never backwards.
 */
class MediaOptimizationManager(
    private val sourceMediaRoot: Path,
    private val optimizedMediaRoot: Path
) {

    fun getMediaInfo(file: Path): MediaInfo {
        val sourcePath = sourceMediaRoot.resolve(file)
        val sourceFileInfo = getFileInfo(sourcePath)

        val optimizePath = optimizedMediaRoot.resolve(file)
        val optimizedFileInfo = getFileInfo(optimizePath)

        return MediaInfo(sourceFileInfo, optimizedFileInfo)
    }

    fun getMediaDirectoryInfo(directory: Path): MediaDirectoryInfo {
        val sourceDir = sourceMediaRoot.resolve(directory)
        val optimizedDir = optimizedMediaRoot.resolve(directory)
        var status: DirectorySyncStatus? = null
        FileUtil.forEachAccessibleFile(sourceDir) { file, _ ->
            val fileStatus = getMediaInfo(sourceDir.relativize(file))
            status += fileStatus.status
        }
        FileUtil.forEachAccessibleFile(optimizedDir) { file, _ ->
            val fileStatus = getMediaInfo(optimizedDir.relativize(file))
            status += fileStatus.status
        }
        return MediaDirectoryInfo(directory.fileName.toString(), status ?: DirectorySyncStatus.FULLY_OPTIMIZED)
    }

    private fun getFileInfo(sourcePath: Path): FileInfo? {
        val sourceFileInfo = if (sourcePath.exists()) {
            val size = Files.size(sourcePath)
            val name = sourcePath.fileName.toString()
            val type = sourcePath.extension
            return FileInfo(name, size, type)
        } else {
            null
        }
        return sourceFileInfo
    }

}

private operator fun DirectorySyncStatus?.plus(fileStatus: MediaStatus): DirectorySyncStatus? {
    return when(this) {
        null -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.FULLY_OPTIMIZED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_NOT_STARTED
            }
        }
        DirectorySyncStatus.OPTIMIZATION_NOT_STARTED -> {
            when(fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_NOT_STARTED
            }
        }
        DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED -> {
            when(fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
            }
        }
        DirectorySyncStatus.FULLY_OPTIMIZED -> {
            when(fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.FULLY_OPTIMIZED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
            }
        }
        DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED -> {
            when(fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
            }
        }
        DirectorySyncStatus.SOURCES_REMOVED -> {
            when(fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
            }
        }
        DirectorySyncStatus.CONTRAVERSIAL -> DirectorySyncStatus.CONTRAVERSIAL
    }
}
