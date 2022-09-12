package dev.shefer.searchengine.engine.dto

import java.nio.file.Path

data class FileLocation(
    /**
     * Path to the file from index sources root
     */
    val path: Path,
    /**
     * Path to the index source root
     */
    val basePath: Path
) {

    val directoryPath: String = path.parent.toString()
    val fileName: String = path.fileName.toString()
    val fullPath: Path = basePath.resolve(path)

    override fun toString(): String {
        return path.toString()
    }
}
