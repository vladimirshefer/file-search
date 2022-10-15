package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.optimize.dto.DirectorySyncStatus
import dev.shefer.searchengine.optimize.dto.FileInfo
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.optimize.dto.MediaInfo
import dev.shefer.searchengine.optimize.dto.MediaStatus
import dev.shefer.searchengine.util.FileUtil
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

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
            val fileStatus = getMediaInfo(sourceMediaRoot.relativize(file))
            status += fileStatus.status
        }
        FileUtil.forEachAccessibleFile(optimizedDir) { file, _ ->
            val fileStatus = getMediaInfo(optimizedMediaRoot.relativize(file))
            status += fileStatus.status
        }
        return MediaDirectoryInfo(
            directory.fileName.toString(),
            directory.toString(),
            status ?: DirectorySyncStatus.EMPTY,
            Files.list(directory).filter { it.isRegularFile() }.count().toInt(),
            Files.list(directory).filter { it.isDirectory() }.count().toInt(),
            listMedia(directory).toList(),
            listDirectories(directory),
        )
    }

    private fun listDirectories(directory: Path): List<MediaDirectoryInfo> {
        val sourceDir = sourceMediaRoot.resolve(directory)
        val optimizedDir = optimizedMediaRoot.resolve(directory)
        val sourceSubdirs = Files.list(sourceDir)
            .filter { it.isDirectory() }
            .map { sourceMediaRoot.relativize(it) }
            .collect(Collectors.toSet())
        val optimizedSubdirs = Files.list(optimizedDir)
            .filter { it.isDirectory() }
            .map { optimizedMediaRoot.relativize(it) }
            .collect(Collectors.toSet())

        val subdirs = sourceSubdirs + optimizedSubdirs

        return subdirs.map { subdir ->
            MediaDirectoryInfo(
                subdir.fileName.toString(),
                directory.toString(),
                DirectorySyncStatus.NONE,
                listMedia(directory).size,
                -1, // TODO
                emptyList(),
                emptyList()
            )
        }
    }

    fun listMedia(directory: Path): Set<MediaInfo> {
        val sourceDir = sourceMediaRoot.resolve(directory)
        val optimizedDir = optimizedMediaRoot.resolve(directory)
        val sourceFiles = Files.list(sourceDir)
            .filter { it.isRegularFile() }
            .map { sourceMediaRoot.relativize(it) }
            .collect(Collectors.toSet())
        val optimizedFiles = Files.list(optimizedDir)
            .filter { it.isRegularFile() }
            .map { optimizedMediaRoot.relativize(it) }
            .collect(Collectors.toSet())
            .toMutableSet()
        val sourceFileMappings: Set<Pair<Path, Path?>> = sourceFiles
            .map { sourceFile ->
                val optimizedFile = optimizedFiles.firstOrNull { it.fileName.startsWith(sourceFile.fileName) }
                sourceFile to optimizedFile
            }
            .toSet()
        val optimizedFilesMapping: Set<Pair<Path?, Path>> = optimizedFiles
            .map { optimizedFile ->
                val sourceFile = sourceFiles.firstOrNull { optimizedFile.fileName.startsWith(it.fileName) }
                sourceFile to optimizedFile
            }
            .toSet()

        val allFilesMappings = sourceFileMappings + optimizedFilesMapping
        return allFilesMappings
            .map { (source, optimized) ->
                MediaInfo(
                    source?.let { getFileInfo(sourceMediaRoot.resolve(source)) },
                    optimized?.let { getFileInfo(optimizedMediaRoot.resolve(optimized)) }
                )
            }
            .toSet()
    }

    private fun getFileInfo(absolutePath: Path): FileInfo? {
        return if (absolutePath.exists()) {
            val size = Files.size(absolutePath)
            val name = absolutePath.fileName.toString()
            val type = absolutePath.extension
            return FileInfo(name, size, type)
        } else {
            null
        }
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
