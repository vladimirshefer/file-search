package dev.shefer.searchengine.service

import dev.shefer.searchengine.dto.OptimizeRequest
import dev.shefer.searchengine.optimize.FileSystemSubtree
import dev.shefer.searchengine.optimize.MediaOptimizationManager
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.plugin.file_metadata.AttributeResolver
import dev.shefer.searchengine.util.FileUtil.forEachAccessibleFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.extension

@Service
class FileSystemService(
    private val mediaOptimizationManager: MediaOptimizationManager,
    @Qualifier("sourceSubtree")
    val sourceSubtree: FileSystemSubtree,
    val attributeResolvers: List<AttributeResolver>,
    val fileServeService: FileServeService
) {

    @Value("\${app.rootDirectory}")
    lateinit var root: String

    fun list(path: String): MediaDirectoryInfo {
        return mediaOptimizationManager.getMediaDirectoryInfo(Path.of(path))
    }

    fun getTextFileContent(path: String): String {
        return Files.readString(resolve(preparePath(path)))
    }

    fun serveFile(
        path: String,
        rootName: String,
        range: LongRange?,
    ): ResponseEntity<ByteArray> {
        val absolutePath = mediaOptimizationManager.find(rootName, preparePath(path))
        return fileServeService.serveFile(absolutePath, range)
    }

    fun stats(path: String): Map<String, Any?> {
        val path = resolve(preparePath(path))
        var forbiddenDirectoriess = 0
        var totalSize = 0L
        val extension2count = HashMap<String, Int>()
        val extension2totalSize = HashMap<String, Long>()

        Files.walkFileTree(path, object : SimpleFileVisitor<Path?>() {
            override fun visitFileFailed(file: Path?, e: IOException?): FileVisitResult {
                System.err.printf("Visiting failed for %s\n", file)
                forbiddenDirectoriess++
                return FileVisitResult.SKIP_SUBTREE
            }

            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                val result = super.visitFile(file, attrs)
                file ?: return result

                val fileSize = attrs?.size() ?: 0
                totalSize += fileSize

                val extension = file.extension
                extension2count.computeIfPresent(extension) { _, v -> v + 1 }
                extension2count.putIfAbsent(extension, 1)

                extension2totalSize.computeIfPresent(extension) { _, v -> v + fileSize }
                extension2totalSize.putIfAbsent(extension, fileSize)

                return result
            }
        })

        return mapOf(
            "forbiddenDirectories" to forbiddenDirectoriess,
            "totalSize" to totalSize,
            "extension2count" to extension2count,
            "extension2totalSize" to extension2totalSize
        )
    }

    fun getReadme(path: String): Map<String, Any?> {
        val dir = resolve(preparePath(path)).toFile()

        if (!dir.isDirectory) {
            throw IllegalArgumentException("Not a directory: $path")
        }

        val indexContent = dir.listFiles()
            .firstOrNull { it.name.lowercase() == "readme.txt" }
            ?.readText()

        return mapOf("content" to indexContent)
    }

    private fun resolve(path: Path): Path {
        val file = Path.of(root).resolve(path).normalize()

        if (!file.exists()) {
            throw IllegalArgumentException("No such file $path")
        }

        return file
    }

    private fun preparePath(path: String): Path {
        val relativePath = if (path.startsWith("/")) path.substring(1) else path
        return Path.of(relativePath).normalize()
    }

    fun size(path: String): Long {
        val file = resolve(preparePath(path))
        if (Files.isRegularFile(file)) {
            return Files.size(file)
        }
        if (Files.isDirectory(file)) {
            var size = 0L
            forEachAccessibleFile(file) { _, attrs ->
                size += attrs.size()
            }
            return size
        }

        throw IllegalArgumentException("Neither file not directory $path")
    }

    fun bigFiles(path: String): Map<String, Any?> {
        TODO("Not yet implemented")
    }

    fun optimize(optimizeRequest: OptimizeRequest) {
        val optimizePaths = optimizeRequest.paths.map { Path.of(optimizeRequest.basePath, it) }
        mediaOptimizationManager.optimize(optimizePaths)
    }

    /**
     * @param path relative path
     */
    fun getInfo(path: String): Map<String, Any> {
        val absolutePath = sourceSubtree.resolve(Path.of(path))

        val attributes = HashMap<String, Any>()
        attributeResolvers
            .map { attributeResolver ->
                runCatching { attributeResolver.get(absolutePath) }
                    .onFailure { e -> LOG.error("Attribute resolver failed: ${attributeResolver::class.java.name}", e) }
                    .getOrElse { emptyMap() }
            }
            .forEach { attributes.putAll(it) }
        return attributes
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
}
