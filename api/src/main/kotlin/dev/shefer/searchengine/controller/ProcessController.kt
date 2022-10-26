package dev.shefer.searchengine.controller

import dev.shefer.searchengine.optimize.MediaOptimizationManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/processes")
class ProcessController(
    private val mediaOptimizationManager: MediaOptimizationManager
) {

    @GetMapping
    fun list(): Any {
        return mediaOptimizationManager.currentProcesses.map { it.javaClass to it }
    }

}
