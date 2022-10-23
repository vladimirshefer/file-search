package dev.shefer.searchengine.controller

import dev.shefer.searchengine.dto.OptimizeRequest
import dev.shefer.searchengine.optimize.dto.MediaDirectoryInfo
import dev.shefer.searchengine.service.FileSystemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
        @RequestParam(required = false, defaultValue = "")
        path: String,
        @RequestParam(required = false, defaultValue = "source")
        rootName: String
    ): ResponseEntity<ByteArray> {
        return fileSystemService.showFileContent(path, rootName)
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
}
