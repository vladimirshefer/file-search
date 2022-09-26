package dev.shefer.searchengine.controller

import dev.shefer.searchengine.dto.DirectoryInfoDto
import dev.shefer.searchengine.dto.FileInfoDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File

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
}
