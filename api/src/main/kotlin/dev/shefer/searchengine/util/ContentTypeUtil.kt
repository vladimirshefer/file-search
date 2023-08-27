package dev.shefer.searchengine.util

import java.nio.file.Files
import java.nio.file.Path

object ContentTypeUtil {
    /**
     * @param path absolute path to file.
     * @return null if unknown content type.
     */
    fun guessCorrectFileExtension(path: Path): String? {
        val contentType = Files.probeContentType(path) ?: return null
        if (contentType == "image/png") {
            return "png"
        }
        if (contentType == "image/jpeg") {
            return "jpg"
        }
        return null
    }
}
