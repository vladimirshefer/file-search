package dev.shefer.searchengine.engine.dto

import dev.shefer.searchengine.engine.analysis.Analyzer

class IndexSettings(
    /**
     * Source files directory path.
     */
    val source: String,
    /**
     * Location for index data.
     */
    val data: String,
    val analyzer: Analyzer,
)
