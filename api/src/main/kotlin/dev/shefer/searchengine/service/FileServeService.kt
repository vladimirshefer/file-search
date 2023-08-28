package dev.shefer.searchengine.service

import dev.shefer.searchengine.util.ContentTypeUtil.isVideo
import dev.shefer.searchengine.util.ContentTypeUtil.mediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize

@Component
class FileServeService {

    /**
     * Will serve file with correct Content-Type.
     * Supports HTTP Range requests.
     * If the file is of video type, then ranges (chunks) are set
     * in response even if there is no or unbounded (0-) range requested.
     */
    fun serveFile(
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

    companion object {
        private val ONE_MEGABYTE = 1000000

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
