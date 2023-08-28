package dev.shefer.searchengine.plugin.file_metadata

import java.nio.file.Path

interface AttributeResolver {
    fun get(absolutePath: Path): Map<String, Any>
}
