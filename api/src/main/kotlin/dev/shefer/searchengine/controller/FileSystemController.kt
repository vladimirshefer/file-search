package dev.shefer.searchengine.controller

import dev.shefer.searchengine.service.FileSystemService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/files")
class FileSystemController(
    private val fileSystemService: FileSystemService
) {

    @Value("\${app.rootDirectory}")
    lateinit var root: String

    @GetMapping("/list")
    fun listDirectories(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        return fileSystemService.listDirectories(path)
    }

    @GetMapping("/content")
    fun getFileContent(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any?> {
        return fileSystemService.getFileContent(path)
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
        path: String
    ): ResponseEntity<ByteArray> {
        return fileSystemService.showFileContent(path)
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
        return fileSystemService.size(path)
    }
}
