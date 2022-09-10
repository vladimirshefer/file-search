package dev.shefer.searchengine.indexing.filter

interface TokenFilter {
    fun filter(token: String) : String?
}
