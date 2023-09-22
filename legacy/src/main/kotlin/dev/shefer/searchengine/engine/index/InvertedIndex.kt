package dev.shefer.searchengine.engine.index

import dev.shefer.searchengine.engine.dto.Token
import dev.shefer.searchengine.engine.dto.TokenLocation

interface InvertedIndex {

    /**
     * Add token and location to the index.
     */
    fun registerToken(token: Token)

    /**
     * Get all token locations.
     */
    fun findTokenLocations(token: String): List<TokenLocation>

    /**
     * Check if specific token exists in index.
     */
    fun checkExists(token: Token): Boolean

    /**
     * Save index into specific directory.
     * If directory or index files do not exist, it will be created.
     */
    fun save()

    /**
     * Load index from specific directory.
     * If directory or index files do not exist, then IOException will be thrown.
     */
    fun load()

    /**
     * Drop any index information both stored and in memory.
     */
    fun drop()
}
