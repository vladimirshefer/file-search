package dev.shefer.searchengine.engine.config

import java.nio.file.Path

class IndexSettings(
    /**
     * Source files directory path.
     */
    val sourceDir: String,
    /**
     * Location for index data.
     */
    val dataDir: String,
    val analyzer: Analyzer,
) {
    val sourcePath: Path = Path.of(sourceDir).normalize()
    val dataPath: Path = Path.of(dataDir).normalize()
}
