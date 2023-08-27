package dev.shefer.searchengine.service

import dev.shefer.searchengine.plugin.file_inspections.FileInspection
import dev.shefer.searchengine.plugin.file_inspections.InspectionResult
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class FileInspectionService(
    private val inspections: List<FileInspection>
) {

    fun runInspections(path: Path): List<InspectionResult> {
        return inspections.mapNotNull { it.run(path) }
    }

}
