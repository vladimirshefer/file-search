package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.optimize.dto.DirectorySyncStatus
import dev.shefer.searchengine.optimize.dto.FileInfo
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.optimize.dto.MediaInfo
import dev.shefer.searchengine.optimize.dto.MediaStatus
import dev.shefer.searchengine.util.FileUtil
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

/**
 * Files migrate only from source to optimized root and never backwards.
 */
class MediaOptimizationManager(
    private val mediaOptimizer: MediaOptimizer,
    sourceMediaRoot: Path,
    optimizedMediaRoot: Path,
    thumbnailsMediaRoot: Path,
) {

    private val LOG = LoggerFactory.getLogger(this.javaClass)


    private val sourceMediaSubtree = FileSystemSubtree(sourceMediaRoot)
    private val optimizedMediaSubtree = FileSystemSubtree(optimizedMediaRoot)
    private val thumbnailsMediaSubtree = FileSystemSubtree(thumbnailsMediaRoot)

    fun getMediaInfo(file: Path): MediaInfo {
        val sourceFileInfo = sourceMediaSubtree.getFileInfo(file)
        val optimizedFileInfo = optimizedMediaSubtree.getFileInfo(file)

        return MediaInfo(sourceFileInfo, optimizedFileInfo)
    }

    fun getMediaDirectoryInfo(directory: Path): MediaDirectoryInfo {
        val path = normalizePath(directory)

        return MediaDirectoryInfo(
            path.fileName.toString(),
            path.toString(),
            withTimer("dirstatus") { directorySyncStatus(path) } ?: DirectorySyncStatus.EMPTY,
            withTimer("listmedia") { listMedia(path) }.toList(),
            withTimer("listdirectories") { listDirectories(path) },
        )
    }

    private fun <T : Any?> withTimer(name: String, action: () -> T): T {
        val before = System.currentTimeMillis()
        val result = action()
        val after = System.currentTimeMillis()
        LOG.info("Timer $name ${after - before}")
        return result
    }

    /**
     * Normalizes path and throws exception if out of media root directories
     */
    private fun normalizePath(directory: Path): Path {
        val path = directory.normalize()
        sourceMediaSubtree.resolve(path)
        optimizedMediaSubtree.resolve(path)
        return path
    }

    private fun directorySyncStatus(directory: Path): DirectorySyncStatus? {
        val sourceDir = sourceMediaSubtree.resolve(directory)
        val optimizedDir = optimizedMediaSubtree.resolve(directory)
        var status: DirectorySyncStatus? = null
        FileUtil.forEachAccessibleFile(sourceDir) { file, _ ->
            val fileStatus = getMediaInfo(sourceMediaSubtree.relativize(file))
            status += fileStatus.status
        }
        FileUtil.forEachAccessibleFile(optimizedDir) { file, _ ->
            val fileStatus = getMediaInfo(optimizedMediaSubtree.relativize(file))
            status += fileStatus.status
        }
        return status
    }

    private fun listDirectories(directory: Path): List<MediaDirectoryInfo> {
        val sourceSubdirs = sourceMediaSubtree.listDirectoriesOrEmpty(directory)
        val optimizedSubdirs = optimizedMediaSubtree.listDirectoriesOrEmpty(directory)
        val subdirs = sourceSubdirs + optimizedSubdirs

        return subdirs.map { subdir ->
            MediaDirectoryInfo(
                subdir.fileName.toString(),
                directory.toString(),
                DirectorySyncStatus.NONE,
                emptyList(),
                emptyList()
            )
        }
    }

    private fun listMedia(directory: Path): Set<MediaInfo> {
        val sourceFiles = sourceMediaSubtree.listFilesOrEmpty(directory)
        val optimizedFiles = optimizedMediaSubtree.listFilesOrEmpty(directory)
        val sourceFileMappings: Set<Pair<Path, Path?>> = sourceFiles
            .map { sourceFile ->
                val optimizedFile = optimizedFiles.firstOrNull {
                    it.fileName.toString().startsWith(sourceFile.fileName.toString())
                }
                sourceFile to optimizedFile
            }
            .toSet()
        val optimizedFilesMapping: Set<Pair<Path?, Path>> = optimizedFiles
            .map { optimizedFile ->
                val sourceFile = sourceFiles.firstOrNull {
                    optimizedFile.fileName.toString().startsWith(it.fileName.toString())
                }
                sourceFile to optimizedFile
            }
            .toSet()

        val allFilesMappings = sourceFileMappings + optimizedFilesMapping
        return allFilesMappings
            .map { (source, optimized) ->
                MediaInfo(
                    source?.let { sourceMediaSubtree.getFileInfo(source) },
                    optimized?.let { optimizedMediaSubtree.getFileInfo(optimized) }
                )
            }
            .toSet()
    }

    private fun FileSystemSubtree.getFileInfo(file: Path): FileInfo? {
        val optimizePath = resolve(file)
        return if (optimizePath.exists()) {
            val size = fileSize(file)
            val name = file.fileName.toString()
            val type = file.extension.lowercase()
            FileInfo(name, size, type)
        } else {
            null
        }
    }

    fun optimize(optimizePaths: List<Path>) {
        optimizePaths.map { optimize(it) }
    }

    private fun optimize(relPath: Path) {
        val path = normalizePath(relPath)
        val sourceFile = sourceMediaSubtree.resolve(path)
        val optimizedMedia = optimizedMediaSubtree.resolve(path)
        LOG.info("Start optimizing $path")
        if (sourceFile.isDirectory()) {
            sourceMediaSubtree.listFilesOrEmpty(path).forEach { optimize(it) }
            return
        }
        optimizedMedia.parent.createDirectories()
        if (path.extension.lowercase() in listOf("jpg", "jpeg", "png")) {
            mediaOptimizer.optimizeImage(
                sourceFile,
                optimizedMedia
            )
        }
        if (path.extension.lowercase() in listOf("mp4", "avi", "flv")) {
            mediaOptimizer.optimizeVideo(
                sourceFile,
                optimizedMedia
            )
        }
        LOG.info("End optimizing $path")
    }

    /**
     * @param path relative unsafe path.
     * @return absolute path
     */
    fun find(rootName: String, path: Path): Path {
        if (rootName != "optimized") {
            return sourceMediaSubtree.resolve(path)
        }

        val exactMatch = optimizedMediaSubtree.resolve(path)
        if (exactMatch.exists()) {
            return exactMatch
        }

        return Files
            .list(exactMatch.parent)
            .filter { it.fileName.startsWith(path.fileName) }
            .findAny()
            .orElseThrow { FileNotFoundException("No optimized file for path $path") }
    }

}

