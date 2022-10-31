package dev.shefer.searchengine.engine.dto

data class LineLocation(
    val fileLocation: FileLocation,
    val lineIndex: Int
) {
    override fun toString(): String {
        return "$fileLocation:$lineIndex"
    }
}
