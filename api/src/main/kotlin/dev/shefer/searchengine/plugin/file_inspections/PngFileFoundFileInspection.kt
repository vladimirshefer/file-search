package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.util.ContentTypeUtil
import org.springframework.stereotype.Component
import java.nio.file.Path

/**
 * This inspection suggests to convert PNG files to JPG.
 */
@Component
class PngFileFoundFileInspection : FileInspection {
    override fun run(path: Path): InspectionResult? {
        if (ContentTypeUtil.guessCorrectFileExtension(path) == "png") {
            return InspectionResult("PNG found")
        }
        return null
    }
}
