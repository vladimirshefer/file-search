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
import java.io.RandomAccessFile
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize

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
        return Files.readString(resolve(preparePath(path)))
    }

    fun serveFile(
        path: String,
        rootName: String,
        range: LongRange?,
    ): ResponseEntity<ByteArray> {
        val absolutePath = mediaOptimizationManager.find(rootName, preparePath(path))
        return serveFile(absolutePath, range)
    }

    /**
     * Will serve file with correct Content-Type.
     * Supports HTTP Range requests.
     * If the file is of video type, then ranges (chunks) are set
     * in response even if there is no or unbounded (0-) range requested.
     */
    private fun serveFile(
        absolutePath: Path,
        range: LongRange?,
    ): ResponseEntity<ByteArray> {
        if (absolutePath.isVideo) {
            if (range == null) {
                // Serve only 2KB of video file, until explicitly asked more.
                return serveFile(absolutePath, LongRange(0, 2000))
            }
            if (range.last == Long.MAX_VALUE) {
                // Force serving video by chunks if range end is not specified
                return serveFile(absolutePath, LongRange(range.first, range.first + ONE_MEGABYTE))
            }
        }

        val fileSize = absolutePath.fileSize()
        if (range != null && range.first == 0L && range.last == fileSize - 1) return serveFile(absolutePath, null)

        val content = absolutePath.readBytesRanged(range)
        val mediaType = absolutePath.mediaType

        val responseEntity = if (range == null) ResponseEntity.ok() else ResponseEntity.status(206)
//        val responseEntity = ResponseEntity.ok()
        return responseEntity
            .contentType(mediaType)
            .also {
                if (range != null) {
                    it.header("Cache-Control", "no-cache, no-store, must-revalidate")
                    it.header("Pragma", "no-cache")
                    it.header("Expires", "0")
                    it.header("Accept-Ranges", "bytes")
                    val lastByteIndex = range.first + content.size - 1
                    it.header("Content-Range", "bytes ${range.first}-$lastByteIndex/$fileSize")
                }
            }
            .body(content)
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

    companion object {
        private val ONE_MEGABYTE = 1000000

        private val Path.mediaType: MediaType get() = MediaType.asMediaType(mimeType)

        private val Path.mimeType: MimeType get() = MimeType.valueOf(contentType)

        private val Path.isVideo: Boolean get() = mimeType.type.lowercase() == "video"

        private val Path.contentType: String
            get() {
                return Files.probeContentType(this)
                    ?: when (extension.lowercase()) {
                        "flv" -> "video/x-flv"
                        "mp4" -> "video/mp4"
                        else -> "text/plain"
                    }
            }

        private fun Path.readBytesRanged(range: LongRange?): ByteArray {
            if (range == null) {
                return Files.readAllBytes(this)
            }

            val size = Files.size(this)
            val from = range.first
            val to = minOf(range.last, size - 1)
            val len = to - from + 1
            val buffer = ByteArray(len.toInt())

            RandomAccessFile(toFile(), "r")
                .also { it.seek(from) }
                .use { it.read(buffer, 0, buffer.size) }

            return buffer
        }
    }
}