private operator fun DirectorySyncStatus?.plus(fileStatus: MediaStatus): DirectorySyncStatus {
    return when (this) {
        null -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.FULLY_OPTIMIZED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_NOT_STARTED
            }
        }


        DirectorySyncStatus.EMPTY -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.FULLY_OPTIMIZED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_NOT_STARTED
            }
        }

        DirectorySyncStatus.OPTIMIZATION_NOT_STARTED -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_NOT_STARTED
            }
        }

        DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
            }
        }

        DirectorySyncStatus.FULLY_OPTIMIZED -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.FULLY_OPTIMIZED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.OPTIMIZATION_PARTIALLY_COMPLETED
            }
        }

        DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
            }
        }

        DirectorySyncStatus.SOURCES_REMOVED -> {
            when (fileStatus) {
                MediaStatus.OPTIMIZED_ONLY -> DirectorySyncStatus.SOURCES_REMOVED
                MediaStatus.OPTIMIZED -> DirectorySyncStatus.SOURCES_PARTIALLY_REMOVED
                MediaStatus.SOURCE_ONLY -> DirectorySyncStatus.CONTRAVERSIAL
            }
        }

        DirectorySyncStatus.CONTRAVERSIAL -> DirectorySyncStatus.CONTRAVERSIAL

        DirectorySyncStatus.NONE -> DirectorySyncStatus.NONE
    }
}
