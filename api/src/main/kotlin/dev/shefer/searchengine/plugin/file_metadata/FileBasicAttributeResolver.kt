package dev.shefer.searchengine.plugin.file_metadata

import dev.shefer.searchengine.util.ContentTypeUtil.contentType
import dev.shefer.searchengine.util.ContentTypeUtil.isVideo
import dev.shefer.searchengine.util.ContentTypeUtil.mediaType
import dev.shefer.searchengine.util.ContentTypeUtil.mimeType
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.*

@Component
class FileBasicAttributeResolver : AttributeResolver {
    override fun get(absolutePath: Path): Map<String, Any> {
        if (absolutePath.isRegularFile()) return mapOf(
            "isFile" to true,
            "isDirectory" to false,
            "fileSize" to absolutePath.fileSize(),
            "fileName" to absolutePath.name,
            "displayName" to absolutePath.name,
            "fileExtension" to absolutePath.extension,
            "mimeType" to absolutePath.mimeType.toString(),
            "mediaType" to absolutePath.mediaType.toString(),
            "contentType" to absolutePath.contentType,
            "isVideo" to absolutePath.isVideo,
            "lastModified" to absolutePath.getLastModifiedTime().toInstant().epochSecond.toString(),
        )
        return emptyMap()
    }
}
