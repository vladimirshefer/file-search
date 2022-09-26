package dev.shefer.searchengine.search.dto

import dev.shefer.searchengine.engine.dto.LineLocation
import dev.shefer.searchengine.util.console.ConsoleUtil

data class SearchResult(
    val lineLocation: LineLocation,
    val searchQuery: String,
    val originalLine: String,
    val entryPosition: Int
) {

    init {
        if (entryPosition < 0) throw IllegalArgumentException("Index could not be less than 0: $entryPosition")
    }

    override fun toString(): String {
        val startIndex = entryPosition

        val prefix = originalLine.substring(0, startIndex)
        val match = originalLine.substring(startIndex, startIndex + searchQuery.length)
        val suffix = originalLine.substring(startIndex + searchQuery.length)

        return "Entry at file $lineLocation:$startIndex" +
                "\n$prefix${ConsoleUtil.ANSI_BLUE}$match${ConsoleUtil.ANSI_RESET}$suffix"
    }

}
