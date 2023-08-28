package dev.shefer.searchengine.util

import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import org.springframework.http.MediaType
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension


object ContentTypeUtil {
    /**
     * Tries to guess extension based on real file content, not just name.
     * @param this@guessCorrectFileExtension absolute path to file.
     * @return null if unknown content type.
     */
    fun Path.guessCorrectFileExtension(): String? {
        val file = toFile()
        val connection: URLConnection = file.toURI().toURL().openConnection()
        val contentType: String = connection.contentType ?: return null
        runCatching {
            connection.getInputStream().close()
        }
        val allTypes = MimeTypes.getDefaultMimeTypes()
        val jpeg: MimeType = allTypes.forName(contentType) ?: return null
        return jpeg.extension.takeIf { it.isNotBlank() }?.substring(1)
    }

    val Path.mediaType: MediaType get() = MediaType.asMediaType(mimeType)

    val Path.mimeType: org.springframework.util.MimeType get() = org.springframework.util.MimeType.valueOf(contentType)

    val Path.isVideo: Boolean get() = mimeType.type.lowercase() == "video"

    val Path.contentType: String
        get() {
            return Files.probeContentType(this)
                ?: when (extension.lowercase()) {
                    "flv" -> "video/x-flv"
                    "mp4" -> "video/mp4"
                    else -> "text/plain"
                }
        }
}
