package dev.shefer.searchengine.service

import dev.shefer.searchengine.dto.OptimizeRequest
import dev.shefer.searchengine.optimize.MediaOptimizationManager
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.util.FileUtil.forEachAccessibleFile
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
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
    private val mediaOptimizationManager: MediaOptimizationManager
) {

    @Value("\${app.rootDirectory}")
    lateinit var root: String

    fun list(path: String): MediaDirectoryInfo {
        return mediaOptimizationManager.getMediaDirectoryInfo(Path.of(path))
    }

    fun getTextFileContent(path: String): String {
        val path = if (path.startsWith("/")) path.substring(1) else path
        return Files.readString(resolve(path))
    }

    fun showFileContent(path: String, rootName: String): ResponseEntity<ByteArray> {
        val path = if (path.startsWith("/")) path.substring(1) else path
        val absolutePath = mediaOptimizationManager.find(rootName, Path.of(path))
        val content = Files.readAllBytes(absolutePath)
        val contentType = Files.probeContentType(absolutePath) ?: "text/plain"
        val mimeType = MimeType.valueOf(contentType)
        val mediaType = MediaType.asMediaType(mimeType)
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(content)
    }

    fun stats(path: String): Map<String, Any?> {
        val path = resolve(path)

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
        val dir = resolve(path).toFile()

        if (!dir.isDirectory) {
            throw IllegalArgumentException("Not a directory: $path")
        }

        val indexContent = dir.listFiles()
            .firstOrNull { it.name.lowercase() == "readme.txt" }
            ?.readText()

        return mapOf("content" to indexContent)
    }

    private fun resolve(path: String): Path {
        val path = if (path.startsWith("/")) path.substring(1) else path
        val file = Path.of(root).resolve(path).normalize()

        if (!file.exists()) {
            throw IllegalArgumentException("No such file $path")
        }

        return file
    }

    fun size(path: String): Long {
        val file = resolve(path)
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
}
