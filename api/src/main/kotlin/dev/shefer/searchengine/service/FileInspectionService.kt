package dev.shefer.searchengine.service

import dev.shefer.searchengine.controller.FileSystemController.Companion.InspectionReport
import dev.shefer.searchengine.plugin.file_inspections.FileInspection
import dev.shefer.searchengine.plugin.file_inspections.InspectionFixResult
import dev.shefer.searchengine.plugin.file_inspections.InspectionResult
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class FileInspectionService(
    private val inspections: List<FileInspection>
) {

    fun runInspections(path: Path): List<Pair<Class<*>, InspectionResult>> {
        return inspections.map { it::class.java as Class<*> to runCatching { it.run(path) }.getOrElse { null } }
            .filter { it.second != null } as List<Pair<Class<*>, InspectionResult>>
    }

    fun fixInspection(inspectionReport: InspectionReport, absolutePath: Path): List<InspectionFixResult> {
        return inspections
            .filter { it::class.java.simpleName == inspectionReport.type}
            .map { it.tryFix(absolutePath) }
    }

}