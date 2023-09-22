package dev.shefer.searchengine.engine.dto

import java.nio.file.Path

data class FileLocation(
    /**
     * Path to the file from index sources root
     */
    val path: Path
) {

    val directoryPath: String = path.parent?.toString() ?: ""
    val fileName: String = path.fileName.toString()

    override fun toString(): String {
        return path.toString()
    }
}
