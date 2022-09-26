package dev.shefer.searchengine.controller

import dev.shefer.searchengine.dto.DirectoryInfoDto
import dev.shefer.searchengine.dto.FileInfoDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.nio.file.Files

@RestController
@RequestMapping("/api/files")
class FileSystemController {

    @Value("\${app.rootDirectory}")
    lateinit var root: String

    @GetMapping("/list")
    fun listDirectories(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, List<Any>> {
        val children = File(root + path).listFiles().toList()
        val directories = children
            .filter { it.isDirectory }
            .map { DirectoryInfoDto(it.name) }
        val files = children
            .filter { it.isFile }
            .map { FileInfoDto(it.name, it.length()) }
        return mapOf(
            "files" to files,
            "directories" to directories
        )
    }

    @GetMapping("/content")
    fun getFileContent(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): Map<String, Any> {
        val file = File(root + path)
        val content = Files.readString(file.toPath())
        return mapOf(
            "content" to content
        )
    }

    @GetMapping("/show")
    fun showFileContent(
        @RequestParam(required = false, defaultValue = "")
        path: String
    ): ResponseEntity<ByteArray> {
        val file = File(root + path)
        val path = file.toPath()
        val content = Files.readAllBytes(path)
        val contentType = Files.probeContentType(path) ?: "text/plain"
        val mimeType = MimeType.valueOf(contentType)
        val mediaType = MediaType.asMediaType(mimeType)
        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(content)
    }
}
