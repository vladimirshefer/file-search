package dev.shefer.searchengine.plugin.file_inspections

import java.nio.file.Path

interface FileInspection {
    /**
     * @param path absolute path to existing file (not directory).
     * @return null if inspection is not supported or not required
     */
    fun run(path: Path): InspectionResult?

}

data class InspectionResult(
    val name: String,
)
