package dev.shefer.searchengine.search

import dev.shefer.searchengine.engine.dto.LineLocation

interface SearchService {
    fun search(query: String): List<LineLocation>
}
