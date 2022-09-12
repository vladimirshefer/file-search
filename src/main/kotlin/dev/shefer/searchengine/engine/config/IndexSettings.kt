package dev.shefer.searchengine.engine.config

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
