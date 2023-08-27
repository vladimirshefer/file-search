package dev.shefer.searchengine.util

import org.apache.tika.mime.MimeType
import org.apache.tika.mime.MimeTypes
import java.net.URLConnection
import java.nio.file.Path


object ContentTypeUtil {
    /**
     * @param path absolute path to file.
     * @return null if unknown content type.
     */
    fun guessCorrectFileExtension(path: Path): String? {
        val file = path.toFile()
        val connection: URLConnection = file.toURI().toURL().openConnection()
        val contentType: String = connection.contentType ?: return null
        val allTypes = MimeTypes.getDefaultMimeTypes()
        val jpeg: MimeType = allTypes.forName(contentType) ?: return null
        return jpeg.extension.takeIf { it.isNotBlank() }?.substring(1)
    }
}
