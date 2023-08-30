package dev.shefer.searchengine.optimize

import dev.shefer.searchengine.bash.process.BashProcess
import dev.shefer.searchengine.bash.process.BashProcess.Companion.ProcessStatus
import dev.shefer.searchengine.bash.process.BashProcessChain
import dev.shefer.searchengine.bash.process.MockBashProcess
import dev.shefer.searchengine.optimize.dto.*
import dev.shefer.searchengine.plugin.thumbnails.ThumbnailsGenerator
import dev.shefer.searchengine.util.FileUtil
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

private val NOT_EXISTING_PATH = Path.of("/" + UUID.randomUUID().toString() + "/" + UUID.randomUUID().toString())

/**
 * Files migrate only from source to optimized root and never backwards.
 */
class MediaOptimizationManager(
    private val mediaOptimizer: MediaOptimizer,
    sourceMediaRoot: Path,
    optimizedMediaRoot: Path,
    thumbnailsMediaRoot: Path,
    private val thumbnailsGenerators: List<ThumbnailsGenerator>
) {

    private val LOG = LoggerFactory.getLogger(this.javaClass)

    private val sourceMediaSubtree = FileSystemSubtree(sourceMediaRoot)
    private val optimizedMediaSubtree = FileSystemSubtree(optimizedMediaRoot)
    private val thumbnailsMediaSubtree = FileSystemSubtree(thumbnailsMediaRoot)

    val currentProcesses = ArrayList<BashProcess>()

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
            DirectorySyncStatus.NONE,
//            withTimer("dirstatus") { directorySyncStatus(path) } ?: DirectorySyncStatus.EMPTY,
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
        currentProcesses += BashProcessChain.of(optimizePaths.map { optimize(it) }).start()
    }

    private fun optimize(relPath: Path): BashProcess {
        val path = normalizePath(relPath)
        val sourceFile = sourceMediaSubtree.resolve(path)
        val optimizedMedia = optimizedMediaSubtree.resolve(path)
        LOG.info("Start optimizing $path")
        if (sourceFile.isDirectory()) {
            return BashProcessChain.of(
                sourceMediaSubtree.listFilesOrEmpty(path).map { optimize(it) } +
                        sourceMediaSubtree.listDirectoriesOrEmpty(path).map { optimize(it) }
            )
        }
        optimizedMedia.parent.createDirectories()
        if (path.extension.lowercase() in listOf("jpg", "jpeg", "png")) {
            return mediaOptimizer.optimizeImage(
                sourceFile,
                optimizedMedia
            )
        }
        if (path.extension.lowercase() in listOf("mp4", "avi", "flv")) {
            return mediaOptimizer.optimizeVideo(
                sourceFile,
                optimizedMedia
            )
        }
        LOG.info("End optimizing $path")
        return MockBashProcess(ProcessStatus.SUCCESS)
    }

    /**
     * @param rootName comma separated list of root names such as "source", "optimized", "thumbnails"
     * @param path relative unsafe path.
     * @return absolute path to file in one of specified roots
     */
    fun find(rootName: String, path: Path): Path {
        if (rootName.contains(',')) {
            rootName.split(',').forEach {
                val find = find(it, path)

                if (find.exists()) {
                    return find
                }
            }
            throw NoSuchFileException("Path does not exist $path")
        }

        when (rootName) {
            "thumbnails" -> {
                if (!sourceMediaSubtree.resolve(path).isRegularFile()) {
                    return NOT_EXISTING_PATH
                }
                val thumbnailPath = sourceMediaSubtree.resolve(path)
                    .parent
                    .resolve(DATA_DIR_NAME)
                    .resolve("thumbnails")
                    .resolve(path.name + ".thumbnail.jpg")

                if (!thumbnailPath.exists()) {
                    val sourceAbsolutePath = sourceMediaSubtree.resolve(path)
                    val thumbnailsGenerator = thumbnailsGenerators.find { thumbnailsGenerator ->
                        runCatching {
                            thumbnailsGenerator.canRun(sourceAbsolutePath)
                        }.getOrElse { false }
                    }
                        ?: throw NoSuchFileException("No thumbnail available for $path")
                    runCatching {
                        thumbnailPath.parent.createDirectories()
                        thumbnailsGenerator.run(sourceAbsolutePath, thumbnailPath)
                    }.onFailure { e ->
                        LOG.error("Could not create thumbnail $path", e)
                    }
                }
                return thumbnailPath
            }

            "optimized" -> {
                val exactMatch = optimizedMediaSubtree.resolve(path)

                if (exactMatch.exists()) {
                    return exactMatch
                }

                if (!exactMatch.parent.exists()) return exactMatch

                return Files
                    .list(exactMatch.parent)
                    .filter { it.fileName.startsWith(path.fileName) }
                    .findAny()
                    .orElse(null)
                    ?: exactMatch
            }

            else -> {
                return sourceMediaSubtree.resolve(path)
            }
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

    private fun getThumbnailPath(path: Path): Path = sourceMediaSubtree.resolve(path)
        .parent
        .resolve(DATA_DIR_NAME)
        .resolve("thumbnails")
        .resolve(path.name)

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
