package dev.shefer.searchengine.engine.dto

import dev.shefer.searchengine.engine.analysis.Analyzer
import java.nio.file.Path

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
) {
    val sourcePath: Path = Path.of(source).normalize()
}
