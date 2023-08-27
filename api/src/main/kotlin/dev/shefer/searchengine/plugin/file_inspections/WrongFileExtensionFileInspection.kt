package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.plugin.file_inspections.InspectionFixResult.InspectionFixStatus.*
import dev.shefer.searchengine.util.ContentTypeUtil
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.moveTo
import kotlin.io.path.nameWithoutExtension

/**
 * Suggests to rename files with wrong file extensions.
 */
@Component
class WrongFileExtensionFileInspection : FileInspection {
    override fun run(path: Path): InspectionResult? {
        val correctExtension = ContentTypeUtil.guessCorrectFileExtension(path) ?: return null
        val actualExtension = path.extension

        if (actualExtension != correctExtension) {
            return InspectionResult("Wrong extension. Expected $correctExtension, Actual: $actualExtension")
        }

        return null
    }

    override fun tryFix(path: Path): InspectionFixResult {
        run(path)
            ?: return InspectionFixResult(NOT_REQUIRED, "There is no such problem")

        val correctExtension = ContentTypeUtil.guessCorrectFileExtension(path)
            ?: return InspectionFixResult(FAILED, "Could not find correct extension for this file")

        val target = path.parent.resolve("${path.nameWithoutExtension}.$correctExtension")
        if (target.exists()) {
            return InspectionFixResult(FAILED, "Target file already exists")
        }
        path.moveTo(target)
        return InspectionFixResult(FIXED, "Renamed")
    }
}
