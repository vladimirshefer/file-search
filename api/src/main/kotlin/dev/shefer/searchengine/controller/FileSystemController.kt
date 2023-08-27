package dev.shefer.searchengine.controller

import dev.shefer.searchengine.dto.OptimizeRequest
import dev.shefer.searchengine.optimize.FileSystemSubtree
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.service.FileInspectionService
import dev.shefer.searchengine.service.FileSystemService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

@RestController
@RequestMapping("/api/files")
class FileSystemController(
    private val fileSystemService: FileSystemService,
    private val fileInspectionService: FileInspectionService,
) {

    @GetMapping("/list")
    fun list(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): MediaDirectoryInfo {
        return fileSystemService.list(path)
    }

    @GetMapping("/content")
    fun getFileContent(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        val content = fileSystemService.getTextFileContent(path)
        return mapOf("content" to content)
    }

    @GetMapping("/info")
    fun getInfo(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Any? {
        return fileSystemService.getInfo(path)
    }

    @GetMapping("/readme")
    fun readme(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        return fileSystemService.getReadme(path)
    }

    @GetMapping("/show")
    fun serveFile(
        @RequestHeader(value = "Range", required = false)
        videoRange: String?,
        @RequestParam(required = false)
        chunkSize: Long?,
        @RequestParam(required = false, defaultValue = "")
        path: String,
        @RequestParam(required = false, defaultValue = "source")
        rootName: String
    ): ResponseEntity<ByteArray> {
        val range = parseRange(videoRange, chunkSize) ?: chunkSize?.let { LongRange(0, it - 1) }

        return fileSystemService.serveFile(path, rootName, range)
    }

    @GetMapping("/stats")
    fun stats(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        return fileSystemService.stats(path)
    }

    @GetMapping("/size")
    fun size(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        val size = fileSystemService.size(path)
        return mapOf("size" to size)
    }

    @GetMapping("/big_files")
    fun bigFiles(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        return fileSystemService.bigFiles(path)
    }

    @PostMapping("/optimize")
    fun optimize(
        @RequestBody
        optimizeRequest: OptimizeRequest
    ) {
        fileSystemService.optimize(optimizeRequest)
    }

    private fun parseRange(videoRange: String?, chunkSize: Long?): LongRange? {
        videoRange ?: return null

        val rangeValues = videoRange.split('-', '=')
            .mapNotNull { runCatching { it.toLong() }.getOrNull() }

        val range = when (rangeValues.size) {
            1 -> {
                if (chunkSize == null) LongRange(rangeValues[0], Long.MAX_VALUE)
                else LongRange(rangeValues[0], rangeValues[0] + chunkSize - 1)
            }

            2 -> {
                LongRange(rangeValues[0], rangeValues[1])
            }

            else -> {
                null
            }
        }
        return range
    }

    @GetMapping("/inspections")
    fun inspect(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Any {
        val subtree = fileSystemService.sourceSubtree
        listOf(Path.of(path))
        val inspectionResults = ArrayList<InspectionReport>()
        inspectPathRecursively(subtree, Path.of(path), inspectionResults)
        return inspectionResults
    }

    @PostMapping("/inspections/fix")
    fun inspect(
        @RequestBody
        inspectionReport: InspectionReport
    ): Any {
        val subtree = fileSystemService.sourceSubtree
        return fileInspectionService.fixInspection(inspectionReport, subtree.resolve(Path.of(inspectionReport.path)))
    }

    private fun inspectPathRecursively(
        subtree: FileSystemSubtree,
        relativePath: Path,
        inspectionResults: ArrayList<InspectionReport>
    ) {
        val absolutePath = subtree.resolve(relativePath)
        if (absolutePath.isRegularFile()) {
            inspectionResults.addAll(fileInspectionService.runInspections(absolutePath).map {
                InspectionReport(
                    it.second.name,
                    it.first.simpleName,
                    relativePath.toString()
                )
            })
        } else if (absolutePath.isDirectory()) {
            subtree.listFilesOrEmpty(relativePath).forEach {
                inspectPathRecursively(subtree, it, inspectionResults)
            }
            subtree.listDirectoriesOrEmpty(relativePath).forEach {
                inspectPathRecursively(subtree, it, inspectionResults)
            }
        } else {
            LOG.warn("Neither file nor directory $relativePath")
        }
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(this::class.java)

        data class InspectionReport(
            val description: String,
            val type: String,
            val path: String,
        )
    }
}
