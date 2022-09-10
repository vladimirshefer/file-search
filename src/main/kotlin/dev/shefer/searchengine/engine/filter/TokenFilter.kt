package dev.shefer.searchengine.engine.filter

interface TokenFilter {
    fun filter(token: String) : String?
}
