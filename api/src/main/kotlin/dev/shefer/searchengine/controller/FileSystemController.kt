package dev.shefer.searchengine.controller

import dev.shefer.searchengine.dto.OptimizeRequest
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.service.FileSystemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/files")
class FileSystemController(
    private val fileSystemService: FileSystemService
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

    @GetMapping("/readme")
    fun readme(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        return fileSystemService.getReadme(path)
    }

    @GetMapping("/show")
    fun showFileContent(
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

        return fileSystemService.getFileContentBytes(path, rootName, range)
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
}
