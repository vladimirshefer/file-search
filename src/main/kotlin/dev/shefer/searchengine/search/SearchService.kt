package dev.shefer.searchengine.search

import dev.shefer.searchengine.LineLocation

interface SearchService {
    fun search(query: String): List<LineLocation>
}
