package dev.shefer.searchengine.plugin.file_metadata

import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

@Component
class DirectoryBasicAttributeResolver : AttributeResolver {
    override fun get(absolutePath: Path): Map<String, Any> {
        if (absolutePath.isDirectory()) return mapOf(
            "isFile" to false,
            "isDirectory" to true,
            "directoryName" to absolutePath.name,
            "displayName" to absolutePath.name,
            "filesInside" to Files.list(absolutePath).filter { it.isRegularFile() }.count(),
            "directoriesInside" to Files.list(absolutePath).filter { it.isDirectory() }.count(),
        )
        return emptyMap()
    }
}
