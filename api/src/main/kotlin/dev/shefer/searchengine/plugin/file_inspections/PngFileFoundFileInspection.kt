package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.bash.BashExecutor
import dev.shefer.searchengine.util.ContentTypeUtil.guessCorrectFileExtension
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.nameWithoutExtension

/**
 * This inspection suggests to convert PNG files to JPG.
 */
@Component
class PngFileFoundFileInspection : FileInspection {
    override fun run(path: Path): InspectionResult? {
        if (path.guessCorrectFileExtension() == "png") {
            return InspectionResult("PNG found")
        }
        return null
    }

    override fun tryFix(path: Path): InspectionFixResult {
        run(path)
            ?: return InspectionFixResult(InspectionFixResult.InspectionFixStatus.NOT_REQUIRED, "There is no such problem")
        val target = path.parent.resolve("${path.nameWithoutExtension}.jpg")
        if (target.exists()) {
            return InspectionFixResult(InspectionFixResult.InspectionFixStatus.FAILED, "Target file already exists")
        }
        path.moveTo(target)
        BashExecutor.toJpg(target).join()
        return InspectionFixResult(InspectionFixResult.InspectionFixStatus.FIXED, "Converted to JPG")
    }
}
