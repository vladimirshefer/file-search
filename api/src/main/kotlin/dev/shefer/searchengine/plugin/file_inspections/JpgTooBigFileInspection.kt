package dev.shefer.searchengine.plugin.file_inspections

import dev.shefer.searchengine.bash.BashExecutor
import dev.shefer.searchengine.util.ImageFileUtil.imageResolution
import dev.shefer.searchengine.util.ImageFileUtil.isImage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.extension

@Component
class JpgTooBigFileInspection(
    @param:Value("8294400" /*4K resolution 3840x2160)*/)
    private val maxPixels: Int
) : FileInspection {

    init {
        LOG.info("Max image size is $maxPixels")
    }

    override fun run(path: Path): InspectionResult? {
        if (path.extension != "jpg") return null
        if (!path.isImage) return null
        val imageResolution = path.imageResolution
        if (imageResolution.width * imageResolution.height > maxPixels) {
            return InspectionResult("Image is too big ${imageResolution.width}x${imageResolution.height}")
        }
        return null
    }

    override fun tryFix(path: Path): InspectionFixResult {
        run(path) ?: return InspectionFixResult(InspectionFixResult.InspectionFixStatus.NOT_REQUIRED, "Not required")
        val imageResolutionBefore = path.imageResolution

        BashExecutor.resizeDown(path, maxPixels).join()
            .also { LOG.info("JPG resize ${it.output}\n${it.errorOutput}")}

        val imageResolutionAfter = path.imageResolution
        return InspectionFixResult(InspectionFixResult.InspectionFixStatus.FIXED, "Resized ${imageResolutionBefore.width}x${imageResolutionBefore.height} to ${imageResolutionAfter.width}x${imageResolutionAfter.height}")
    }

    companion object {
        val LOG = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

}
