package dev.shefer.searchengine.controller

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
    ): Map<String, List<String>> {
        return mapOf("files" to File(root + path).listFiles().toList().map { it.name })
    }
}
