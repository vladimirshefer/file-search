package dev.shefer.searchengine.plugin.file_inspections

import java.nio.file.Path

interface FileInspection {

    /**
     * @param path absolute path to existing file (not directory).
     * @return null if inspection is not supported or not required
     */
    fun run(path: Path): InspectionResult?

    /**
     * Try to fix issue.
     */
    fun tryFix(path: Path): InspectionFixResult {
        return InspectionFixResult(InspectionFixResult.InspectionFixStatus.NOT_SUPPORTED, "This inspection does not support fixes")
    }
}

data class InspectionResult(
    val name: String,
    /**
     * the args, required to fix this issue
     */
    val args: Map<String, String> = emptyMap()
)

data class InspectionFixResult(
    val status: InspectionFixStatus,
    val description: String,
) {
    enum class InspectionFixStatus {
        FIXED, NOT_REQUIRED, NOT_SUPPORTED, FAILED, OTHER,
    }
}
