package dev.shefer.searchengine.engine.repository

import dev.shefer.searchengine.engine.dto.LineLocation

interface TokenRepository {

    /**
     * Add token and location to the index.
     */
    fun registerToken(
        token: String,
        directoryPath: String,
        filename: String,
        lineNumber: Int,
        linePosition: Int
    )

    /**
     * Get all token locations.
     */
    fun findLinesByToken(
        token: String
    ): List<LineLocation>

    /**
     * Check if specific token exists on specified file line.
     */
    fun checkExists(
        searchCandidate: LineLocation,
        queryToken: String
    ): Boolean

    /**
     * Save index into specific directory.
     * If directory or index files do not exist, it will be created.
     */
    fun save(indexDirectory: String)

    /**
     * Load index from specific directory.
     * If directory or index files do not exist, then IOException will be thrown.
     */
    fun load(indexDirectory: String)

    /**
     * Drop any index information both stored and in memory.
     */
    fun drop(indexDirectory: String)

}
