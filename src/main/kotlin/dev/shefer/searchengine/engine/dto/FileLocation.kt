package dev.shefer.searchengine.engine.dto

data class FileLocation(
    val directoryPath: String,
    val fileName: String
) {
    override fun toString(): String {
        return "$directoryPath/$fileName"
    }
}
