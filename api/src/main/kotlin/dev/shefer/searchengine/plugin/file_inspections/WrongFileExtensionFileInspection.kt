package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.util.ContentTypeUtil
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * Suggests to rename files with wrong file extensions.
 */
@Component
class WrongFileExtensionFileInspection : FileInspection {
    override fun run(path: Path): InspectionResult? {
        val correctExtension = ContentTypeUtil.guessCorrectFileExtension(path) ?: return null;
        val actualExtension = path.extension
        if (actualExtension != correctExtension) {
            return InspectionResult("Wrong extension. Expected $correctExtension, Actual: $actualExtension")
        }
        return null
    }
}
